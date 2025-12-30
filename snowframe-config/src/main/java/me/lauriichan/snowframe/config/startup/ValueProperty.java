package me.lauriichan.snowframe.config.startup;

import java.util.function.Consumer;

import me.lauriichan.snowframe.config.Configuration;

public final class ValueProperty<T> extends Property {

    private final IPropertyIO<T> io;
    private final Consumer<T> onLoad;

    private final T defaultValue;
    private volatile T value;

    public ValueProperty(String path, String description, IPropertyIO<T> io, T defaultValue) {
        this(path, description, io, defaultValue, null);
    }

    public ValueProperty(String path, String description, IPropertyIO<T> io, T defaultValue, Consumer<T> onLoad) {
        super(path, description);
        this.io = io;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.onLoad = onLoad;
    }

    public final T value() {
        return value;
    }

    public final T defaultValue() {
        return defaultValue;
    }

    public final T get() {
        return value == null ? defaultValue : value;
    }

    public final void load(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, false);
        if (section == null) {
            return;
        }
        T value = io.read(section, "value");
        this.value = value == null ? defaultValue : value;
        if (onLoad != null) {
            onLoad.accept(value);
        }
    }

    public final void save(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, true);
        section.set("description", description);
        io.write(section, "value", value);
    }

}
