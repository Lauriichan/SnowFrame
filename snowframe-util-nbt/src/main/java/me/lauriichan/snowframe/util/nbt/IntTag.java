package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class IntTag extends NumericTag<IntTag> {
    
    public static IntTag of(int value) {
        return new IntTag(value);
    }
    
    static IntTag readInt(DataInput input) throws IOException {
        return new IntTag(input.readInt());
    }
    
    private final int value;
    
    public IntTag(int value) {
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }
    
    @Override
    public int asInt() {
        return value;
    }

    @Override
    public IntTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeInt(value);
    }

}
