package me.lauriichan.snowframe.util.http;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class HttpHeaders {

    private final Map<String, List<String>> delegate;

    public HttpHeaders(Map<String, List<String>> delegate) {
        this.delegate = delegate;
    }

    public Map<String, List<String>> delegate() {
        return delegate;
    }

    public Set<String> keys() {
        return delegate.keySet();
    }

    public Set<Entry<String, List<String>>> entries() {
        return delegate.entrySet();
    }

    public boolean has(String key) {
        List<String> list = delegate.get(key);
        return list != null && !list.isEmpty();
    }

    public List<String> get(String key) {
        return delegate.get(key);
    }

    public String getValue(String key) {
        return getValueOrDefault(key, null);
    }

    public String getValueOrDefault(String key, String fallback) {
        List<String> list = delegate.get(key);
        if (list == null || list.isEmpty()) {
            return fallback;
        }
        return list.get(0);
    }

}
