package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ByteTag extends NumericTag<ByteTag> {
    
    private static final ByteTag[] TAGS;
    
    static {
        ByteTag[] tags = new ByteTag[256];
        int index = 0;
        for (byte value = Byte.MIN_VALUE; index < tags.length; value++, index++) {
            tags[index] = new ByteTag(value);
        }
        TAGS = tags;
    }
    
    public static ByteTag of(byte value) {
        return TAGS[value + 128];
    }
    
    static ByteTag readByte(DataInput input) throws IOException {
        return of(input.readByte());
    }
    
    private final byte value;
    
    private ByteTag(byte value) {
        if (TAGS != null) {
            throw new UnsupportedOperationException();
        }
        this.value = value;
    }

    @Override
    public Number asNumber() {
        return value;
    }
    
    @Override
    public byte asByte() {
        return value;
    }

    @Override
    public ByteTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeByte(value);
    }

}
