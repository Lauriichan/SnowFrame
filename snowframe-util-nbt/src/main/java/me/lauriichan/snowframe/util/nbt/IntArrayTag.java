package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class IntArrayTag extends Tag<IntArrayTag> {

    private static final IntArrayTag EMPTY = new IntArrayTag(new int[0]);

    public static IntArrayTag of(int... values) {
        if (values.length == 0) {
            return EMPTY;
        }
        return new IntArrayTag(values);
    }

    static IntArrayTag readIntArray(DataInput input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Invalid array length: " + length);
        }
        if (length == 0) {
            return EMPTY;
        }
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = input.readInt();
        }
        return new IntArrayTag(array);
    }

    private final int[] array;

    private IntArrayTag(int[] array) {
        this.array = array;
    }

    public int[] array() {
        return array;
    }

    @Override
    public IntArrayTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            output.writeInt(array[i]);
        }
    }

}
