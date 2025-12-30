package me.lauriichan.snowframe.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;

public class Enum2ObjectMap<K extends Enum<K>, V> extends Reference2ObjectOpenCustomHashMap<K, V> {

    private static final long serialVersionUID = -2511917512475044189L;

    private static final Strategy<Enum<?>> STRATEGY = new Strategy<Enum<?>>() {
        @Override
        public int hashCode(Enum<?> o) {
            return o.ordinal();
        }

        @Override
        public boolean equals(Enum<?> a, Enum<?> b) {
            return a == b;
        }
    };

    public Enum2ObjectMap(Class<K> enumType) {
        super(enumType.getEnumConstants().length, 0.6f, STRATEGY);
    }

}
