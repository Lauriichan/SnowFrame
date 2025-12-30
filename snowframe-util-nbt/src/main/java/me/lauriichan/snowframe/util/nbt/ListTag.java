package me.lauriichan.snowframe.util.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

public final class ListTag extends CollectionTag<ListTag> {

    private static final ListTag EMPTY = new ListTag();

    static ListTag readList(DataInput input) throws IOException {
        TagType<?> type = Tag.readType(input);
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Invalid list length: " + length);
        }
        if (length == 0) {
            return EMPTY;
        }
        ListTag list = new ListTag(type);
        for (int i = 0; i < length; i++) {
            list.list.add(type.read(input));
        }
        return list;
    }

    private final TagType<?> elementType;
    private final ObjectList<ITag<?>> list;

    private ListTag() {
        if (EMPTY != null) {
            throw new IllegalArgumentException("Can't create list of type " + TagType.END);
        }
        this.elementType = TagType.END;
        this.list = ObjectLists.emptyList();
    }

    public ListTag(TagType<?> elementType) {
        this.elementType = checkType(elementType);
        this.list = ObjectLists.synchronize(new ObjectArrayList<>());
    }

    public ListTag(TagType<?> elementType, int capacity) {
        this.elementType = checkType(elementType);
        this.list = ObjectLists.synchronize(new ObjectArrayList<>(capacity));
    }

    private TagType<?> checkType(TagType<?> elementType) {
        Objects.requireNonNull(elementType, "Element type can't be null");
        if (elementType == TagType.END && EMPTY != null) {
            throw new IllegalArgumentException("Can't create list of type " + TagType.END);
        }
        return elementType;
    }

    public TagType<?> elementType() {
        return elementType;
    }

    @Override
    public ListTag duplicate() {
        if (this == EMPTY) {
            return EMPTY;
        }
        ListTag newList = new ListTag(elementType);
        for (int i = 0; i < list.size(); i++) {
            newList.list.add(list.get(i).duplicate());
        }
        return newList;
    }

    @Override
    public int size() {
        return list.size();
    }

    @SuppressWarnings("unchecked")
    public <E extends ITag<E>> E get(int index, TagType<E> type) {
        if (type != elementType) {
            return null;
        }
        return (E) get(index);
    }

    @Override
    public ITag<?> get(int index) {
        return list.get(index);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean add(ITag<?> tag) {
        return list.add(checkTag(tag));
    }

    @Override
    public void add(int index, ITag<?> tag) {
        list.add(index, checkTag(tag));
    }

    @Override
    public ITag<?> set(int index, ITag<?> tag) {
        return list.set(index, checkTag(tag));
    }
    
    private ITag<?> checkTag(ITag<?> tag) {
        Objects.requireNonNull(tag, "Tag can't be null");
        if (tag.type() != elementType) {
            throw new IllegalArgumentException("Expected tag of type " + elementType + " but got tag of type " + tag.type());
        }
        return tag;
    }

    @Override
    public void writeData(DataOutput output) throws IOException {
        if (list.isEmpty()) {
            output.writeByte(TagType.END.id());
            output.writeInt(0);
            return;
        }
        output.writeByte(elementType.id());
        output.writeInt(list.size());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).writeData(output);
        }
    }
}