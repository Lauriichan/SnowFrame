package me.lauriichan.snowframe.data;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface IMultiDataExtension<K, E, T, D extends IFileDataExtension<T>> extends IExtension {
    
    K getDataKey(E element);

    String path(E element);
    
    D create();
    
}
