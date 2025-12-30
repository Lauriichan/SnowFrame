package me.lauriichan.snowframe;

import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryUtil;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.logger.util.StringUtil;

public class GLFWLoggerCallback implements GLFWErrorCallbackI {

    private final Map<Integer, String> errorCodes = APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null,
        GLFW.class);

    protected final ISimpleLogger logger;

    public GLFWLoggerCallback(ISimpleLogger logger) {
        this.logger = logger;
    }

    @Override
    public final void invoke(int error, long description) {
        print(errorCodes.get(error), error, MemoryUtil.memUTF8(description));
    }

    public void print(String error, int errorCode, String description) {
        logger.error(new RuntimeException(StringUtil.format("{0} ({1}) error: {2}", new Object[] {
            error,
            errorCode,
            description
        })));
    }

}
