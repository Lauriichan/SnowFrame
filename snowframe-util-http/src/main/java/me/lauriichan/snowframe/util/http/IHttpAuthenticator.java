package me.lauriichan.snowframe.util.http;

import java.net.HttpURLConnection;

@FunctionalInterface
public interface IHttpAuthenticator {
    
    void authenticate(HttpURLConnection connection);

}
