package me.lauriichan.snowframe.util.http.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class MultiFormData {

    public static record FormData<T>(String key, HttpContentType<T> contentType, T value) {}

    private final Object2ObjectOpenHashMap<String, FormData<?>> data = new Object2ObjectOpenHashMap<>();

}
