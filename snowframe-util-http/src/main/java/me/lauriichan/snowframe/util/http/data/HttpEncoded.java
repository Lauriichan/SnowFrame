package me.lauriichan.snowframe.util.http.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.snowframe.util.http.UrlEncoder;

public final class HttpEncoded {

    public static final MapType DEFAULT_TYPE = MapType.MUTABLE;
    public static final MapType DEFAULT_PARSE_TYPE = MapType.IMMUTABLE;

    private static final Function<String, ObjectArrayList<String>> CREATOR = (_i) -> new ObjectArrayList<>();
    private static final UrlEncoder ENCODER = UrlEncoder.UTF_8;

    public static enum MapType {

        MUTABLE(false, true),
        NORMALIZED_MUTABLE(true, true),
        IMMUTABLE(false, false),
        NORMALIZED_IMMUTABLE(true, false);

        private final boolean normalized, mutable;

        private MapType(boolean normalized, boolean mutable) {
            this.normalized = normalized;
            this.mutable = mutable;
        }

        public boolean normalized() {
            return normalized;
        }

        public boolean mutable() {
            return mutable;
        }

    }

    public static HttpEncoded parse(String urlEncoded) {
        return parse(urlEncoded, DEFAULT_PARSE_TYPE);
    }

    public static HttpEncoded parse(String urlEncoded, MapType type) {
        if (urlEncoded == null || urlEncoded.isBlank()) {
            return new HttpEncoded(type.mutable() ? new Object2ObjectArrayMap<>() : Object2ObjectMaps.emptyMap(), type);
        }
        if (urlEncoded.startsWith("?")) {
            urlEncoded = urlEncoded.substring(1);
        }
        String[] params = urlEncoded.split("&"), param;
        Map<String, List<String>> map = new Object2ObjectArrayMap<>(params.length);
        for (int i = 0; i < params.length; i++) {
            param = params[i].split("=", 3);
            String paramKey = ENCODER.decode(param[0]);
            List<String> entries = map.computeIfAbsent(paramKey, CREATOR);
            if (param.length != 2) {
                entries.add("true");
                continue;
            }
            entries.add(ENCODER.decode(param[1]));
        }
        return of(map, type);
    }

    public static HttpEncoded empty() {
        return new HttpEncoded(Object2ObjectMaps.emptyMap(), MapType.IMMUTABLE);
    }

    public static HttpEncoded of() {
        return of(false);
    }

    public static HttpEncoded of(boolean normalized) {
        return new HttpEncoded(new Object2ObjectArrayMap<>(), normalized ? MapType.MUTABLE : MapType.NORMALIZED_MUTABLE);
    }

    public static HttpEncoded of(Map<String, List<String>> map) {
        return of(map, DEFAULT_TYPE);
    }

    public static HttpEncoded of(Map<String, List<String>> map, MapType type) {
        return new HttpEncoded(transform(map, type), type);
    }

