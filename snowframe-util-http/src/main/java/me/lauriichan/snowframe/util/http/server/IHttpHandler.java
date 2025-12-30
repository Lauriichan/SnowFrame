package me.lauriichan.snowframe.util.http.server;

import java.net.InetAddress;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import me.lauriichan.snowframe.util.http.HttpData;

@FunctionalInterface
public interface IHttpHandler<T> {

    Response handle(HttpExchange exchange, InetAddress address, Headers headers, HttpQuery parameters, HttpData<T> body);

}
