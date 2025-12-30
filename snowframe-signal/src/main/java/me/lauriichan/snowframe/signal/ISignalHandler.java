package me.lauriichan.snowframe.signal;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface ISignalHandler extends IExtension {
    
    default SignalContainer newContainer() {
        throw new UnsupportedOperationException();
    }

}
