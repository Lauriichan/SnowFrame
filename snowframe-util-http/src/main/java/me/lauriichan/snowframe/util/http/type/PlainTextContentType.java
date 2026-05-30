package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.snowframe.util.http.HttpHeaders;

final class PlainTextContentType extends HttpContentType<String> {

    public static final PlainTextContentType PLAIN_TEXT = new PlainTextContentType();

    private PlainTextContentType() {
        super("text/plain", "text/*", String.class);
    }

    @Override
    public String read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public void write(FastByteArrayOutputStream outputStream, String value) throws IOException {
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
    }

}
