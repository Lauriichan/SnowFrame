package me.lauriichan.snowframe;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;

/**
 * This is not to be implemented outside of SnowFrame
 */
@ExtensionPoint
public interface ISnowFrameModule extends IExtension {

    default void setupLifecycle(LifecycleBuilder<?> builder) {}
    
    default void setupLifecyclePostModule(LifecycleBuilder<?> builder) {}

    void registerLifecycle(Lifecycle<?> lifecycle);

}
