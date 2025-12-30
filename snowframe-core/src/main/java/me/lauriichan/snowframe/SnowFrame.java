package me.lauriichan.snowframe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.extension.IConditionMap;
import me.lauriichan.snowframe.extension.IExtension;
import me.lauriichan.snowframe.extension.IExtensionPool;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;
import me.lauriichan.snowframe.resource.ResourceManager;
import me.lauriichan.snowframe.resource.source.FileDataSource;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.util.instance.IInstanceInvoker;
import me.lauriichan.snowframe.util.instance.SharedInstances;
import me.lauriichan.snowframe.util.instance.SimpleInstanceInvoker;

public final class SnowFrame<T extends ISnowFrameApp<T>> {

    public static final String LIFECYCLE_CHAIN_STARTUP = "STARTUP";
    public static final String LIFECYCLE_CHAIN_SHUTDOWN = "SHUTDOWN";

    public static <T extends ISnowFrameApp<T>> SnowFrameBuilder<T> builder(T app) {
        return new SnowFrameBuilder<>(app);
    }

    private final T app;

    private final File jarFile;
    private final Path jarRoot;

    private final ISimpleLogger logger;

    private final Lifecycle<T> lifecycle;

    private final SimpleInstanceInvoker invoker;
    private final SharedInstances<IExtension> sharedExtensions;

    private final ResourceManager<T> resourceManager;

    private final ExtensionPoolImpl.ConditionMapImpl conditionMap = new ExtensionPoolImpl.ConditionMapImpl();

    private final IExtensionPool<ISnowFrameModule> modules;
    private final Reference2ReferenceMap<Class<? extends ISnowFrameModule>, ISnowFrameModule> moduleMap;

    SnowFrame(T app, File jarFile, ISimpleLogger logger, IInstanceInvoker baseInvoker) {
        this.app = app;

        this.logger = logger;
        this.invoker = new SimpleInstanceInvoker(baseInvoker);
        this.sharedExtensions = new SharedInstances<>(invoker);

        this.jarFile = jarFile(jarFile);
        this.jarRoot = jarRoot(this.jarFile);
        this.resourceManager = new ResourceManager<>(this);

        // Set default invoker values
        invoker.addExtra(this);
        invoker.addExtra(logger);
        invoker.addExtra(resourceManager);

        // Setup ResourceManager
        resourceManager.setDefault("jar");
        resourceManager.register("jar", jarRoot);
        resourceManager.register("fs", (_i, path) -> new FileDataSource(new File(path)));

        // Load SnowFrame modules
        this.modules = extension(ISnowFrameModule.class, true);
        Reference2ReferenceArrayMap<Class<? extends ISnowFrameModule>, ISnowFrameModule> moduleMap = new Reference2ReferenceArrayMap<>();
        modules.callInstances(module -> moduleMap.put(module.getClass(), module));
        this.moduleMap = Reference2ReferenceMaps.unmodifiable(moduleMap);

        // Build Lifecycle
        LifecycleBuilder<T> builder = Lifecycle.builder();
        builder.startupChain().newPhase("load", true).newPhase("ready", true);
        builder.shutdownChain().newPhase("shutdown", false);
        modules.callInstances(module -> module.setupLifecycle(builder));
        modules.callInstances(module -> module.setupLifecyclePostModule(builder));
        app.setupLifecycle(builder);
        this.lifecycle = builder.build(this);

        // Register Lifecycle executors
        lifecycle.startupChain().register("load", Stage.MAIN, (_i) -> {
            app.setupConditionMap(conditionMap);
            conditionMap.lock();
        });
        modules.callInstances(module -> module.registerLifecycle(lifecycle));
        app.registerLifecycle(lifecycle);
    }

    private File jarFile(File jarFile) {
        if (jarFile != null && jarFile.isFile()) {
            return jarFile;
        }
        try {
            return new File(app.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Couldn't retrieve jar file", e);
        }
    }

    private Path jarRoot(File jarFile) {
        URI uri = null;
        Path path = null;
        try {
            if (jarFile.isDirectory()) {
                path = jarFile.toPath();
            } else {
                uri = new URI(("jar:file:/" + jarFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20") + "!/").replace("//", "/"));
            }
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Failed to build resource uri", e);
        }
        if (uri != null) {
            try {
                FileSystems.getFileSystem(uri).close();
            } catch (final Exception exp) {
                if (!(exp instanceof NullPointerException || exp instanceof FileSystemNotFoundException)) {
                    logger.warning("Something went wrong while closing the file system", exp);
                }
            }
        }
        if (path == null) {
            try {
                path = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath("/");
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to resolve jar root!", e);
            }
        }
        return path;
    }

    /*
     * Resources
     */

    public IDataSource resource(String path) {
        return resourceManager.resolve(path);
    }

    public IDataSource externalResource(final String internalPath, final String externalPath) throws IOException {
        return externalResource(internalPath, externalPath, false);
    }

    public IDataSource externalResource(final String internalPath, final String externalPath, boolean forceSameContents)
        throws IOException {
        IDataSource internal = resourceManager.resolve(internalPath);
        IDataSource external = resourceManager.resolve(externalPath);
        transferOutside(internal, external, forceSameContents);
        return external;
    }

    private void transferOutside(IDataSource source, IDataSource target, boolean forceSameContents) throws IOException {
        if (!source.isContainer()) {
            try (OutputStream output = target.openWritableStream()) {
                try (InputStream input = source.openReadableStream()) {
                    input.transferTo(output);
                }
            }
            return;
        }
        IDataSource[] contents = source.getContents();
        for (IDataSource content : contents) {
            transferOutside(content, target.resolve(content.name()), forceSameContents);
        }
        if (forceSameContents) {
            IDataSource[] outside = target.getContents();
            if (contents.length != outside.length) {
                for (IDataSource tmp1 : outside) {
                    boolean found = false;
                    for (IDataSource tmp2 : contents) {
                        if (tmp1.name().equals(tmp2.name())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        tmp1.delete();
                    }
                }
            }
        }
    }

    /*
     * Extensions
     */

    public <E extends IExtension> IExtensionPool<E> extension(final Class<E> type, final boolean instantiate) {
        return new ExtensionPoolImpl<>(this, type, instantiate);
    }

    public <E extends IExtension> IExtensionPool<E> extension(final Class<? extends IExtension> extensionType, final Class<E> type,
        final boolean instantiate) {
        return new ExtensionPoolImpl<>(this, extensionType, type, instantiate);
    }

    /*
     * Modules
     */

    public <M extends ISnowFrameModule> M module(Class<M> type) {
        ISnowFrameModule module = moduleMap.get(type);
        if (module == null) {
            return null;
        }
        return type.cast(module);
    }

    /*
     * Getter
     */

    public T app() {
        return app;
    }

    public ISimpleLogger logger() {
        return logger;
    }
    
    public Lifecycle<T> lifecycle() {
        return lifecycle;
    }

    public SimpleInstanceInvoker invoker() {
        return invoker;
    }

    public SharedInstances<IExtension> sharedExtensions() {
        return sharedExtensions;
    }

    public IConditionMap conditionMap() {
        return conditionMap;
    }

    public ResourceManager<T> resourceManager() {
        return resourceManager;
    }

}
