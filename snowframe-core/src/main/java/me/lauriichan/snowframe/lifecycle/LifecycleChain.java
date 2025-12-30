package me.lauriichan.snowframe.lifecycle;

import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import me.lauriichan.snowframe.ISnowFrameApp;

public final class LifecycleChain<T extends ISnowFrameApp<T>> {

    private final String name;
    private final Object2ObjectMap<String, LifecyclePhase<T>> phaseMap;
    private final ReferenceList<LifecyclePhase<T>> phases;

    LifecycleChain(String name, ReferenceList<LifecyclePhase<T>> phases) {
        this.name = name;
        this.phases = phases;
        Object2ObjectOpenHashMap<String, LifecyclePhase<T>> name2phase = new Object2ObjectOpenHashMap<>(phases.size());
        phases.forEach(phase -> name2phase.put(phase.name(), phase));
        this.phaseMap = Object2ObjectMaps.unmodifiable(name2phase);
    }

    public String name() {
        return name;
    }

    public ReferenceList<LifecyclePhase<T>> phases() {
        return phases;
    }

    public Optional<LifecyclePhase<T>> phase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(phaseMap.get(Lifecycle.sanatizeName(name)));
    }

    public LifecyclePhase<T> phaseOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid phase name '%s'".formatted(name));
        }
        LifecyclePhase<T> phase = phaseMap.get(Lifecycle.sanatizeName(name));
        if (phase == null) {
            throw new IllegalStateException("Unknown phase '%s'".formatted(name));
        }
        return phase;
    }

    public LifecycleChain<T> register(String name, LifecyclePhase.Stage stage, LifecyclePhase.IStageExecutor<T> executor) {
        phaseOrThrow(name).register(stage, executor);
        return this;
    }

}
