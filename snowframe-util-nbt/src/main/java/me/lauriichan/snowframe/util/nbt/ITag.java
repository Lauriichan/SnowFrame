package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ITag<T extends ITag<T>> {

    static ITag<?> read(DataInput input) throws IOException {
        return Tag.readType(input).read(input);
    }

    TagType<T> type();

    T duplicate();

    void write(DataOutput output) throws IOException;
    
    void writeData(DataOutput output) throws IOException;

}
