package me.lauriichan.snowframe;

import me.lauriichan.snowframe.config.ConfigManager;
import me.lauriichan.snowframe.config.ConfigMigrator;
import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;

@Extension
public class ConfigModule implements ISnowFrameModule {

    private ConfigManager manager;
    private ConfigMigrator migrator;

    @Override
    public void setupLifecycle(LifecycleBuilder<?> builder) {
        builder.startupChain().newPhaseAfter("io", "config", false).newPhaseAfter("config", "reload_config", false);
    }

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.startupChain().register("config", Stage.MAIN, (snowFrame) -> {
            migrator = new ConfigMigrator(snowFrame);
            manager = new ConfigManager(snowFrame);
        }).register("reload_config", Stage.MAIN, (snowFrame) -> {
            manager.reload();
        });
    }

    public ConfigManager manager() {
        return manager;
    }

    public ConfigMigrator migrator() {
        return migrator;
    }

}