    private static Object2ObjectMap<String, List<String>> transform(Map<String, List<String>> map, MapType type) {
        if (map.isEmpty()) {
            if (map instanceof Object2ObjectMap<String, List<String>> otherMap) {
                if (type.mutable() && otherMap instanceof Object2ObjectMaps.UnmodifiableMap) {
                    return new Object2ObjectArrayMap<>();
                }
                return otherMap;
            }
            if (type.mutable()) {
                return new Object2ObjectArrayMap<>();
            }
            return Object2ObjectMaps.emptyMap();
        }
        boolean isMutable = map instanceof Object2ObjectMaps.UnmodifiableMap;
        if (isMutable != type.mutable() || !isMutable && type.normalized()
            || !(map instanceof Object2ObjectMap<String, List<String>> otherMap)) {
            Object2ObjectArrayMap<String, List<String>> newMap = new Object2ObjectArrayMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key != null && !key.isBlank()) {
                    key = key.toLowerCase();
                }
                newMap.computeIfAbsent(key, CREATOR).addAll(entry.getValue());
            }
            if (type.mutable()) {
                return newMap;
            }
            newMap.entrySet().forEach(entry -> entry.setValue(ObjectLists.unmodifiable((ObjectList<String>) entry.getValue())));
            return Object2ObjectMaps.unmodifiable(newMap);
        }
        return otherMap;
    }

    private final Object2ObjectMap<String, List<String>> map;
    private final MapType type;

    private HttpEncoded(Object2ObjectMap<String, List<String>> map, MapType type) {
        this.map = map;
        this.type = type;
    }

    public final Object2ObjectMap<String, List<String>> map() {
        return map;
    }

    public final MapType type() {
        return type;
    }

    private String key(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }
        return type.normalized() ? key.toLowerCase() : key;
    }

    public ObjectSet<String> keys() {
        return map.keySet();
    }

    public boolean has(String key) {
        return map.containsKey(key(key));
    }

    public int size() {
        return map.size();
    }

    public int size(String key) {
        List<String> list = get(key);
        return list == null ? 0 : list.size();
    }

    public List<String> get(String key) {
        return get(key, (List<String>) null);
    }

    public List<String> get(String key, List<String> fallback) {
        List<String> list = map.get(key(key));
        if (list == null) {
            return fallback;
        }
        return list;
    }

    public List<String> get(String key, Supplier<List<String>> fallbackSupplier) {
        List<String> list = map.get(key(key));
        if (list == null) {
            return fallbackSupplier.get();
        }
        return list;
    }

    public String getString(String key) {
        return getStringOr(key, 0, null);
    }

    public String getString(String key, int index) {
        return getStringOr(key, index, null);
    }

    public String getStringOr(String key, String fallback) {
        return getStringOr(key, 0, fallback);
    }

    public String getStringOr(String key, int index, String fallback) {
        List<String> list = get(key);
        if (list == null || list.size() < index || index < 0) {
            return fallback;
        }
        return list.get(index);
    }

    public Number getNumber(String key) {
        return getNumberOr(key, 0, null);
    }

    public Number getNumber(String key, int index) {
        return getNumberOr(key, index, null);
    }

    public Number getNumberOr(String key, Number fallback) {
        return getNumberOr(key, 0, fallback);
    }

    public Number getNumberOr(String key, int index, Number fallback) {
        List<String> list = get(key);
        if (list == null || list.size() < index || index < 0) {
            return fallback;
        }
        String string = list.get(index);
        try {
            BigDecimal decimal = new BigDecimal(string).stripTrailingZeros();
            if (decimal.scale() > 0) {
                return decimal;
            }
            return decimal.toBigIntegerExact();
        } catch (NumberFormatException nfe) {
            return fallback;
        }
    }

    public byte getByte(String key) {
        return getByteOr(key, 0, (byte) 0);
    }

    public byte getByte(String key, int index) {
        return getByteOr(key, index, (byte) 0);
    }

    public byte getByteOr(String key, byte fallback) {
        return getByteOr(key, 0, fallback);
    }

    public byte getByteOr(String key, int index, byte fallback) {
        return getNumberOr(key, index, fallback).byteValue();
    }

    public short getShort(String key) {
        return getShortOr(key, 0, (short) 0);
    }

    public short getShort(String key, int index) {
        return getShortOr(key, index, (short) 0);
    }

    public short getShortOr(String key, short fallback) {
        return getShortOr(key, 0, fallback);
    }

    public short getShortOr(String key, int index, short fallback) {
        return getNumberOr(key, index, fallback).shortValue();
    }

    public int getInt(String key) {
        return getIntOr(key, 0, 0);
    }

    public int getInt(String key, int index) {
        return getIntOr(key, index, 0);
    }

    public int getIntOr(String key, int fallback) {
        return getIntOr(key, 0, fallback);
    }

    public int getIntOr(String key, int index, int fallback) {
        return getNumberOr(key, index, fallback).intValue();
    }

    public long getLong(String key) {
        return getLongOr(key, 0, 0L);
    }

    public long getLong(String key, int index) {
        return getLongOr(key, index, 0L);
    }

    public long getLongOr(String key, long fallback) {
        return getLongOr(key, 0, fallback);
    }

    public long getLongOr(String key, int index, long fallback) {
        return getNumberOr(key, index, fallback).longValue();
    }

    public float getFloat(String key) {
        return getFloatOr(key, 0, 0f);
    }

    public float getFloat(String key, int index) {
        return getFloatOr(key, index, 0f);
    }

    public float getFloatOr(String key, float fallback) {
        return getFloatOr(key, 0, fallback);
    }

    public float getFloatOr(String key, int index, float fallback) {
        return getNumberOr(key, index, fallback).floatValue();
    }

    public double getDouble(String key) {
        return getDoubleOr(key, 0, 0d);
    }

    public double getDouble(String key, int index) {
        return getDoubleOr(key, index, 0d);
    }

    public double getDoubleOr(String key, long fallback) {
        return getDoubleOr(key, 0, fallback);
    }

    public double getDoubleOr(String key, int index, double fallback) {
        return getNumberOr(key, index, fallback).doubleValue();
    }

    public String asEncodedString() {
        StringBuilder builder = new StringBuilder();
        for (Object2ObjectMap.Entry<String, List<String>> mapEntry : map.object2ObjectEntrySet()) {
            List<String> valueEntries = mapEntry.getValue();
            if (valueEntries == null || valueEntries.isEmpty()) {
                continue;
            }
            String encodedKey = ENCODER.encode(mapEntry.getKey());
            for (String valueEntry : valueEntries) {
                if (valueEntry == null || valueEntry.isBlank()) {
                    continue;
                }
                builder.append(encodedKey).append('=').append(ENCODER.encode(valueEntry)).append('&');
            }
        }
        return builder.toString();
    }

}
