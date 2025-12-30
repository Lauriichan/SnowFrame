package me.lauriichan.snowframe.lifecycle;

import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SnowFrame;

public final class Lifecycle<T extends ISnowFrameApp<T>> {

    static final String sanatizeName(String name) {
        return name.toUpperCase().replaceAll("\\s+", "_");
    }

    public static <E extends ISnowFrameApp<E>> LifecycleBuilder<E> builder() {
        return new LifecycleBuilder<>();
    }

    private final SnowFrame<T> snowFrame;
    private final Object2ObjectMap<String, LifecycleChain<T>> chains;

    Lifecycle(SnowFrame<T> snowFrame, Object2ObjectMap<String, LifecycleChain<T>> chains) {
        this.snowFrame = snowFrame;
        this.chains = chains;
    }

    public SnowFrame<T> snowFrame() {
        return snowFrame;
    }

    /**
     * Executes a lifecycle chain
     * 
     * @param  chainName the name of the chain
     * 
     * @return           {@code true} if the chain was successfully executed
     *                       otherwise {@code false}
     */
    public boolean execute(String chainName) {
        LifecycleChain<T> chain = chainOrThrow(chainName);
        chainName = chain.name();
        snowFrame.logger().debug("Executing lifecycle chain '{0}'...", chainName);
        for (LifecyclePhase<T> phase : chain.phases()) {
            if (!phase.execute(chainName, snowFrame)) {
                snowFrame.logger().debug("Failed to execute lifecycle chain '{0}'!", chainName);
                return false;
            }
        }
        snowFrame.logger().debug("Successfully executed lifecycle chain '{0}'", chainName);
        return true;
    }

    public LifecycleChain<T> startupChain() {
        return chainOrThrow(SnowFrame.LIFECYCLE_CHAIN_STARTUP);
    }

    public LifecycleChain<T> shutdownChain() {
        return chainOrThrow(SnowFrame.LIFECYCLE_CHAIN_SHUTDOWN);
    }

    public Optional<LifecycleChain<T>> chain(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(chains.get(sanatizeName(name)));
    }

    public LifecycleChain<T> chainOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid phase name '%s'".formatted(name));
        }
        LifecycleChain<T> phase = chains.get(sanatizeName(name));
        if (phase == null) {
            throw new IllegalStateException("Unknown phase '%s'".formatted(name));
        }
        return phase;
    }

}
