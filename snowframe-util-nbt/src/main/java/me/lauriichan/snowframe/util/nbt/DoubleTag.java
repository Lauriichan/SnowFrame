package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class DoubleTag extends NumericTag<DoubleTag> {
    
    public static DoubleTag of(double value) {
        return new DoubleTag(value);
    }
    
    static DoubleTag readDouble(DataInput input) throws IOException {
        return new DoubleTag(input.readDouble());
    }
    
    private final double value;
    
    public DoubleTag(double value) {
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }
    
    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public DoubleTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeDouble(value);
    }

}
