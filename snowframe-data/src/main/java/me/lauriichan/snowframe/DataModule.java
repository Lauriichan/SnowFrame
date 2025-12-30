package me.lauriichan.snowframe;

import me.lauriichan.snowframe.data.DataManager;
import me.lauriichan.snowframe.data.DataMigrator;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;
import me.lauriichan.snowframe.lifecycle.LifecycleChainBuilder;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;

@Extension
public class DataModule implements ISnowFrameModule {

    private DataManager manager;
    private DataMigrator migrator;
    
    @Override
    public void setupLifecyclePostModule(LifecycleBuilder<?> builder) {
        LifecycleChainBuilder<?> chainBuilder = builder.startupChain();
        if (chainBuilder.has("config")) {
            chainBuilder.newPhaseAfter("config", "data", false);
        } else {
            chainBuilder.newPhaseAfter("load", "data", false);
        }
    }

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.startupChain().register("data", Stage.MAIN, (snowFrame) -> {
            migrator = new DataMigrator(snowFrame);
            manager = new DataManager(snowFrame);
        });
    }

    public DataManager manager() {
        return manager;
    }

    public DataMigrator migrator() {
        return migrator;
    }

}
