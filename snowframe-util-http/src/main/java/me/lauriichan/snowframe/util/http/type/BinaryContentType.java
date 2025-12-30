package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

final class BinaryContentType extends HttpContentType<byte[]> {

    public static final BinaryContentType BINARY = new BinaryContentType();

    private BinaryContentType() {
        super("application/octet-stream", "*/*", byte[].class);
        if (BINARY != null) {
            throw new UnsupportedOperationException("Singleton");
        }
    }

    @Override
    public byte[] read(FastByteArrayInputStream inputStream) throws IOException {
        byte[] output = new byte[inputStream.length];
        System.arraycopy(inputStream.array, inputStream.offset, output, 0, inputStream.length);
        return output;
    }

    @Override
    public void write(FastByteArrayOutputStream outputStream, byte[] value) throws IOException {
        outputStream.write(value);
    }

}
