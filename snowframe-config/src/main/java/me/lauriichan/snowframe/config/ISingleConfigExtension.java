package me.lauriichan.snowframe.config;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface ISingleConfigExtension extends IConfig, IExtension {

    String path();

}
