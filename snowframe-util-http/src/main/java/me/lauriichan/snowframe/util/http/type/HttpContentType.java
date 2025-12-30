package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;
import java.util.Objects;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.laylib.json.IJson;

public abstract class HttpContentType<T> {

    public static final HttpContentType<byte[]> BINARY = BinaryContentType.BINARY;
    public static final HttpContentType<IJson<?>> JSON = JsonContentType.JSON;
    public static final HttpContentType<Object2ObjectArrayMap<String, String>> URL_ENCODED = UrlEncodedContentType.URL_ENCODED;

    private final String name, accepts;
    private final Class<? super T> valueType;

    public HttpContentType(String name, String accepts, Class<? super T> valueType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid type name");
        }
        if (accepts == null || accepts.isBlank()) {
            throw new IllegalArgumentException("Invalid type accepts");
        }
        this.name = name;
        this.accepts = accepts;
        this.valueType = Objects.requireNonNull(valueType);
    }

    public final String name() {
        return name;
    }

    public final String accepts() {
        return accepts;
    }

    public final Class<? super T> valueType() {
        return valueType;
    }

    public abstract T read(FastByteArrayInputStream inputStream) throws IOException;

    public abstract void write(FastByteArrayOutputStream outputStream, T value) throws IOException;

}
