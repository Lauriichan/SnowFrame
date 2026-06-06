package me.lauriichan.snowframe.util.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class NormalizedMap implements Map<String, List<String>> {

    private final Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>();

    public NormalizedMap() {}

    public NormalizedMap(Map<String, List<String>> other) {
        putAll(other);
    }

    /**
     * @see com.sun.net.httpserver.Headers#normalize
     */
    private String normalize(String key) {
        Objects.requireNonNull(key);
        int len = key.length();
        if (len == 0) {
            return key;
        }
        char[] b = key.toCharArray();
        if (b[0] >= 'a' && b[0] <= 'z') {
            b[0] = (char) (b[0] - ('a' - 'A'));
        } else if (b[0] == '\r' || b[0] == '\n')
            throw new IllegalArgumentException("illegal character in key");

        for (int i = 1; i < len; i++) {
            if (b[i] >= 'A' && b[i] <= 'Z') {
                b[i] = (char) (b[i] + ('a' - 'A'));
            } else if (b[i] == '\r' || b[i] == '\n')
                throw new IllegalArgumentException("illegal character in key");
        }
        return new String(b);
    }

    private String key(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof String string) {
            return normalize(string);
        }
        return normalize(key.toString());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        return map.get(key(key));
    }

    @Override
    public List<String> put(String key, List<String> value) {
        return map.put(key(key), value);
    }

    @Override
    public List<String> remove(Object key) {
        return map.remove(key(key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        for (Map.Entry<? extends String, ? extends List<String>> entry : m.entrySet()) {
            map.put(key(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return map.entrySet();
    }

}
