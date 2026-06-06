package me.lauriichan.snowframe.util.http.data;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class MultiFormData {

    public static record FormData<T>(String key, String fileName, String providedType, HttpContentType<T> type, T value) {}

    private final Object2ObjectOpenHashMap<String, FormData<?>> fields = new Object2ObjectOpenHashMap<>();

    public <T> void put(String key, HttpContentType<T> type, T value) {
        fields.put(key, new FormData<>(key, null, type.name(), type, value));
    }

    public <T> void put(String key, String fileName, HttpContentType<T> type, T value) {
        fields.put(key, new FormData<>(key, fileName, type.name(), type, value));
    }

    public <T> void readAndPut(String key, String fileName, String providedType, HttpContentType<T> type, HttpHeaders headers,
        FastByteArrayInputStream input) throws IOException {
        fields.put(key, new FormData<>(key, fileName, providedType, type, type.read(headers, input)));
    }

    public ObjectSet<String> keys() {
        return fields.keySet();
    }

    public ObjectCollection<FormData<?>> fields() {
        return fields.values();
    }

    public boolean has(String key) {
        return fields.containsKey(key);
    }

    public boolean has(String key, HttpContentType<?> contentType) {
        FormData<?> data = fields.get(key);
        return data != null && data.type() == contentType;
    }

    public boolean has(String key, Class<?> type) {
        FormData<?> data = fields.get(key);
        if (data == null) {
            return false;
        }
        return type.isInstance(data.value());
    }

    public <T> T get(String key, HttpContentType<T> contentType) {
        return getOrDefault(key, contentType, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, HttpContentType<T> contentType, T fallback) {
        FormData<?> data = fields.get(key);
        if (data == null || data.type() != contentType) {
            return fallback;
        }
        return (T) data.value();
    }

    public <T> T get(String key, Class<T> type) {
        return getOrDefault(key, type, null);
    }

    public <T> T getOrDefault(String key, Class<T> type, T fallback) {
        FormData<?> data = fields.get(key);
        if (data == null || !type.isInstance(data.value())) {
            return fallback;
        }
        return type.cast(data.value());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MultiFormData[");
        builder.append("\n");
        ObjectIterator<FormData<?>> iterator = fields().iterator();
        while (iterator.hasNext()) {
            FormData<?> data = iterator.next();
            builder.append("\tData[");
            builder.append("\n\t");
            builder.append("name=").append(data.key());
            builder.append("\n\t");
            builder.append("filename=");
            if (data.fileName() == null || data.fileName().isBlank()) {
                builder.append("null");
            } else {
                builder.append(data.fileName());
            }
            builder.append("\n\t");
            builder.append("data='");
            builder.append(data.value());
            builder.append("'\n\t]");
            if (iterator.hasNext()) {
                builder.append(",\n");
            }
        }
        return builder.append("\n]").toString();
    }

}
