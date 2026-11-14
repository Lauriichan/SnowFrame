package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.netty.INettyContentType;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public abstract class NettyContentType<T> extends HttpContentType<T> implements INettyContentType<T> {

    public static final HttpContentType<ByteBuf> NETTY_BINARY = NettyBinaryContentType.NETTY_BINARY;

    public NettyContentType(String name, Class<? super T> valueType, String... acceptPattern) {
        super(name, valueType, acceptPattern);
    }

    @Override
    public NettyContentType<T> restrict(String... acceptPatterns) {
        return new RestrictingNettyContentType<>(this, acceptPatterns);
    }

    @Override
    public NettyContentType<T> disguise(String name) {
        return new DisguisedNettyContentType<>(this, name);
    }

    protected final Charset charsetFor(HttpHeaders headers) {
        String charsetName = headers.getArguments("Content-Type").get("charset");
        if (charsetName == null) {
            return charset();
        }
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException uce) {
            return charset();
        }
    }

    @Override
    public T read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        return readNetty(headers, Unpooled.wrappedBuffer(inputStream.array, inputStream.offset, inputStream.length));
    }

    @Override
    public void write(IHeaderArgs typeArgs, FastByteArrayOutputStream outputStream, T value) throws IOException {
        ObjectArrayList<Object> output = new ObjectArrayList<>(1);
        writeNetty(typeArgs, value, output);
        for (Object obj : output) {
            if (obj instanceof ByteBuf buf) {
                buf.readBytes(outputStream, buf.readableBytes());
            }
        }
    }

}
