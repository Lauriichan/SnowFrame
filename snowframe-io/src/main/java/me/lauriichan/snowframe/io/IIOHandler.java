package me.lauriichan.snowframe.io;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface IIOHandler<B, V> extends IExtension {
    
    Class<B> bufferType();
    
    Class<V> valueType();

}
