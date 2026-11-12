package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;

public final class UrlEncodedNettyContentType extends NettyContentType<HttpEncoded> {

    public static final UrlEncodedNettyContentType URL_ENCODED = new UrlEncodedNettyContentType();

    private UrlEncodedNettyContentType() {
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
    public HttpEncoded readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        return HttpEncoded.parse(buffer.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void writeNetty(IHeaderArgs typeArgs, HttpEncoded value, ObjectList<Object> output) throws IOException {
        typeArgs.set("charset", "utf-8");
        output.add(value.asEncodedString());
    }

}
