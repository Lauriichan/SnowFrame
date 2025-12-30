package me.lauriichan.snowframe.util;

public final record Tuple<A, B>(A first, B second) {

    public static <A, B> Tuple<A, B> of(A first, B second) {
        return new Tuple<>(first, second);
    }

}
