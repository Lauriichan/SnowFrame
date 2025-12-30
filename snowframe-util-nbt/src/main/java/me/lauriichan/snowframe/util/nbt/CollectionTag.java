package me.lauriichan.snowframe.util.nbt;

import java.io.DataOutput;
import java.io.IOException;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;

abstract class CollectionTag<T extends CollectionTag<T>> extends AbstractObjectList<ITag<?>> implements ITag<T> {

    protected final TagType<T> type;

    @SuppressWarnings("unchecked")
    public CollectionTag() {
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
