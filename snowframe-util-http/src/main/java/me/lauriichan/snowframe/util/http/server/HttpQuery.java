package me.lauriichan.snowframe.util.http.server;

import com.sun.net.httpserver.HttpExchange;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import me.lauriichan.snowframe.util.http.type.UrlEncodedContentType;

public final class HttpQuery {

    private final Object2ObjectMap<String, String> map;

    HttpQuery(HttpExchange exchange) {
        this.map = Object2ObjectMaps.unmodifiable(UrlEncodedContentType.URL_ENCODED.readFromString(exchange.getRequestURI().getQuery()));
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public boolean asBoolean(String key) {
        return asBoolean(key, false);
    }

    public boolean asBoolean(String key, boolean fallback) {
        String str = map.get(key);
        if (str == null) {
            return fallback;
        }
        return str.equalsIgnoreCase("true");
    }

    public int asInt(String key) {
        return asInt(key, 0);
    }

    public int asInt(String key, int fallback) {
        String str = map.get(key);
        if (str == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return fallback;
        }
    }

    public long asLong(String key) {
        return asLong(key, 0);
    }

    public long asLong(String key, long fallback) {
        String str = map.get(key);
        if (str == null) {
            return fallback;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return fallback;
        }
    }

    public String asString(String key) {
        return map.get(key);
    }

    public String asString(String key, String fallback) {
        String str = map.get(key);
        if (str == null) {
            return fallback;
        }
        return str;
    }

}
