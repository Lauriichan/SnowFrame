package me.lauriichan.snowframe.lifecycle;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.util.Enum2ObjectMap;

public final class LifecyclePhase<T extends ISnowFrameApp<T>> {

    @FunctionalInterface
    public static interface IStageExecutor<T extends ISnowFrameApp<T>> {

        void execute(SnowFrame<T> snowFrame) throws Exception;

    }

    public static enum Stage {
        PRE,
        MAIN,
        POST;
    }

    private static final Stage[] STAGES = Stage.values();

    private final String name;
    private final boolean failOnError;
    private final Enum2ObjectMap<Stage, ReferenceArrayList<IStageExecutor<T>>> listeners = new Enum2ObjectMap<>(Stage.class);

    LifecyclePhase(String name, boolean failOnError) {
        this.name = name;
        this.failOnError = failOnError;
    }

    public String name() {
        return name;
    }

    public LifecyclePhase<T> register(Stage stage, IStageExecutor<T> executor) {
        Objects.requireNonNull(stage, "Stage can't be null");
        Objects.requireNonNull(executor, "IStageExecutor can't be null");
        ReferenceArrayList<IStageExecutor<T>> list = listeners.get(stage);
        if (list == null) {
            listeners.put(stage, list = new ReferenceArrayList<>());
        } else if (list.contains(executor)) {
            return this;
        }
        list.add(executor);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LifecyclePhase<?> phase)) {
            return false;
        }
        return phase.name.equals(name);
    }

    final boolean execute(String chainName, SnowFrame<T> snowFrame) {
        if (listeners.isEmpty()) {
            snowFrame.logger().track("Skipping phase '{0}' in chain '{1}'.", name, chainName);
            return true;
        }
        ISimpleLogger logger = snowFrame.logger();
        for (Stage stage : STAGES) {
            ReferenceList<IStageExecutor<T>> executors = listeners.get(stage);
            if (executors == null || executors.isEmpty()) {
                logger.track("Skipping stage '{0}' of phase '{1}' in chain '{2}'.", stage, name, chainName);
                continue;
            }
            logger.track("Executing stage '{0}' of phase '{1}' in chain '{2}'...", stage, name, chainName);
            try {
                for (IStageExecutor<T> executor : executors) {
                    executor.execute(snowFrame);
                }
            } catch (Exception e) {
                logger.error("Failed to fully execute stage '{0}' of phase '{1}' in chain '{2}'!", e, stage, name, chainName);
                if (failOnError) {
                    return false;
                }
            }
        }
        return true;
    }

}
