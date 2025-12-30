package me.lauriichan.snowframe.config;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface IMultiConfigExtension<K, T, C extends IConfig> extends IExtension {
    
    Class<C> type();
    
    K getConfigKey(T element);

    String path(T element);
    
    C create();
    
    default void onLoad(ISimpleLogger logger) {}
    
    default void onSave(ISimpleLogger logger) {}
    
}
