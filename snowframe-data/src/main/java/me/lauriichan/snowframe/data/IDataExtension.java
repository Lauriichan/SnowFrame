package me.lauriichan.snowframe.data;

import me.lauriichan.snowframe.extension.IExtension;

public interface IDataExtension<T> extends IExtension {

    default String name() {
        return getClass().getSimpleName();
    }

    IDataHandler<T> handler();

}
