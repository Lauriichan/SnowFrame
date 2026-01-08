package me.lauriichan.snowframe.pulsar.schema;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import org.apache.pulsar.client.api.SchemaSerializationException;
import org.apache.pulsar.client.api.schema.SchemaReader;
import org.apache.pulsar.client.api.schema.SchemaWriter;

import it.auties.protobuf.stream.ProtobufInputStream;
import it.auties.protobuf.stream.ProtobufOutputStream;

final class ModernProtobufIO<T> implements SchemaWriter<T>, SchemaReader<T> {

    private static final Lookup lookup = MethodHandles.lookup();

    private static MethodHandle findStaticMethod(Class<?> type, String name, MethodType methodType) {
        try {
            return lookup.findStatic(type, name, methodType);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access method '" + name + "' in class: " + type.getName());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unknown method '" + name + "' in class: " + type.getName());
        }
    }

    private static final byte[] EMPTY = new byte[0];
    private final MethodHandle size, encode, decode;

    public ModernProtobufIO(Class<T> type) {
        Class<?> specType;
        try {
            specType = Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "Couldn't find class '" + type.getSimpleName() + "Spec' in package '" + type.getPackageName() + "'");
        }
        this.size = findStaticMethod(specType, "sizeOf", MethodType.methodType(int.class).appendParameterTypes(type));
        this.encode = findStaticMethod(specType, "encode",
            MethodType.methodType(void.class).appendParameterTypes(type, ProtobufOutputStream.class));
        this.decode = findStaticMethod(specType, "decode", MethodType.methodType(type).appendParameterTypes(ProtobufInputStream.class));
    }

    private int sizeOf(T message) {
        try {
            return (int) size.invoke(message);
        } catch (Throwable e) {
            throw new SchemaSerializationException(e);
        }
    }

    private void encode(T message, ProtobufOutputStream<?> outputStream) {
        try {
            encode.invoke(message, outputStream);
        } catch (Throwable e) {
            throw new SchemaSerializationException(e);
        }
    }

    private T decode(ProtobufInputStream inputStream) {
        try {
            return (T) decode.invoke(inputStream);
        } catch (Throwable e) {
            throw new SchemaSerializationException(e);
        }
    }

    @Override
    public byte[] write(T message) {
        int size = sizeOf(message);
        if (size == 0) {
            return EMPTY;
        }
        ProtobufOutputStream<byte[]> output = ProtobufOutputStream.toBytes(size);
        encode(message, output);
        return output.toOutput();
    }

    @Override
    public T read(byte[] bytes, int offset, int length) {
        return decode(ProtobufInputStream.fromBytes(bytes, offset, length));
    }

    @Override
    public T read(InputStream inputStream) {
        return decode(ProtobufInputStream.fromStream(inputStream));
    }

}
