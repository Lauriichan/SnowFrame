package me.lauriichan.snowframe.pulsar.schema;

import java.util.Map;

import org.apache.pulsar.client.impl.schema.AvroBaseStructSchema;
import org.apache.pulsar.client.impl.schema.SchemaInfoImpl;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;

import it.auties.protobuf.annotation.ProtobufMessage;

public final class ModernProtobufSchema<T> extends AvroBaseStructSchema<T> {
    public static final String PARSING_INFO_PROPERTY = "__PARSING_INFO__";

    private ModernProtobufSchema(SchemaInfo schemaInfo, T protoMessageInstance) {
        super(schemaInfo);
    }

    public static <T> ModernProtobufSchema<T> of(Class<T> clazz) {
        return of(clazz, Map.of());
    }

    public static <T> ModernProtobufSchema<T> of(Class<T> clazz, Map<String, String> properties) {
        if (!clazz.isAnnotationPresent(ProtobufMessage.class)) {
            throw new IllegalArgumentException("%s is not a ProtobufMessage".formatted(clazz.getName()));
        }
        String name = "";
        ProtobufSchemaName schemaName = clazz.getDeclaredAnnotation(ProtobufSchemaName.class);
        if (schemaName != null) {
            name = schemaName.value();
            if (name == null) {
                throw new IllegalArgumentException("%s has invalid schema name: '%s'".formatted(clazz.getName(), name));
            }
        }
        SchemaInfo schemaInfo = SchemaInfoImpl.builder().schema(null).type(SchemaType.PROTOBUF).name(name)
            .properties(properties == null ? Map.of() : properties).build();

        return null;
    }

}
