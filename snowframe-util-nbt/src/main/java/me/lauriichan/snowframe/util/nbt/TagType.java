package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.IOException;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

public final class TagType<T extends ITag<T>> {

    public static final int TAG_AMOUNT = 13;

    private static final Object2ObjectMap<Class<? extends ITag<?>>, TagType<?>> CLASS2TYPE;
    private static final Byte2ObjectMap<TagType<?>> ID2TYPE;

    public static final TagType<EndTag> END;

    public static final TagType<ByteTag> BYTE;
    public static final TagType<ShortTag> SHORT;
    public static final TagType<IntTag> INT;
    public static final TagType<LongTag> LONG;
    public static final TagType<FloatTag> FLOAT;
    public static final TagType<DoubleTag> DOUBLE;

    public static final TagType<ByteArrayTag> BYTE_ARRAY;
    public static final TagType<IntArrayTag> INT_ARRAY;
    public static final TagType<LongArrayTag> LONG_ARRAY;

    public static final TagType<StringTag> STRING;
    public static final TagType<ListTag> LIST;
    public static final TagType<CompoundTag> COMPOUND;

    static {
        Object2ObjectArrayMap<Class<? extends ITag<?>>, TagType<?>> classMap = new Object2ObjectArrayMap<>(TAG_AMOUNT);
        Byte2ObjectArrayMap<TagType<?>> idMap = new Byte2ObjectArrayMap<>(TAG_AMOUNT);
        END = type(classMap, idMap, 0, "TAG_END", EndTag.class, (i) -> EndTag.INSTANCE);
        BYTE = type(classMap, idMap, 1, "TAG_BYTE", ByteTag.class, ByteTag::readByte);
        SHORT = type(classMap, idMap, 2, "TAG_SHORT", ShortTag.class, ShortTag::readShort);
        INT = type(classMap, idMap, 3, "TAG_INT", IntTag.class, IntTag::readInt);
        LONG = type(classMap, idMap, 4, "TAG_LONG", LongTag.class, LongTag::readLong);
        FLOAT = type(classMap, idMap, 5, "TAG_FLOAT", FloatTag.class, FloatTag::readFloat);
        DOUBLE = type(classMap, idMap, 6, "TAG_DOUBLE", DoubleTag.class, DoubleTag::readDouble);
        BYTE_ARRAY = type(classMap, idMap, 7, "TAG_BYTE_ARRAY", ByteArrayTag.class, ByteArrayTag::readByteArray);
        INT_ARRAY = type(classMap, idMap, 11, "TAG_INT_ARRAY", IntArrayTag.class, IntArrayTag::readIntArray);
        LONG_ARRAY = type(classMap, idMap, 12, "TAG_LONG_ARRAY", LongArrayTag.class, LongArrayTag::readLongArray);
        STRING = type(classMap, idMap, 8, "TAG_STRING", StringTag.class, StringTag::readString);
        LIST = type(classMap, idMap, 9, "TAG_LIST", ListTag.class, ListTag::readList);
        COMPOUND = type(classMap, idMap, 10, "TAG_COMPOUND", CompoundTag.class, CompoundTag::readCompound);
        ID2TYPE = Byte2ObjectMaps.unmodifiable(idMap);
        CLASS2TYPE = Object2ObjectMaps.unmodifiable(classMap);
    }

    @FunctionalInterface
    private static interface ReadConstructor<T extends ITag<T>> {

        T read(DataInput input) throws IOException;

    }

    private static <T extends ITag<T>> TagType<T> type(Object2ObjectMap<Class<? extends ITag<?>>, TagType<?>> classMap,
        Byte2ObjectMap<TagType<?>> idMap, int id, String name, Class<T> type, ReadConstructor<T> constructor) {
        TagType<T> tag = new TagType<>(type, id, name, constructor);
        classMap.put(type, tag);
        idMap.put(tag.id(), tag);
        return tag;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITag<T>> TagType<T> of(Class<T> clazz) {
        return (TagType<T>) CLASS2TYPE.get(clazz);
    }

    public static TagType<?> of(int id) {
        if (id < 0 || id >= TAG_AMOUNT) {
            return null;
        }
        return ID2TYPE.get((byte) id);
    }

    private final Class<T> type;
    private final ReadConstructor<T> constructor;

    private final byte id;
    private final String name;
    
    private final boolean numeric;

    private TagType(Class<T> type, int id, String name, ReadConstructor<T> constructor) {
        if (ID2TYPE != null) {
            throw new UnsupportedOperationException();
        }
        this.type = type;
        this.id = (byte) id;
        this.name = name;
        this.constructor = constructor;
        this.numeric = NumericTag.class.isAssignableFrom(type);
    }

    public Class<T> type() {
        return type;
    }

    public byte id() {
        return id;
    }

    public String name() {
        return name;
    }

    public T read(DataInput input) throws IOException {
        return constructor.read(input);
    }

    public boolean isNumeric() {
        return numeric;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

}
