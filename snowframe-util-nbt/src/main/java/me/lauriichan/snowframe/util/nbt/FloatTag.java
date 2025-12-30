package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class FloatTag extends NumericTag<FloatTag> {
    
    public static FloatTag of(float value) {
        return new FloatTag(value);
    }
    
    static FloatTag readFloat(DataInput input) throws IOException {
        return new FloatTag(input.readFloat());
    }
    
    private final float value;
    
    public FloatTag(float value) {
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }
    
    @Override
    public float asFloat() {
        return value;
    }

    @Override
    public FloatTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeFloat(value);
    }

}
