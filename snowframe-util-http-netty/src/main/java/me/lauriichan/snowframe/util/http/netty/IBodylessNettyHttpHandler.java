package me.lauriichan.snowframe.util.http.netty;

import io.netty.channel.Channel;
import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;
import me.lauriichan.snowframe.util.http.server.Response;

@FunctionalInterface
public interface IBodylessNettyHttpHandler<T> extends INettyHttpHandler<T> {

    default Response handle(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path, HttpData<T> body) {
        return handle(channel, headers, query, path);
    }
    
    Response handle(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path);

}
