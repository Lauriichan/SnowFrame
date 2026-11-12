package me.lauriichan.snowframe.util.http.netty;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

public record HttpPath(HttpNode node, Object2ObjectMap<String, Object> variables, boolean valid) {

    static class Builder {

        private final Object2ObjectArrayMap<String, Object> variables = new Object2ObjectArrayMap<>(2);

        Builder() {}

        public Builder var(String id, Object value) {
            if (value == null) {
                return this;
            }
            variables.put(id, value);
            return this;
        }

        public HttpPath done(HttpNode node, boolean valid) {
            return new HttpPath(node, variables.isEmpty() ? Object2ObjectMaps.emptyMap() : Object2ObjectMaps.unmodifiable(variables), valid);
        }

    }

}
