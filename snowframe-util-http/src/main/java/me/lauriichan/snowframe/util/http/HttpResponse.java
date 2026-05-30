package me.lauriichan.snowframe.util.http;

import java.util.List;

public record HttpResponse<T>(HttpCode code, HttpData<T> data, HttpHeaders headers) {

    public boolean hasHeader(String key) {
        return headers.has(key);
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public String getHeaderValue(String key) {
        return headers.getValue(key);
    }

    public String getHeaderValueOrDefault(String key, String fallback) {
        return headers.getValueOrDefault(key, fallback);
    }

}
