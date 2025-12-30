package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.snowframe.util.Tuple;

public final class CompoundTag extends Tag<CompoundTag> {

    public static Tuple<String, CompoundTag> readNamed(DataInput input) throws IOException {
        TagType<?> type = readType(input);
        if (type != TagType.COMPOUND) {
            throw new IOException("Expected '" + TagType.COMPOUND + "' but got '" + type + "'");
        }
        String name = input.readUTF();
        return Tuple.of(name, (CompoundTag) type.read(input));
    }

    private final Object2ObjectMap<String, ITag<?>> tags = Object2ObjectMaps.synchronize(new Object2ObjectLinkedOpenHashMap<>());

    static CompoundTag readCompound(DataInput input) throws IOException {
        CompoundTag compound = new CompoundTag();
        byte id;
        while ((id = input.readByte()) != 0) {
            String key = input.readUTF();
            compound.tags.put(key, getType(id).read(input));
        }
        return compound;
    }

    @Override
    public CompoundTag duplicate() {
        CompoundTag tag = new CompoundTag();
        for (Entry<String, ITag<?>> entry : tags.object2ObjectEntrySet()) {
            tag.tags.put(entry.getKey(), entry.getValue().duplicate());
        }
        return tag;
    }

    public TagType<?> getType(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null) {
            return null;
        }
        return tag.type();
    }

    public TagType<?> getListType(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.LIST) {
            return null;
        }
        return ((ListTag) tag).elementType();
    }

    public boolean has(String key) {
        return tags.containsKey(key);
    }

    public boolean has(String key, TagType<?> type) {
        return getType(key) == type;
    }

    public boolean hasNumeric(String key) {
        TagType<?> type = getType(key);
        return type != null && type.isNumeric();
    }

    public boolean hasList(String key, TagType<?> type) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.LIST) {
            return false;
        }
        TagType<?> listType = ((ListTag) tag).elementType();
        return listType == type || listType == TagType.END;
    }

    public ITag<?> get(String key) {
        return tags.get(key);
    }

    public byte getByte(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asByte();
    }

    public short getShort(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asShort();
    }

    public int getInt(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asInt();
    }

    public long getLong(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asLong();
    }

    public float getFloat(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asFloat();
    }

    public double getDouble(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || !tag.type().isNumeric()) {
            return 0;
        }
        return ((NumericTag<?>) tag).asDouble();
    }

    public byte[] getByteArray(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.BYTE_ARRAY) {
            return null;
        }
        return ((ByteArrayTag) tag).array();
    }

    public int[] getIntArray(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.INT_ARRAY) {
            return null;
        }
        return ((IntArrayTag) tag).array();
    }

    public long[] getLongArray(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.LONG_ARRAY) {
            return null;
        }
        return ((LongArrayTag) tag).array();
    }

    public CompoundTag getCompound(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.COMPOUND) {
            return null;
        }
        return (CompoundTag) tag;
    }

    public ListTag getList(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.LIST) {
            return null;
        }
        return (ListTag) tag;
    }

    public ListTag getList(String key, TagType<?> type) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.LIST) {
            return null;
        }
        ListTag list = (ListTag) tag;
        if (list.elementType() != type) {
            return null;
        }
        return list;
    }

    public String getString(String key) {
        ITag<?> tag = tags.get(key);
        if (tag == null || tag.type() != TagType.STRING) {
            return null;
        }
        return ((StringTag) tag).asString();
    }

    public void put(String key, ITag<?> tag) {
        Objects.requireNonNull(key, "Key can't be null");
        Objects.requireNonNull(tag);
        if (tag.type() == TagType.END) {
            throw new IllegalArgumentException("Can't put end tag into compound");
        }
        tags.put(key, tag);
    }

    public void put(String key, byte value) {
        put(key, ByteTag.of(value));
    }

    public void put(String key, short value) {
        put(key, new ShortTag(value));
    }

    public void put(String key, int value) {
        put(key, new IntTag(value));
    }

    public void put(String key, long value) {
        put(key, new LongTag(value));
    }

    public void put(String key, float value) {
        put(key, new FloatTag(value));
    }

    public void put(String key, double value) {
        put(key, new DoubleTag(value));
    }

    public void put(String key, byte[] value) {
        put(key, ByteArrayTag.of(value));
    }

    public void put(String key, int[] value) {
        put(key, IntArrayTag.of(value));
    }

    public void put(String key, long[] value) {
        put(key, LongArrayTag.of(value));
    }

    public void put(String key, String value) {
        put(key, new StringTag(value));
    }

    public ObjectSet<String> keySet() {
        return tags.keySet();
    }

    public ObjectCollection<ITag<?>> values() {
        return tags.values();
    }

    public boolean remove(String key) {
        return tags.remove(key) != null;
    }

    public int size() {
        return tags.size();
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public void clear() {
        tags.clear();
    }

    /*
     * IO
     */

    public void writeNamed(DataOutput output) throws IOException {
        writeNamed(output, "");
    }

    public void writeNamed(DataOutput output, String name) throws IOException {
        Objects.requireNonNull(name, "Name can't be null");
        output.writeByte(type.id());
        output.writeUTF(name);
        writeData(output);
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        for (Entry<String, ITag<?>> entry : tags.object2ObjectEntrySet()) {
            if (entry.getValue().type().id() == 0) {
                continue;
            }
            output.writeByte(entry.getValue().type().id());
            output.writeUTF(entry.getKey());
            entry.getValue().writeData(output);
        }
        output.writeByte(0);
    }

}
