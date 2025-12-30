package me.lauriichan.snowframe;

import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.io.IOManager;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;

@Extension
public class IOModule implements ISnowFrameModule {

    private IOManager manager;

    @Override
    public void setupLifecycle(LifecycleBuilder<?> builder) {
        builder.startupChain().newPhaseAfter("load", "io", false);
    }

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.startupChain().register("io", Stage.MAIN, (snowFrame) -> {
            manager = new IOManager(snowFrame);
        });
    }

    public IOManager manager() {
        return manager;
    }

}
