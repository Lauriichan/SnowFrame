package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

abstract class Tag<T extends Tag<T>> implements ITag<T> {

    static TagType<?> getType(byte id) throws IOException {
        TagType<?> type = TagType.of(id);
        if (type == null) {
            throw new IOException("Unknown tag type '" + id + "'");
        }
        return type;
    }

    static TagType<?> readType(DataInput input) throws IOException {
        return getType(input.readByte());
    }

    protected final TagType<T> type;

    @SuppressWarnings("unchecked")
    public Tag() {
        type = (TagType<T>) TagType.of(getClass());
    }

    @Override
    public final TagType<T> type() {
        return type;
    }

    @Override
    public final void write(DataOutput output) throws IOException {
        output.writeByte(type.id());
        writeData(output);
    }

}
