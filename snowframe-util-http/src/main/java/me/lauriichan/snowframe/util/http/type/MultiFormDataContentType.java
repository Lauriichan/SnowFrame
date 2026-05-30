package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.MultiFormData;

final class MultiFormDataContentType extends HttpContentType<MultiFormData> {

    private final Object2ObjectMap<String, HttpContentType<?>> supported;

    MultiFormDataContentType(HttpContentType<?>[] supportedTypes) {
        super("multipart/form-data", "multipart/form-data", MultiFormData.class);
        Object2ObjectOpenHashMap<String, HttpContentType<?>> types = new Object2ObjectOpenHashMap<>();
        for (HttpContentType<?> supportedType : supportedTypes) {
            if (supportedType instanceof MultiFormDataContentType) {
                continue;
            }
            String[] accepts = supportedType.accepts().split(";");
            for (String accept : accepts) {
                types.put(accept, supportedType);
            }
        }
        this.supported = Object2ObjectMaps.unmodifiable(types);
    }

    @Override
    public MultiFormData read(HttpHeaders headers, FastByteArrayInputStream inputStream) throws IOException {
        System.out.println(headers.get("Content-Type").get(1));
        
        return new MultiFormData();
    }

    @Override
    public void write(FastByteArrayOutputStream outputStream, MultiFormData value) throws IOException {
        // TODO Auto-generated method stub

    }

}
