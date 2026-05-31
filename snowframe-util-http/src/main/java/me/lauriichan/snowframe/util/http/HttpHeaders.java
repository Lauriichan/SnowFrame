package me.lauriichan.snowframe.util.http;

import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map.Entry;

public final class HttpHeaders {

    private static record ReadData(String value, int newIndex, int terminatorIndex) {}

    public static class HeaderArgs {

        private final Map<String, String> named;
        private final String[] unnamed;

        private HeaderArgs(Map<String, String> named, String[] unnamed) {
            this.named = named;
            this.unnamed = unnamed;
        }

        public Set<String> keys() {
            return named.keySet();
        }

        public boolean has(String key) {
            return named.containsKey(key);
        }

        public String get(String key) {
            return getOrDefault(key, null);
        }

        public String getOrDefault(String key, String fallback) {
            String value = named.get(key);
            if (value == null) {
                return fallback;
            }
            return value;
        }

        public boolean hasUnnamed() {
            return unnamed.length != 0;
        }

        public int unnamedCount() {
            return unnamed.length;
        }

        public String getUnnamed(int index) {
            return getUnnamedOrDefault(index, null);
        }

        public String getUnnamedOrDefault(int index, String fallback) {
            if (index < 0 || index >= unnamed.length) {
                return fallback;
            }
            return unnamed[index];
        }

    }

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
        return getValueOrDefault(key, 0, null);
    }

    public String getValue(String key, int index) {
        return getValueOrDefault(key, index, null);
    }

    public String getValueOrDefault(String key, String fallback) {
        return getValueOrDefault(key, 0, fallback);
    }

    public String getValueOrDefault(String key, int index, String fallback) {
        List<String> list = delegate.get(key);
        if (list == null || list.isEmpty() || index >= list.size() || index < 0) {
            return fallback;
        }
        return list.get(index);
    }

    public HeaderArgs getArguments(String key) {
        return getArguments(key, 0);
    }

    public HeaderArgs getArguments(String key, int index) {
        String value = getValue(key, index);
        if (value == null) {
            return null;
        }
        int current = 0;
        ReadData data;
        String hdrKey = null;
        Object2ObjectOpenHashMap<String, String> named = new Object2ObjectOpenHashMap<>();
        ObjectArrayList<String> unnamed = new ObjectArrayList<>();
        while (current < value.length()) {
            data = readUntilUnescaped(value, current, ';', '=');
            current = data.newIndex + 1;
            if (data.terminatorIndex == 1) {
                hdrKey = data.value;
                continue;
            }
            if (hdrKey != null) {
                named.put(hdrKey, data.value);
                hdrKey = null;
            } else {
                unnamed.add(data.value);
            }
        }
        named.trim();
        return new HeaderArgs(Object2ObjectMaps.unmodifiable(named), unnamed.toArray(String[]::new));
    }

    private ReadData readUntilUnescaped(String string, int index, char... terminators) {
        StringBuilder builder = new StringBuilder();
        char ch;
        Character escape = null;
        boolean backslashEscaped = false;
        int terminated = -1;
        readLoop:
        for (; index < string.length(); index++) {
            ch = string.charAt(index);
            if (builder.isEmpty() && Character.isWhitespace(ch)) {
                continue;
            }
            if (escape != null) {
                if (escape == ch) {
                    if (!backslashEscaped) {
                        escape = null;
                        continue;
                    } else {
                        backslashEscaped = true;
                    }
                }
                if (backslashEscaped) {
                    builder.append('\\');
                    backslashEscaped = false;
                }
                if (ch == '\\') {
                    backslashEscaped = true;
                    continue;
                }
                builder.append(ch);
                continue;
            }
            for (int n = 0; n < terminators.length; n++) {
                if (ch == terminators[n]) {
                    terminated = n;
                    break readLoop;
                }
            }
            if (ch == '\'' || ch == '"') {
                escape = ch;
                continue;
            }
            builder.append(ch);
        }
        if (backslashEscaped) {
            builder.append('\\');
        }
        return new ReadData(builder.toString(), index, terminated);
    }

}
