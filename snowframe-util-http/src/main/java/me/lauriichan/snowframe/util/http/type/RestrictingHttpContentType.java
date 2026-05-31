package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.snowframe.util.http.HttpHeaders;

final class RestrictingHttpContentType<T> extends HttpContentType<T> {

    private final HttpContentType<T> delegate;

    RestrictingHttpContentType(HttpContentType<T> delegate, String[] acceptPattern) {
        super(delegate.name(), delegate.valueType(), acceptPattern);
        this.delegate = delegate;
    }

    @Override
    public T read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        return delegate.read(headers, inputStream);
    }

    @Override
    public void write(FastByteArrayOutputStream outputStream, T value) throws IOException {
        delegate.write(outputStream, value);
    }

}
