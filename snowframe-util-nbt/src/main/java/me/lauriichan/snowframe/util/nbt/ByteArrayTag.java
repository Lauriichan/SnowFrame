package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ByteArrayTag extends Tag<ByteArrayTag> {

    private static final ByteArrayTag EMPTY = new ByteArrayTag(new byte[0]);

    public static ByteArrayTag of(byte... values) {
        if (values.length == 0) {
            return EMPTY;
        }
        return new ByteArrayTag(values);
    }

    static ByteArrayTag readByteArray(DataInput input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Invalid array length: " + length);
        }
        if (length == 0) {
            return EMPTY;
        }
        byte[] array = new byte[length];
        input.readFully(array);
        return new ByteArrayTag(array);
    }

    private final byte[] array;

    private ByteArrayTag(byte[] array) {
        this.array = array;
    }

    public byte[] array() {
        return array;
    }

    @Override
    public ByteArrayTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeInt(array.length);
        if (array.length != 0) {
            output.write(array);
        }
    }

}
