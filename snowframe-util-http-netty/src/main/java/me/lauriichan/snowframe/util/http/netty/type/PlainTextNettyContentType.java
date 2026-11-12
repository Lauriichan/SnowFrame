package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;

final class PlainTextNettyContentType extends NettyContentType<String> {

    public static final PlainTextNettyContentType PLAIN_TEXT = new PlainTextNettyContentType();

    private PlainTextNettyContentType() {
        super("text/plain", String.class, "text/*");
    }
    
    @Override
    public Charset charset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public String readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        return buffer.toString(StandardCharsets.UTF_8);
    }
    
    @Override
    public void writeNetty(IHeaderArgs typeArgs, String value, ObjectList<Object> output) throws IOException {
        typeArgs.set("charset", "utf-8");
        output.add(value);
    }

}
