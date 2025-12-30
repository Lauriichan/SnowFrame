package me.lauriichan.snowframe.util.nbt;

import java.io.DataOutput;
import java.io.IOException;

public final class EndTag extends Tag<EndTag> {

    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public EndTag duplicate() {
        return this;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {}

}
