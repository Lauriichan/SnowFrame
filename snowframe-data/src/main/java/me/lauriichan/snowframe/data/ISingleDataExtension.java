package me.lauriichan.snowframe.data;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface ISingleDataExtension<T> extends IFileDataExtension<T>, IExtension {
    
    String path();

}
