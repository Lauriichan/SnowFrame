package me.lauriichan.snowframe;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;
import me.lauriichan.snowframe.resource.source.FileDataSource;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.resource.source.PathDataSource;
import me.lauriichan.snowframe.signal.SignalManager;
import me.lauriichan.snowframe.util.color.SimpleColor;
import me.lauriichan.snowframe.util.tick.BlockingTicker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Objects;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.PriorityQueues;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

@Extension
public class ImGUIModule implements ISnowFrameModule {

    public static final ScopedValue<Long> DELTA_TIME = ScopedValue.newInstance();

    public static final String STARTUP_CHAIN = "IMGUI_STARTUP";
    public static final String RENDER_CHAIN = "IMGUI_RENDER";

    private final SimpleColor background = SimpleColor.sRGB(0, 0, 0, 0);

    private final PriorityQueue<Runnable> taskQueue = PriorityQueues.synchronize(new ObjectArrayFIFOQueue<>());

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private volatile boolean requestFontAtlasReload = false;

    private ISimpleLogger logger;
    private SignalManager signalManager;

    private WindowConfiguration configuration;
    private String glslVersion = null;
    private long windowPointer;

    private double previousScrollOffsetX, previousScrollOffsetY;
    private double scrollOffsetX, scrollOffsetY;

    private float scrollDeltaX, scrollDeltaY;

    private volatile BlockingTicker renderTicker;

    @Override
    public void setupLifecycle(LifecycleBuilder<?> builder) {
        builder.chain(STARTUP_CHAIN).newPhase("window", true).newPhase("setup", true).newPhase("start", true);
        builder.chain(RENDER_CHAIN).newPhase("render", true);
    }

    @Override
    public void setupLifecyclePostModule(LifecycleBuilder<?> builder) {
        builder.shutdownChain().newPhase("dispose", false);
    }

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.shutdownChain().register("shutdown", Stage.PRE, this::onShutdown).register("dispose", Stage.MAIN, this::onDispose);
        lifecycle.chainOrThrow(STARTUP_CHAIN).register("window", Stage.MAIN, this::onWindow).register("setup", Stage.MAIN, this::onSetup)
            .register("start", Stage.MAIN, this::onStart);

