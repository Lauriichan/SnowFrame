package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.json.io.JsonSyntaxException;
import me.lauriichan.laylib.json.io.JsonWriter;
import me.lauriichan.snowframe.util.http.HttpHeaders;

final class JsonContentType extends HttpContentType<IJson<?>> {

    public static final JsonContentType JSON = new JsonContentType();

    private final JsonWriter writer = new JsonWriter().setPretty(false);

    private JsonContentType() {
        super("application/json", IJson.class, "application/vnd.github.v3+json", "application/json");
        if (JSON != null) {
            throw new UnsupportedOperationException("Singleton");
        }
    }

    @Override
    public IJson<?> read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        try {
            return JsonParser.fromStream(inputStream);
        } catch (IllegalStateException | JsonSyntaxException e) {
            throw new IOException("Failed to read json", e);
        }
    }

    @Override
    public void write(FastByteArrayOutputStream outputStream, IJson<?> value) throws IOException {
        writer.toStream(value, outputStream);
    }

}
