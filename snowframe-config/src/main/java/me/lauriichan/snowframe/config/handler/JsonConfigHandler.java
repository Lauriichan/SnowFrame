package me.lauriichan.snowframe.config.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.json.io.JsonWriter;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.snowframe.config.Configuration;
import me.lauriichan.snowframe.config.IConfigHandler;
import me.lauriichan.snowframe.io.IOManager;
import me.lauriichan.snowframe.io.serialization.SerializationException;
import me.lauriichan.snowframe.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.snowframe.resource.source.IDataSource;

public final class JsonConfigHandler implements IConfigHandler {
    
    public static final JsonWriter WRITER = new JsonWriter().setPretty(true).setSpaces(true).setIndent(4);

    public static final JsonConfigHandler JSON = new JsonConfigHandler();
    
    public static final String KEY_SERIALIZE_TYPE = "type";
    public static final String KEY_SERIALIZE_DATA = "data";

    @Override
    public void load(final IOManager ioManager, final Configuration configuration, final IDataSource source, boolean onlyRaw) throws Exception {
        IJson<?> element;
        try (BufferedReader reader = source.openReader()) {
            element = JsonParser.fromReader(reader);
        }
        if (!element.isObject()) {
            throw new IllegalStateException("Config source doesn't contain a JsonObject");
        }
        loadToConfig(ioManager, element.asJsonObject(), configuration, onlyRaw);
    }

    private void loadToConfig(final IOManager ioManager, final JsonObject object, final Configuration configuration, boolean onlyRaw) throws SerializationException {
        for (final String key : object.keySet()) {
            final IJson<?> element = object.get(key);
            if (element.isNull()) {
                continue;
            }
            if (element.isObject()) {
                deserialize(ioManager, configuration, key, element.asJsonObject(), onlyRaw);
                continue;
            }
            if (element.isArray()) {
                configuration.set(key, deserialize(element.asJsonArray()));
                continue;
            }
            configuration.set(key, deserialize(element));
        }
    }

    @SuppressWarnings("unchecked")
    private void deserialize(final IOManager ioManager, Configuration configuration, String key, JsonObject object, boolean onlyRaw) throws SerializationException {
        String type = object.getAsString(KEY_SERIALIZE_TYPE);
        if (onlyRaw || type == null) {
            loadToConfig(ioManager, object, configuration.getConfiguration(key, true), onlyRaw);
            return;
        }
        Class<?> valueType = ClassUtil.findClass(type);
        if (valueType == null) {
            throw new SerializationException("Can't read unknown serialized object of type '" + type + "', reason: Type is unknown");
        }
        IJson<?> json = object.get(KEY_SERIALIZE_DATA);
        configuration.set(key, ioManager.deserialize(JsonSerializationHandler.class, json == null ? object : json, valueType));
    }

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    private List deserialize(final JsonArray array) {
        final ObjectArrayList list = new ObjectArrayList();
        for (final IJson<?> arrayElement : array) {
            if (arrayElement.isObject() || arrayElement.isNull()) {
                continue;
            }
            if (arrayElement.isArray()) {
                list.add(deserialize(arrayElement.asJsonArray()));
                continue;
            }
            list.add(deserialize(arrayElement));
        }
        return list;
    }

    private Object deserialize(final IJson<?> primitive) {
        if (primitive.isBoolean()) {
            return primitive.asJsonBoolean().value();
        }
        if (primitive.isNumber()) {
            return primitive.asJsonNumber().value();
        }
        return primitive.asJsonString().value();
    }

    @Override
    public void save(final IOManager ioManager, final Configuration configuration, final IDataSource source) throws Exception {
        final JsonObject root = new JsonObject();
        saveToObject(ioManager, root, configuration);
        try (BufferedWriter writer = source.openWriter()) {
            WRITER.toWriter(root, writer);
        }
    }

    private void saveToObject(final IOManager ioManager, final JsonObject object, final Configuration configuration) throws SerializationException {
        IJson<?> json;
        for (final String key : configuration.keySet()) {
            if (configuration.isConfiguration(key)) {
                final JsonObject child = new JsonObject();
                saveToObject(ioManager, child, configuration.getConfiguration(key));
                object.put(key, child);
                continue;
            }
            json = serialize(ioManager, configuration.get(key));
            if (json == null) {
                continue;
            }
            object.put(key, json);
        }
    }

    @SuppressWarnings("unchecked")
    private IJson<?> serialize(final IOManager ioManager, final Object object) throws SerializationException {
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            final JsonArray array = new JsonArray();
            for (final Object elem : list) {
                array.add(serialize(ioManager, elem));
            }
            return array;
        }
        if (object != null && object.getClass().isEnum()) {
            return IJson.of(object.toString());
        }
        try {
            return IJson.of(object);
        } catch (IllegalArgumentException e) {
        }
        IJson<?> json = (IJson<?>) ioManager.serialize(JsonSerializationHandler.class, object);
        if (json == null) {
            return null;
        }
        JsonObject jsonObject;
        if (json.isObject()) {
            jsonObject = json.asJsonObject();
        } else {
            jsonObject = new JsonObject();
            jsonObject.put(KEY_SERIALIZE_DATA, json);
        }
        jsonObject.put(KEY_SERIALIZE_TYPE, object.getClass().getName());
        return jsonObject;
    }

}
