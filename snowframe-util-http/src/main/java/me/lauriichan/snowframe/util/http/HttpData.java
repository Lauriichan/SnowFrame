package me.lauriichan.snowframe.util.http;

import java.io.IOException;

public record HttpData<T>(T value, IOException error) {

    public boolean isError() {
        return error != null;
    }

}
