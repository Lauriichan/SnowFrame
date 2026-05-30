package me.lauriichan.snowframe.util.http.server;

import java.net.InetAddress;

import com.sun.net.httpserver.HttpExchange;

import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.HttpHeaders;

@FunctionalInterface
public interface IHttpHandler<T> {

    Response handle(HttpExchange exchange, InetAddress address, HttpHeaders headers, HttpQuery parameters, HttpData<T> body);

}
