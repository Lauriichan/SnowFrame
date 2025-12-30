package me.lauriichan.snowframe.config;

public interface IConfigWrapper<T extends IConfig> {
    
    Class<T> configType();
    
    default int[] reload() {
        return reload(false, false);
    }
    
    int[] reload(boolean forceReload, boolean wipeAfterLoad);
    
    default int[] save() {
        return save(false);
    }
    
    int[] save(boolean forceSave);

}
