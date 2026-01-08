package me.lauriichan.snowframe.pulsar.schema;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pulsar.client.impl.schema.AvroBaseStructSchema;
import org.apache.pulsar.client.impl.schema.ProtobufSchema;
import org.apache.pulsar.client.impl.schema.SchemaInfoImpl;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.common.util.ObjectMapperFactory;
import org.apache.pulsar.shade.com.fasterxml.jackson.core.JsonProcessingException;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;

public final class ModernProtobufSchema<T> extends AvroBaseStructSchema<T> {
    public static final String PARSING_INFO_PROPERTY = "__PARSING_INFO__";

    private static record ProtoProperty(String name, ProtobufProperty property) {}

    private ModernProtobufSchema(Class<T> type, SchemaInfo schemaInfo) {
        super(schemaInfo);
        ModernProtobufIO<T> io = new ModernProtobufIO<>(type);
        setReader(io);
        setWriter(io);
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
        HashMap<String, String> allProperties = properties == null ? new HashMap<>() : new HashMap<>(properties);
        try {
            Stream<ProtoProperty> propertyStream;
            if (clazz.isRecord()) {
                propertyStream = Arrays.stream(clazz.getRecordComponents())
                    .filter(component -> component.isAnnotationPresent(ProtobufProperty.class))
                    .map(component -> new ProtoProperty(component.getName(), component.getAnnotation(ProtobufProperty.class)));
            } else {
                propertyStream = Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ProtobufProperty.class))
                    .map(field -> new ProtoProperty(field.getName(), field.getAnnotation(ProtobufProperty.class)));
            }
            final List<ProtobufSchema.ProtoBufParsingInfo> parsingInfos = new LinkedList<>();
            propertyStream.forEach(prop -> {
                parsingInfos.add(new ProtobufSchema.ProtoBufParsingInfo(prop.property().index(), prop.name(), prop.property().type().name(),
                    prop.name(), null));

            });
            allProperties.put(PARSING_INFO_PROPERTY,
                ObjectMapperFactory.getMapperWithIncludeAlways().writer().writeValueAsString(parsingInfos));
        } catch (JsonProcessingException var4) {
            throw new RuntimeException(var4);
        }
        return new ModernProtobufSchema<T>(clazz,
            SchemaInfoImpl.builder().schema(ModernProtobufData.INSTANCE.get(clazz).toString().getBytes(StandardCharsets.UTF_8))
                .type(SchemaType.PROTOBUF).name(name).properties(allProperties).build());
    }

}
