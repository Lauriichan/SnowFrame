package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class LongTag extends NumericTag<LongTag> {

    public static LongTag of(long value) {
        return new LongTag(value);
    }

    static LongTag readLong(DataInput input) throws IOException {
        return new LongTag(input.readLong());
    }

    private final long value;

    public LongTag(long value) {
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }

    @Override
    public long asLong() {
        return value;
    }

    @Override
    public LongTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeLong(value);
    }

}
