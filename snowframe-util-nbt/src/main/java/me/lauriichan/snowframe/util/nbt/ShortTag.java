package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ShortTag extends NumericTag<ShortTag> {
    
    public static ShortTag of(short value) {
        return new ShortTag(value);
    }
    
    static ShortTag readShort(DataInput input) throws IOException {
        return new ShortTag(input.readShort());
    }
    
    private final short value;
    
    public ShortTag(short value) {
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }
    
    @Override
    public short asShort() {
        return value;
    }

    @Override
    public ShortTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeShort(value);
    }

}
