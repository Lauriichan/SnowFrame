package me.lauriichan.snowframe;

import java.io.File;
import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.util.instance.IInstanceInvoker;
import me.lauriichan.snowframe.util.logger.SysOutSimpleLogger;

public class SnowFrameBuilder<T extends ISnowFrameApp<T>> {

    private final T app;

    private File jarFile;
    private ISimpleLogger logger = SysOutSimpleLogger.INSTANCE;
    private IInstanceInvoker invoker = IInstanceInvoker.DEFAULT;

    SnowFrameBuilder(T app) {
        this.app = Objects.requireNonNull(app);
    }

    public SnowFrameBuilder<T> jarFile(File jarFile) {
        this.jarFile = jarFile;
        return this;
    }

    public SnowFrameBuilder<T> logger(ISimpleLogger logger) {
        this.logger = Objects.requireNonNull(logger);
        return this;
    }

    public SnowFrameBuilder<T> invoker(IInstanceInvoker invoker) {
        this.invoker = Objects.requireNonNull(invoker);
        return this;
    }

    public SnowFrame<T> build() {
        return new SnowFrame<>(app, jarFile, logger, invoker);
    }

}
