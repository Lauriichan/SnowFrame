package me.lauriichan.snowframe.util.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.Map.Entry;

public final class HttpHeaders {

    private static record ReadData(String value, int newIndex, int terminatorIndex) {}

    public static IHeaderArgs modifiableArgs() {
        return new ModifiableHeaderArgs(false);
    }

    public static IHeaderArgs modifiableArgs(boolean allowsOverwrites) {
        return new ModifiableHeaderArgs(allowsOverwrites);
    }

    public static IHeaderArgs modifiableArgs(IHeaderArgs source) {
        return new ModifiableHeaderArgs(source, false);
    }

    public static IHeaderArgs modifiableArgs(IHeaderArgs source, boolean allowsOverwrites) {
        return new ModifiableHeaderArgs(source, allowsOverwrites);
    }

    private static final class ModifiableHeaderArgs implements IHeaderArgs {

        private final Object2ObjectArrayMap<String, String> named = new Object2ObjectArrayMap<>(2);
        private final ObjectArrayList<String> unnamed = new ObjectArrayList<>(2);

        private final boolean allowsOverwrite;

        public ModifiableHeaderArgs(boolean allowOverwrite) {
            this.allowsOverwrite = allowOverwrite;
        }

        public ModifiableHeaderArgs(IHeaderArgs source, boolean allowOverwrite) {
            Objects.requireNonNull(source, "IHeaderArgs source can't be null, use ModifiableHeaderArgs(boolean) instead.");
            this.allowsOverwrite = allowOverwrite;
            for (String key : source.keys()) {
                named.put(key, source.get(key));
            }
            for (int i = 0; i < source.unnamedCount(); i++) {
                unnamed.add(source.getUnnamed(i));
            }
        }

        @Override
        public Set<String> keys() {
            return ObjectSets.unmodifiable(named.keySet());
        }

        @Override
        public boolean has(String key) {
            return named.containsKey(key);
        }

        @Override
        public String get(String key) {
            return getOrDefault(key, null);
        }

        @Override
        public String getOrDefault(String key, String fallback) {
            String value = named.get(key);
            if (value == null) {
                return fallback;
            }
            return value;
        }

        @Override
        public boolean hasUnnamed() {
            return !unnamed.isEmpty();
        }

        @Override
        public int unnamedCount() {
            return unnamed.size();
        }

        @Override
        public String getUnnamed(int index) {
            return getUnnamedOrDefault(index, null);
        }

        @Override
        public String getUnnamedOrDefault(int index, String fallback) {
            if (index < 0 || index >= unnamed.size()) {
                return fallback;
            }
            return unnamed.get(index);
        }

        @Override
        public boolean isModifiable() {
            return true;
        }

        @Override
        public boolean allowsOverwrite() {
            return allowsOverwrite;
        }

        @Override
        public ModifiableHeaderArgs reset() {
            if (!allowsOverwrite) {
                throw new IllegalStateException("Resetting not allowed");
            }
            named.clear();
            unnamed.clear();
            return this;
        }

        @Override
        public ModifiableHeaderArgs addUnnamed(String string) {
            unnamed.add(string);
            return this;
        }

        @Override
        public ModifiableHeaderArgs set(String key, String value) {
            if (has(key) && !allowsOverwrite) {
                throw new IllegalStateException("Key '%s' is already set".formatted(key));
            }
            named.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(getClass().getSimpleName()).append('{');
            builder.append("named={").append(named.toString()).append("}").append("unnamed=[").append(unnamed.toString()).append(']')
                .append('}');
            return builder.toString();
        }

    }

    private static final class HeaderArgs implements IHeaderArgs {

        private final Map<String, String> named;
        private final String[] unnamed;

        private HeaderArgs(Map<String, String> named, String[] unnamed) {
            this.named = named;
            this.unnamed = unnamed;
        }

        @Override
        public Set<String> keys() {
            return named.keySet();
        }

        @Override
        public boolean has(String key) {
            return named.containsKey(key);
        }

        @Override
        public String get(String key) {
            return getOrDefault(key, null);
        }

        @Override
        public String getOrDefault(String key, String fallback) {
            String value = named.get(key);
            if (value == null) {
                return fallback;
            }
            return value;
        }

        @Override
        public boolean hasUnnamed() {
            return unnamed.length != 0;
        }

        @Override
        public int unnamedCount() {
            return unnamed.length;
        }

        @Override
        public String getUnnamed(int index) {
            return getUnnamedOrDefault(index, null);
        }

        @Override
        public String getUnnamedOrDefault(int index, String fallback) {
            if (index < 0 || index >= unnamed.length) {
                return fallback;
            }
            return unnamed[index];
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(getClass().getSimpleName()).append('{');
            builder.append("named={").append(named.toString()).append("}").append("unnamed=[").append(unnamed.toString()).append(']')
                .append('}');
            return builder.toString();
        }

    }

    public static interface IHeaderArgs {

        Set<String> keys();

        boolean has(String key);

        String get(String key);

        String getOrDefault(String key, String fallback);

        default ObjectList<String> getAsList(String key) {
            return asList(get(key));
        }

        boolean hasUnnamed();

        int unnamedCount();

        String getUnnamed(int index);

        String getUnnamedOrDefault(int index, String fallback);

        default ObjectList<String> getUnnamedAsList(int index) {
            return asList(getUnnamed(index));
        }

