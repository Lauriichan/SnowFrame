package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class StringTag extends Tag<StringTag> {

    public static StringTag of(String value) {
        return new StringTag(value);
    }

    public static StringTag of(StringBuilder builder) {
        return new StringTag(builder);
    }

    static StringTag readString(DataInput input) throws IOException {
        return new StringTag(input.readUTF());
    }

    private final String value;

    public StringTag(String value) {
        this.value = value;
    }

    public StringTag(StringBuilder builder) {
        this.value = builder.toString();
    }

    public String asString() {
        return value;
    }

    @Override
    public StringTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeUTF(value);
    }

}
