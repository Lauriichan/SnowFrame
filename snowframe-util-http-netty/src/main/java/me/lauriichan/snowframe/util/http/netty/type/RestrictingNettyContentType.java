package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;

final class RestrictingNettyContentType<T> extends NettyContentType<T> {

    private final NettyContentType<T> delegate;

    RestrictingNettyContentType(NettyContentType<T> delegate, String[] acceptPattern) {
        super(delegate.name(), delegate.valueType(), acceptPattern);
        this.delegate = delegate;
    }
    
    @Override
    public Charset charset() {
        return delegate.charset();
    }
    
    @Override
    public T readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        return delegate.readNetty(headers, buffer);
    }
    
    @Override
    public void writeNetty(IHeaderArgs typeArgs, T value, ObjectList<Object> output) throws IOException {
        delegate.writeNetty(typeArgs, value, output);
    }

}
