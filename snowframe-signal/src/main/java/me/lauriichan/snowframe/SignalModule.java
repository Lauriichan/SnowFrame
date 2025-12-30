package me.lauriichan.snowframe;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.extension.IExtensionPool;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;
import me.lauriichan.snowframe.signal.ISignalHandler;
import me.lauriichan.snowframe.signal.SignalManager;

@Extension
public final class SignalModule implements ISnowFrameModule {

    private final ISimpleLogger logger;
    private final SignalManager signalManager;

    public SignalModule(ISimpleLogger logger, SnowFrame<?> frame) {
        this.logger = logger;
        this.signalManager = new SignalManager(logger);
    }

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.startupChain().register("ready", Stage.POST, (snowFrame) -> {
            final IExtensionPool<ISignalHandler> pool = snowFrame.extension(ISignalHandler.class, true);
            pool.callInstances(extension -> {
                try {
                    signalManager.register(extension);
                } catch (UnsupportedOperationException exp) {
                    logger.error("Failed to register signal handler extension: " + extension.getClass().getName(), exp);
                }
            });
        });
    }

    public final SignalManager signalManager() {
        return signalManager;
    }

}