        logger = lifecycle.snowFrame().logger();
        signalManager = lifecycle.snowFrame().module(SignalModule.class).signalManager();
    }

    /*
     * Control
     */

    public void queueTask(Runnable runnable) {
        taskQueue.enqueue(runnable);
    }

    public void requestFontAtlasReload() {
        requestFontAtlasReload = true;
    }

    public final void setWindowIcon(IDataSource dataSource) {
        Path path;
        if (dataSource instanceof FileDataSource fileSource) {
            path = fileSource.getSource().toPath();
        } else if (dataSource instanceof PathDataSource pathSource) {
            path =  pathSource.getSource();
        } else {
            throw new IllegalArgumentException("Unsupported IDataSource");
        }
        ByteBuffer image;
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            String filePath = path.toAbsolutePath().toString();
            IntBuffer channelsBuf = stack.mallocInt(1);
            IntBuffer widthBuf = stack.mallocInt(1);
            IntBuffer heightBuf = stack.mallocInt(1);
            image = STBImage.stbi_load(filePath, widthBuf, heightBuf, channelsBuf, 4);
            if (image == null) {
                throw new IOException("Unable to load image from resource");
            }
            width = widthBuf.get();
            height = heightBuf.get();
        } catch (IOException e) {
            logger.warning("Failed to set window icon", e);
            return;
        }
        try (GLFWImage.Buffer imgBuf = GLFWImage.create(1)) {
            imgBuf.get(0).set(width, height, image);
            GLFW.glfwSetWindowIcon(windowPointer, imgBuf);
        }
    }

    /*
     * Getter
     */

    public boolean isRendering() {
        return renderTicker != null && renderTicker.isAlive();
    }
    
    public BlockingTicker renderTicker() {
        return renderTicker;
    }

    public SimpleColor background() {
        return background;
    }

    public long windowPointer() {
        return windowPointer;
    }

    public float scrollDeltaX() {
        return scrollDeltaX;
    }

    public float scrollDeltaY() {
        return scrollDeltaY;
    }

    /*
     * Lifecycle
     */

    private void onWindow(SnowFrame<?> frame) {
        GLFWErrorCallback.create(new GLFWLoggerCallback(frame.logger())).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        decideGlslVersions();

        WindowConfiguration.Signal signal = new WindowConfiguration.Signal();
        signalManager.call(signal);
        this.configuration = signal.asConfiguration();

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        if (configuration.borderless()) {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        }
        if (configuration.transparent()) {
            GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
        }
        windowPointer = GLFW.glfwCreateWindow(configuration.width(), configuration.height(), configuration.title(), MemoryUtil.NULL,
            MemoryUtil.NULL);

        if (windowPointer == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Center window
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer pWidth = stack.mallocInt(1); // int*
            final IntBuffer pHeight = stack.mallocInt(1); // int*

            GLFW.glfwGetWindowSize(windowPointer, pWidth, pHeight);
            final GLFWVidMode vidmode = Objects.requireNonNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()));
            GLFW.glfwSetWindowPos(windowPointer, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        GLFW.glfwMakeContextCurrent(windowPointer);

        GL.createCapabilities();

        GLFW.glfwSwapInterval(GLFW.GLFW_TRUE);

        if (configuration.fullscreen()) {
            GLFW.glfwMaximizeWindow(windowPointer);
        } else {
            GLFW.glfwShowWindow(windowPointer);
        }

        clearBuffer();
        GLFW.glfwSwapBuffers(windowPointer);
        GLFW.glfwPollEvents();

        GLFW.glfwSetScrollCallback(windowPointer, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                scrollOffsetX = xOffset;
                scrollOffsetY = yOffset;
            }
        });
    }

    private void onSetup(SnowFrame<?> frame) {
        ImGui.createContext();
        imGuiGlfw.init(windowPointer, true);
        imGuiGl3.init(glslVersion);
    }

    private void onStart(SnowFrame<?> frame) {
        Runnable render = () -> {
            if (GLFW.glfwWindowShouldClose(windowPointer)) {
                renderTicker.stop();
                return;
            }
            render(frame);
        };
        renderTicker = new BlockingTicker((delta) -> ScopedValue.where(DELTA_TIME, delta).run(render));
        signalManager.call(new SetupRenderSignal(renderTicker, background, windowPointer));
        renderTicker.run();
        renderTicker = null;
    }

    private void onShutdown(SnowFrame<?> frame) {
        // Ensure window is being closed and shut down
        BlockingTicker ticker = renderTicker;
        if (ticker != null) {
            Thread thread = ticker.currentThread();
            if (thread != null && thread != Thread.currentThread()) {
                renderTicker = null;
                ticker.stop();
                while (ticker.isAlive()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    private void onDispose(SnowFrame<?> frame) {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(windowPointer);
        GLFW.glfwDestroyWindow(windowPointer);
        GLFW.glfwTerminate();
        Objects.requireNonNull(GLFW.glfwSetErrorCallback(null)).free();
    }

    private void render(SnowFrame<?> frame) {
        // Execute queued tasks
        while (!taskQueue.isEmpty()) {
            try {
                taskQueue.dequeue().run();
            } catch (RuntimeException exp) {
                frame.logger().error("Failed to run task", exp);
            }
        }

        // Refresh font atlas
        if (requestFontAtlasReload) {
            imGuiGl3.destroyFontsTexture();
            requestFontAtlasReload = false;
        }

        // Calculate scroll delta
        scrollDeltaX = (float) (previousScrollOffsetX - scrollOffsetX);
        scrollDeltaY = (float) (previousScrollOffsetY - scrollOffsetY);
        previousScrollOffsetX = scrollOffsetX;
        previousScrollOffsetY = scrollOffsetY;
        
        // Pull GLFW Events before rendering
        GLFW.glfwPollEvents();

        // Start Frame
        clearBuffer();
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // Application frame
        frame.lifecycle().execute(RENDER_CHAIN);

        // End Frame
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        // Update and Render additional Platform Windows
        // (Platform functions may change the current OpenGL context, so we save/restore it to make it easier to paste this code elsewhere.
        //  For this specific demo app we could also call glfwMakeContextCurrent(window) directly)
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupCurrentContext = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupCurrentContext);
        }
        GLFW.glfwSwapBuffers(windowPointer);

    }

    /*
     * Helper
     */

    private void decideGlslVersions() {
        final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        if (isMac) {
            glslVersion = "#version 150";
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);  // 3.2+ only
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);          // Required on Mac
        } else {
            glslVersion = "#version 130";
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
        }
    }

    private void clearBuffer() {
        GL32.glClearColor((float) background.red(), (float) background.green(), (float) background.blue(), (float) background.alpha());
        GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
    }

}
