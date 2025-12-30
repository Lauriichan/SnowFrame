package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class LongArrayTag extends Tag<LongArrayTag> {

    private static final LongArrayTag EMPTY = new LongArrayTag(new long[0]);

    public static LongArrayTag of(long... values) {
        if (values.length == 0) {
            return EMPTY;
        }
        return new LongArrayTag(values);
    }

    static LongArrayTag readLongArray(DataInput input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Invalid array length: " + length);
        }
        if (length == 0) {
            return EMPTY;
        }
        long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = input.readLong();
        }
        return new LongArrayTag(array);
    }

    private final long[] array;

    private LongArrayTag(long[] array) {
        this.array = array;
    }

    public long[] array() {
        return array;
    }

    @Override
    public LongArrayTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            output.writeLong(array[i]);
        }
    }

}
