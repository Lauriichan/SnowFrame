package me.lauriichan.snowframe.lifecycle;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import me.lauriichan.snowframe.ISnowFrameApp;

public final class LifecycleChainBuilder<T extends ISnowFrameApp<T>> {

    private final LifecycleBuilder<T> parent;

    private final String name;
    private final ReferenceArrayList<LifecyclePhase<T>> phases = new ReferenceArrayList<>();

    LifecycleChainBuilder(LifecycleBuilder<T> parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public LifecycleBuilder<T> next() {
        return parent;
    }

    public LifecycleChainBuilder<T> newPhase(String name, boolean failOnError) {
        Objects.requireNonNull(name, "Name can't be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name can't be blank");
        }
        name = Lifecycle.sanatizeName(name);
        if (hasPhase(name)) {
            throw new IllegalArgumentException("There is already a phase with name '%s'".formatted(name));
        }
        phases.add(new LifecyclePhase<>(name, failOnError));
        return this;
    }

    public LifecycleChainBuilder<T> newPhaseBefore(String before, String name, boolean failOnError) {
        Objects.requireNonNull(before, "Before can't be null");
        if (before.isBlank()) {
            throw new IllegalArgumentException("After can't be blank");
        }
        Objects.requireNonNull(name, "Name can't be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name can't be blank");
        }
        name = Lifecycle.sanatizeName(name);
        if (hasPhase(name)) {
            throw new IllegalArgumentException("There is already a phase with name '%s'".formatted(name));
        }
        before = Lifecycle.sanatizeName(before);
        int beforeIndex = 0;
        for (int index = 0; index < phases.size(); index++) {
            if (!phases.get(index).name().equals(before)) {
                continue;
            }
            beforeIndex = index;
            break;
        }
        phases.add(beforeIndex, new LifecyclePhase<>(name, failOnError));
        return this;
    }

    public LifecycleChainBuilder<T> newPhaseAfter(String after, String name, boolean failOnError) {
        Objects.requireNonNull(after, "After can't be null");
        if (after.isBlank()) {
            throw new IllegalArgumentException("After can't be blank");
        }
        Objects.requireNonNull(name, "Name can't be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name can't be blank");
        }
        name = Lifecycle.sanatizeName(name);
        if (hasPhase(name)) {
            throw new IllegalArgumentException("There is already a phase with name '%s'".formatted(name));
        }
        after = Lifecycle.sanatizeName(after);
        int afterIndex = 0;
        for (int index = 0; index < phases.size(); index++) {
            if (!phases.get(index).name().equals(after)) {
                continue;
            }
            afterIndex = index;
            break;
        }
        phases.add(afterIndex + 1, new LifecyclePhase<>(name, failOnError));
        return this;
    }

    public boolean has(String name) {
        Objects.requireNonNull(name);
        return hasPhase(Lifecycle.sanatizeName(name));
    }

    private boolean hasPhase(String name) {
        return phases.stream().anyMatch(phase -> phase.name().equals(name));
    }

    LifecycleChain<T> build() {
        return new LifecycleChain<>(name, ReferenceLists.unmodifiable(phases));
    }

}
