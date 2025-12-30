package me.lauriichan.snowframe.config;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public abstract class ConfigMigrationExtension<C extends IConfig> implements IExtension {
    
    private final Class<C> targetType;
    private final int minVersion, targetVersion;
    
    public ConfigMigrationExtension(Class<C> targetType, int minVersion, int targetVersion) {
        this.targetType = targetType;
        this.minVersion = minVersion;
        this.targetVersion = targetVersion;
    }
    
    public final Class<C> targetType() {
        return targetType;
    }
    
    public final int minVersion() {
        return minVersion;
    }
    
    public final int targetVersion() {
        return targetVersion;
    }
    
    public abstract String description();
    
    public abstract void migrate(Configuration configuration) throws Throwable;

}