        default boolean isModifiable() {
            return false;
        }

        default boolean allowsOverwrite() {
            return false;
        }

        default IHeaderArgs reset() {
            throw new UnsupportedOperationException();
        }

        default IHeaderArgs addUnnamed(String string) {
            throw new UnsupportedOperationException();
        }

        default IHeaderArgs set(String key, String value) {
            throw new UnsupportedOperationException();
        }

        default String asHeaderValue() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < unnamedCount(); i++) {
                if (!builder.isEmpty()) {
                    builder.append("; ");
                }
                append(builder, getUnnamed(i));
            }
            for (String key : keys()) {
                if (!builder.isEmpty()) {
                    builder.append("; ");
                }
                append(append(builder, key).append('='), get(key));
            }
            if (builder.isEmpty()) {
                return "\"\"";
            }
            return builder.toString();
        }

        private static StringBuilder append(StringBuilder builder, String str) {
            if (str.isEmpty()) {
                return builder.append("\"\"");
            }
            if (!UrlEncoder.needsEncodingHeader(str)) {
                return builder.append(str);
            }
            builder.append('"');
            if (str.contains("\"")) {
                builder.append(str.replace("\"", "\\\""));
            } else {
                builder.append(str);
            }
            return builder.append('"');
        }

    }

    private final Map<String, List<String>> delegate;
    private final boolean modifiable;

    public HttpHeaders(Map<String, List<String>> delegate, boolean modifiable) {
        this.delegate = delegate;
        this.modifiable = modifiable;
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
    
    public boolean isModifiable() {
        return modifiable;
    }

    public boolean has(String key) {
        List<String> list = delegate.get(key);
        return list != null && !list.isEmpty();
    }

    public List<String> get(String key) {
        return delegate.get(key);
    }

    public HttpHeaders remove(String key) {
        if (!modifiable) {
            return this;
        }
        delegate.remove(key);
        return this;
    }

    public HttpHeaders set(String key, List<String> list) {
        if (!modifiable || list == null || key == null || key.isBlank()) {
            return this;
        }
        delegate.put(key, list);
        return this;
    }

    public HttpHeaders set(String key, String string) {
        if (!modifiable || string == null || key == null || key.isBlank()) {
            return this;
        }
        List<String> list = delegate.get(key);
        if (list == null) {
            list = new ObjectArrayList<>();
            delegate.put(key, list);
        } else {
            list.clear();
        }
        list.add(string);
        return this;
    }
    
    public HttpHeaders setValue(String key, IHeaderArgs args) {
        return setValue(key, 0, args);
    }
    
    public HttpHeaders setValue(String key, int index, IHeaderArgs args) {
        if (args == null) {
            return this;
        }
        return setValue(key, index, args.asHeaderValue());
    }
    
    public HttpHeaders setValue(String key, String value) {
        return setValue(key, 0, value);
    }
    
    public HttpHeaders setValue(String key, int index, String value) {
        if (!modifiable || value == null || value.isBlank() || key == null || key.isBlank()) {
            return this;
        }
        List<String> list = delegate.get(key);
        if (list == null) {
            list = new ObjectArrayList<>();
            delegate.put(key, list);
        }
        if (index >= list.size()) {
            index = list.size() - 1;
        } else if (list.isEmpty()) {
            list.add(value);
            return this;
        }
        list.set(index, value);
        return this;
    }

    public HttpHeaders addValue(String key, IHeaderArgs args) {
        if (args == null) {
            return this;
        }
        return addValue(key, args.asHeaderValue());
    }

    public HttpHeaders addValue(String key, String value) {
        if (!modifiable || value == null || value.isBlank() || key == null || key.isBlank()) {
            return this;
        }
        List<String> list = delegate.get(key);
        if (list == null) {
            list = new ObjectArrayList<>();
            delegate.put(key, list);
        }
        list.add(value);
        return this;
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

    public ObjectList<String> getValueAsList(String key) {
        return asList(getValue(key));
    }

    public ObjectList<String> getValueAsList(String key, int index) {
        return asList(getValue(key, index));
    }
    
    public IHeaderArgs getArgumentsModifiable(String key) {
        return getArgumentsModifiable(key, 0);
    }
    
    public IHeaderArgs getArgumentsModifiable(String key, int index) {
        IHeaderArgs args = getArguments(key, index);
        if (args == null) {
            return modifiableArgs();
        }
        return modifiableArgs(args);
    }

    public IHeaderArgs getArguments(String key) {
        return getArguments(key, 0);
    }

    public IHeaderArgs getArguments(String key, int index) {
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName()).append('{');
        builder.append(delegate.toString()).append('}');
        return builder.toString();
    }

    /*
     * Helper
     */

    private static ObjectList<String> asList(String value) {
        if (value == null || value.isBlank()) {
            return ObjectLists.emptyList();
        }
        int current = 0;
        ReadData data;
        ObjectArrayList<String> values = new ObjectArrayList<>();
        while (current < value.length()) {
            data = readUntilUnescaped(value, current, ',', ';');
            current = data.newIndex + 1;
            if (data.terminatorIndex == 1) {
                break;
            }
            values.add(data.value.trim());
        }
        return ObjectLists.unmodifiable(values);
    }

    private static ReadData readUntilUnescaped(String string, int index, char... terminators) {
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
