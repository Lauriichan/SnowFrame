package me.lauriichan.snowframe;

import me.lauriichan.snowframe.extension.Extension;
import me.lauriichan.snowframe.lifecycle.Lifecycle;
import me.lauriichan.snowframe.lifecycle.LifecyclePhase.Stage;

@Extension
public class PulsarModule implements ISnowFrameModule {

    @Override
    public void registerLifecycle(Lifecycle<?> lifecycle) {
        lifecycle.startupChain().register("load", Stage.POST, (frame) -> {
            
        });
        
    }

}
