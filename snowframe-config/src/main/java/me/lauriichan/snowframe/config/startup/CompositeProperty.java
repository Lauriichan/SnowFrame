package me.lauriichan.snowframe.config.startup;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.snowframe.config.Configuration;

public final class CompositeProperty extends Property {

    private final Object2ObjectArrayMap<String, ValueProperty<?>> properties = new Object2ObjectArrayMap<>();

    private final Consumer<CompositeProperty> onLoad;

    public CompositeProperty(String path, String description) {
        this(path, description, null);
    }

    public CompositeProperty(String path, String description, Consumer<CompositeProperty> onLoad) {
        super(path, description);
        this.onLoad = onLoad;
    }

    public ValueProperty<?> add(ValueProperty<?> property) {
        if (properties.containsKey(property.path())) {
            throw new IllegalArgumentException("Duplicate path '" + property.path() + "'!");
        }
        properties.put(property.path(), property);
        return property;
    }

    public final void load(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, false);
        if (section == null || properties.isEmpty()) {
            return;
        }
        Configuration composite = section.getConfiguration("properties", false);
        if (composite == null) {
            return;
        }
        for (ValueProperty<?> property : properties.values()) {
            property.load(composite);
        }
        if (onLoad != null) {
            onLoad.accept(this);
        }
    }

    public final void save(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, true);
        section.set("description", description);
        if (properties.isEmpty()) {
            return;
        }
        Configuration composite = section.getConfiguration("properties", true);
        for (ValueProperty<?> property : properties.values()) {
            property.save(composite);
        }
    }

}
