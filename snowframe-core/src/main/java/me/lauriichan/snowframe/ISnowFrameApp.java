package me.lauriichan.snowframe;

import me.lauriichan.snowframe.extension.IConditionMap;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecycleBuilder;

public interface ISnowFrameApp<T extends ISnowFrameApp<T>> {

    SnowFrame<T> snowFrame();

    default void setupConditionMap(IConditionMap conditionMap) {}

    default void setupLifecycle(LifecycleBuilder<T> builder) {}

    void registerLifecycle(Lifecycle<T> lifecycle);

}
