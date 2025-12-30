package me.lauriichan.snowframe.util.http.server;

import java.net.InetAddress;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import me.lauriichan.snowframe.util.http.HttpData;

@FunctionalInterface
public interface IHttpSimpleHandler<T> extends IHttpHandler<T> {

    Response handle(InetAddress address, Headers headers, HttpQuery parameters);

    @Override
    default Response handle(HttpExchange exchange, InetAddress address, Headers headers, HttpQuery parameters, HttpData<T> body) {
        return handle(address, headers, parameters);
    }

}
