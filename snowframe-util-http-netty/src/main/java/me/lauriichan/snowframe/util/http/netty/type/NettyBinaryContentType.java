package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;

final class NettyBinaryContentType extends NettyContentType<ByteBuf> {

    public static final NettyBinaryContentType NETTY_BINARY = new NettyBinaryContentType();

    public NettyBinaryContentType() {
        super("application/octet-stream", ByteBuf.class, "*/*");
        if (NETTY_BINARY != null) {
            throw new UnsupportedOperationException("Singleton");
        }
    }

    @Override
    public ByteBuf readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        return buffer.asByteBuf();
    }

    @Override
    public void writeNetty(IHeaderArgs typeArgs, ByteBuf value, ObjectList<Object> output) throws IOException {
        output.add(value);
    }

}
