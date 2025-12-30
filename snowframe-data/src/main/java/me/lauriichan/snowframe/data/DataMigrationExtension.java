package me.lauriichan.snowframe.data;

import me.lauriichan.snowframe.data.IDataHandler.Wrapper;
import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public abstract class DataMigrationExtension<T, D extends IDataExtension<T>> implements IExtension {
    
    private final Class<D> targetType;
    private final int minVersion, targetVersion;
    
    public DataMigrationExtension(Class<D> targetType, int minVersion, int targetVersion) {
        this.targetType = targetType;
        this.minVersion = minVersion;
        this.targetVersion = targetVersion;
    }
    
    public final Class<D> targetType() {
        return targetType;
    }
    
    public final int minVersion() {
        return minVersion;
    }
    
    public final int targetVersion() {
        return targetVersion;
    }
    
    public abstract String description();
    
    public abstract void migrate(Wrapper<T> value) throws Throwable;

}
