package me.lauriichan.snowframe.util.http.netty;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;

public interface INettyContentType<T> {
    
    T readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException;

    void writeNetty(IHeaderArgs typeArgs, T value, ObjectList<Object> output) throws IOException;

}
