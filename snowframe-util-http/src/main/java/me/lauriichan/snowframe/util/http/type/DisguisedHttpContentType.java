package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;

final class DisguisedHttpContentType<T> extends HttpContentType<T> {

    private final HttpContentType<T> delegate;

    DisguisedHttpContentType(HttpContentType<T> delegate, String name) {
        super(name, delegate.valueType(), delegate.acceptPattern());
        this.delegate = delegate;
    }

    @Override
    public T read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        return delegate.read(headers, inputStream);
    }

    @Override
    public void write(IHeaderArgs typeArgs, FastByteArrayOutputStream outputStream, T value) throws IOException {
        delegate.write(typeArgs, outputStream, value);
    }

}
