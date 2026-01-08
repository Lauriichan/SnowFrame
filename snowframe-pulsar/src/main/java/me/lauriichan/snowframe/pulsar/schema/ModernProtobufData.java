package me.lauriichan.snowframe.pulsar.schema;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

import org.apache.pulsar.shade.com.fasterxml.jackson.databind.JsonNode;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.pulsar.shade.org.apache.avro.Schema;
import org.apache.pulsar.shade.org.apache.avro.Schema.Type;
import org.apache.pulsar.shade.org.apache.avro.generic.GenericData;
import org.apache.pulsar.shade.org.apache.avro.generic.GenericData.StringType;
import org.apache.pulsar.shade.org.apache.avro.util.internal.Accessor;

import it.auties.protobuf.annotation.ProtobufDefaultValue;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

final class ModernProtobufData {

    public static record PropertyElement(String name, AnnotatedElement element) {}

    public static final ModernProtobufData INSTANCE = new ModernProtobufData();

    private final Object2ObjectMap<Class<?>, Schema> schemaCache = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private final Object2ObjectMap<Class<? extends Enum<?>>, Enum<?>> enumDefaultCache = Object2ObjectMaps
        .synchronize(new Object2ObjectOpenHashMap<>());

    private final Schema nullSchema = Schema.create(Type.NULL);
    private final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private ModernProtobufData() {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException();
        }
    }

    public final Schema get(Class<?> clazz) {
        Schema schema = schemaCache.get(clazz);
        if (schema != null) {
            return schema;
        }
        return createSchema(clazz);
    }

    @SuppressWarnings("unchecked")
    private Schema createSchema(Class<?> clazz) {
        if (clazz.isEnum()) {
            if (clazz.isAnnotationPresent(ProtobufEnum.class)) {
                throw new RuntimeException();
            }
            return createSchemaForEnum(clazz.asSubclass(Enum.class));
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isAnnotation() || clazz.isArray()
            || clazz.isPrimitive()) {
            throw new RuntimeException();
        }
        if (clazz.isAnnotationPresent(ProtobufMessage.class)) {
            throw new RuntimeException();
        }
        if (clazz.isRecord()) {
            return createSchemaForRecord(clazz);
        }
        return createSchemaForClass(clazz);
    }

    private <E extends Enum<E>> Schema createSchemaForEnum(Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        ObjectArrayList<String> values = new ObjectArrayList<>(constants.length);
        Field field;
        E defaultValue = null;
        for (E value : constants) {
            values.push(value.name());
            try {
                field = clazz.getField(value.name());
            } catch (NoSuchFieldException | SecurityException e) {
                continue;
            }
            if (field.isAnnotationPresent(ProtobufDefaultValue.class)) {
                defaultValue = value;
            }
        }
        if (defaultValue == null && constants.length != 0) {
            defaultValue = constants[0];
        }
        enumDefaultCache.put(clazz, defaultValue);
        Schema schema = Schema.createEnum(clazz.getSimpleName(), null, clazz.getPackageName(), ObjectLists.unmodifiable(values));
        schemaCache.put(clazz, schema);
        return schema;
    }

    private Schema createSchemaForRecord(Class<?> clazz) {
        Schema schema = Schema.createRecord(clazz.getSimpleName(), null, clazz.getPackageName(), false);
        schemaCache.put(clazz, schema);
        try {
            for (RecordComponent component : clazz.getRecordComponents()) {
                ProtobufProperty property = component.getDeclaredAnnotation(ProtobufProperty.class);
                if (property == null) {
                    continue;
                }

            }
        } catch (RuntimeException re) {
            // Remove schema in case of an error
            schemaCache.remove(clazz);
        }
        return schema;
    }

    private Schema createSchemaForClass(Class<?> clazz) {
        Schema schema = Schema.createRecord(clazz.getSimpleName(), null, clazz.getPackageName(), false);
        schemaCache.put(clazz, schema);
        try {
            ObjectArrayList<Schema.Field> fields = new ObjectArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                ProtobufProperty property = field.getDeclaredAnnotation(ProtobufProperty.class);
                if (property == null) {
                    continue;
                }
                fields.add(Accessor.createField(field.getName(), schemaForProperty(field.getType(), property), null,
                    defaultForProperty(field.getType(), property)));
            }
            schema.setFields(fields);
        } catch (RuntimeException re) {
            // Remove schema in case of an error
            schemaCache.remove(clazz);
        }
        return schema;
    }

    private JsonNode defaultForProperty(Class<?> propertyType, ProtobufProperty property) {
        if (propertyType.isArray()) {
            return jsonFactory.arrayNode();
        }
        switch (property.type()) {
        case ENUM -> {
            return jsonFactory.textNode(enumDefaultCache.get(propertyType).name());
        }
        case MESSAGE -> {
            return jsonFactory.nullNode();
        }
        case BOOL -> {
            return jsonFactory.booleanNode(false);
        }
        case FLOAT -> {
            return jsonFactory.numberNode(0.0f);
        }
        case DOUBLE -> {
            return jsonFactory.numberNode(0.0);
        }
        case STRING -> {
            return jsonFactory.textNode("");
        }
        case BYTES -> {
            return jsonFactory.binaryNode(new byte[0]);
        }
        case INT32, UINT32, SINT32, FIXED32, SFIXED32, INT64, UINT64, SINT64, FIXED64, SFIXED64 -> {
            return jsonFactory.numberNode(0);
        }
        default -> {
            throw new RuntimeException("Unexpected type: '" + property.type() + "'");
        }
        }
    }

    private Schema schemaForProperty(Class<?> propertyType, ProtobufProperty property) {
        if (!propertyType.isArray()) {
            return schemaForNonArrayProperty(propertyType, property);
        }
        return Schema.createArray(schemaForProperty(propertyType.getComponentType(), property));
    }

    private Schema schemaForNonArrayProperty(Class<?> propertyType, ProtobufProperty property) {
        switch (property.type()) {
        case ENUM -> {
            return get(propertyType);
        }
        case MESSAGE -> {
            Schema schema = get(propertyType);
            if (!property.required()) {
                schema = Schema.createUnion(nullSchema, schema);
            }
            return schema;
        }
        case BOOL -> {
            return Schema.create(Type.BOOLEAN);
        }
        case FLOAT -> {
            return Schema.create(Type.FLOAT);
        }
        case DOUBLE -> {
            return Schema.create(Type.DOUBLE);
        }
        case STRING -> {
            Schema schema = Schema.create(Type.STRING);
            GenericData.setStringType(schema, StringType.String);
            return schema;
        }
        case BYTES -> {
            return Schema.create(Type.BYTES);
        }
        case INT32, UINT32, SINT32, FIXED32, SFIXED32 -> {
            return Schema.create(Type.INT);
        }
        case INT64, UINT64, SINT64, FIXED64, SFIXED64 -> {
            return Schema.create(Type.LONG);
        }
        default -> {
            throw new RuntimeException("Unexpected type: '" + property.type() + "'");
        }
        }
    }

}
