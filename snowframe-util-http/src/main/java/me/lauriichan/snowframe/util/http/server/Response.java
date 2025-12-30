package me.lauriichan.snowframe.util.http.server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.net.httpserver.Headers;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class Response {
    
    public static Response of(HttpCode code) {
        return new Response(code);
    }

    public static record Content<T>(HttpContentType<T> type, T value) {
        public Content(HttpContentType<T> type, T value) {
            this.type = Objects.requireNonNull(type);
            this.value = Objects.requireNonNull(value);
        }
        
        public void write(FastByteArrayOutputStream outputStream) throws IOException {
            type.write(outputStream, value);
        }
    }
    
    private static final Object2ObjectFunction<String, ObjectArrayList<String>> CREATOR = (_i) -> new ObjectArrayList<>();

    private final HttpCode code;
    private final Object2ObjectArrayMap<String, ObjectArrayList<String>> headers = new Object2ObjectArrayMap<>();
    
    private Content<?> content;

    private Response(HttpCode code) {
        this.code = code;
    }

    public final HttpCode code() {
        return code;
    }
    
    public final <T> Response content(HttpContentType<T> type, T value) {
        this.content = new Content<>(type, value);
        return this;
    }
    
    public final Content<?> content() {
        return content;
    }

    public final Response headerSet(String key, String value) {
        ObjectArrayList<String> list = headers.computeIfAbsent(key, CREATOR);
        if (!list.isEmpty()) {
            list.clear();
        }
        list.add(value);
        return this;
    }

    public final Response headerSet(String key, String... values) {
        ObjectArrayList<String> list = headers.computeIfAbsent(key, CREATOR);
        if (!list.isEmpty()) {
            list.clear();
        }
        Collections.addAll(list, values);
        return this;
    }

    public final Response headerAdd(String key, String value) {
        headers.computeIfAbsent(key, CREATOR).add(value);
        return this;
    }

    public final Response headerAdd(String key, String... values) {
        Collections.addAll(headers.computeIfAbsent(key, CREATOR), values);
        return this;
    }

    public final Response headerRemove(String key, String value) {
        ObjectArrayList<String> list = headers.get(key);
        if (list == null) {
            return this;
        }
        if (list.remove(value) && list.isEmpty()) {
            headers.remove(key);
        }
        return this;
    }

    public final Response headerRemove(String key, String... values) {
        ObjectArrayList<String> list = headers.get(key);
        if (list == null) {
            return this;
        }
        for (String value : values) {
            if (list.remove(value) && list.isEmpty()) {
                headers.remove(key);
                break;
            }
        }
        return this;
    }

    public final Response headerDelete(String key) {
        headers.remove(key);
        return this;
    }

    public final Response headerClear() {
        headers.clear();
        return this;
    }

    public final void loadFrom(Headers headers) {
        headers.clear();
        for (Map.Entry<String, List<String>> entries : headers.entrySet()) {
            headers.put(entries.getKey(), new ObjectArrayList<>(entries.getValue()));
        }
    }

    public final void saveTo(Headers headers) {
        headers.putAll(this.headers);
    }

}
