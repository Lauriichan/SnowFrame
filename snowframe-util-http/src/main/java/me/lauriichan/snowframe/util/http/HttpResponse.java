package me.lauriichan.snowframe.util.http;

import java.util.List;
import java.util.Map;

public record HttpResponse<T>(HttpCode code, HttpData<T> data, Map<String, List<String>> headers) {

    public boolean hasHeader(String key) {
        List<String> list = headers.get(key);
        return list != null && !list.isEmpty();
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public String getHeaderValue(String key) {
        return getHeaderValueOrDefault(key, null);
    }

    public String getHeaderValueOrDefault(String key, String fallback) {
        List<String> list = headers.get(key);
        if (list == null || list.isEmpty()) {
            return fallback;
        }
        return list.get(0);
    }

}
