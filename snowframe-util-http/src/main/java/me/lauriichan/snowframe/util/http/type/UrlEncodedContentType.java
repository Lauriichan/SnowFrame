package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;
import me.lauriichan.snowframe.util.http.data.HttpEncoded.MapType;

public final class UrlEncodedContentType extends HttpContentType<HttpEncoded> {

    public static final UrlEncodedContentType URL_ENCODED = new UrlEncodedContentType();

    private UrlEncodedContentType() {
        super("application/x-www-form-urlencoded", HttpEncoded.class, "application/x-www-form-urlencoded");
        if (URL_ENCODED != null) {
            throw new UnsupportedOperationException("Singleton");
        }
    }
    
    @Override
    public Charset charset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public HttpEncoded read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        return HttpEncoded.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), MapType.IMMUTABLE);
    }

    @Override
    public void write(IHeaderArgs typeArgs, FastByteArrayOutputStream outputStream, HttpEncoded value) throws IOException {
        typeArgs.set("charset", "utf-8");
        try (WritableByteChannel channel = Channels.newChannel(outputStream)) {
            channel.write(StandardCharsets.UTF_8.encode(value.asEncodedString()));
        }
    }

}
