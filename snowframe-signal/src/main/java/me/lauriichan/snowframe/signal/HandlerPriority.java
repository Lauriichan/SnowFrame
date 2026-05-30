package me.lauriichan.snowframe.signal;

//TODO: WIP
public enum HandlerPriority {

    LOWEST(-2),
    LOW(-1),
    NORMAL(0),
    HIGH(1),
    HIGHEST(2);

    private final int order;

    HandlerPriority(int order) {
        this.order = order;
    }

    public final int order() {
        return order;
    }

}
