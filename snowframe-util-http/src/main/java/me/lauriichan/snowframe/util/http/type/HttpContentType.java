package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;
import java.util.Objects;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.MultiFormData;

public abstract class HttpContentType<T> {

    public static final HttpContentType<byte[]> BINARY = BinaryContentType.BINARY;
    public static final HttpContentType<IJson<?>> JSON = JsonContentType.JSON;
    public static final HttpContentType<String> TEXT = PlainTextContentType.PLAIN_TEXT;
    public static final HttpContentType<Object2ObjectArrayMap<String, String>> URL_ENCODED = UrlEncodedContentType.URL_ENCODED;

    public static final HttpContentType<MultiFormData> multiFormData(HttpContentType<?>... accepts) {
        if (accepts == null || accepts.length == 0) {
            accepts = new HttpContentType[] {
                URL_ENCODED,
                JSON,
                TEXT,
                BINARY
            };
        }
        return new MultiFormDataContentType(accepts);
    }

    private final String name, accepts;
    private final String[] acceptPattern;
    private final Class<? super T> valueType;

    public HttpContentType(String name, Class<? super T> valueType, String... acceptPattern) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid type name");
        }
        if (acceptPattern == null || acceptPattern.length == 0) {
            throw new IllegalArgumentException("Invalid type accepts");
        }
        this.name = name;
        this.acceptPattern = acceptPattern;
        this.accepts = String.join("; ", acceptPattern);
        this.valueType = Objects.requireNonNull(valueType);
    }

    public final String name() {
        return name;
    }

    public final String accepts() {
        return accepts;
    }

    public final String[] acceptPattern() {
        return acceptPattern;
    }

    public final Class<? super T> valueType() {
        return valueType;
    }

    public final HttpContentType<T> restrict(String... acceptPatterns) {
        return new RestrictingHttpContentType<>(this, acceptPatterns);
    }

    public abstract T read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException;

    public abstract void write(FastByteArrayOutputStream outputStream, T value) throws IOException;

}
