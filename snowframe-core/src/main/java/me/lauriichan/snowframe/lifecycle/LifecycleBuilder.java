package me.lauriichan.snowframe.lifecycle;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SnowFrame;

public final class LifecycleBuilder<T extends ISnowFrameApp<T>> {

    // More than 4 chains are not really something expected
    private final Object2ObjectArrayMap<String, LifecycleChainBuilder<T>> chains = new Object2ObjectArrayMap<>(4);

    LifecycleBuilder() {}

    public LifecycleChainBuilder<T> startupChain() {
        return chain(SnowFrame.LIFECYCLE_CHAIN_STARTUP);
    }

    public LifecycleChainBuilder<T> shutdownChain() {
        return chain(SnowFrame.LIFECYCLE_CHAIN_SHUTDOWN);
    }

    public LifecycleChainBuilder<T> chain(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid chain name '%s'".formatted(name));
        }
        name = Lifecycle.sanatizeName(name);
        LifecycleChainBuilder<T> builder = chains.get(name);
        if (builder != null) {
            return builder;
        }
        chains.put(name, builder = new LifecycleChainBuilder<>(this, name));
        return builder;
    }

    public Lifecycle<T> build(SnowFrame<T> snowFrame) {
        Object2ObjectArrayMap<String, LifecycleChain<T>> chains = new Object2ObjectArrayMap<>(this.chains.size());
        this.chains.values().forEach(builder -> chains.put(builder.name(), builder.build()));
        return new Lifecycle<>(snowFrame, Object2ObjectMaps.unmodifiable(chains));
    }

}
