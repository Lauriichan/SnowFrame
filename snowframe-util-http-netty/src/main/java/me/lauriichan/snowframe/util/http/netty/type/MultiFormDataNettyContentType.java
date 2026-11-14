package me.lauriichan.snowframe.util.http.netty.type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import com.sun.net.httpserver.Headers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.data.MultiFormData;
import me.lauriichan.snowframe.util.http.data.MultiFormData.FormData;
import me.lauriichan.snowframe.util.http.netty.INettyContentType;
import me.lauriichan.snowframe.util.http.plexus.SelectorUtils;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

final class MultiFormDataNettyContentType extends NettyContentType<MultiFormData> {

    private static final byte CR /* Carriage Return */ = 13, LF /* Line feed */ = 10;
    private static final SecureRandom BOUNDARY_RANDOM = new SecureRandom();

    private static final char[] BOUNDARY_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final ObjectList<HttpContentType<?>> supported;

    MultiFormDataNettyContentType(HttpContentType<?>[] supportedTypes) {
        super("multipart/form-data", MultiFormData.class, "multipart/form-data");
        ObjectArrayList<HttpContentType<?>> supported = new ObjectArrayList<>();
        for (HttpContentType<?> supportedType : supportedTypes) {
            if (supportedType == null || supported.contains(supportedType) || supportedType instanceof MultiFormDataNettyContentType) {
                continue;
            }
            supported.add(supportedType);
        }
        this.supported = ObjectLists.unmodifiable(supported);
    }

    @Override
    public MultiFormData readNetty(HttpHeaders headers, ByteBuf buffer) throws IOException {
        String boundary = headers.getArguments("Content-Type").get("boundary");
        if (boundary == null) {
            throw new IOException("No boundary set");
        }
        MultiFormData data = new MultiFormData();
        int boundarySize = boundary.length(), boundaryStart = boundarySize + 2;
        byte[] bytes = new byte[boundaryStart];
        while (buffer.readableBytes() != 0) {
            int firstCh = buffer.getByte(buffer.readerIndex());
            if (firstCh == CR || firstCh == LF) {
                buffer.skipBytes(1);
            }
            if (buffer.readableBytes() < boundaryStart) {
                throw new IOException("Invalid multi form data");
            }
            buffer.readBytes(bytes);
            String str = new String(bytes, StandardCharsets.US_ASCII);
            if (str.startsWith("--") && str.substring(2).equals(boundary)) {
                int b1 = buffer.readByte();
                int b2 = buffer.readByte();
                if (b1 == '-' && b2 == '-') {
                    // Boundary end
                    break;
                }
            }
            buffer.markReaderIndex();
            HttpHeaders dataHeaders = parseHeaders(buffer);
            IHeaderArgs disposition = dataHeaders.getArguments("Content-Disposition");
            if (disposition == null || !disposition.hasUnnamed() || !disposition.getUnnamed(0).equalsIgnoreCase("form-data")) {
                throw new IOException("Invalid multi form data");
            }
            String fieldName = disposition.get("name");
            String fileName = disposition.getOrDefault("filename", null);
            String providedFieldType = rawContentTypeOf(dataHeaders);
            HttpContentType<?> fieldType = supportedTypeFor(providedFieldType);
            if (fieldType == null) {
                throw new IOException("Unsupported content type '%s' for form field '%s'".formatted(providedFieldType, fieldName));
            }
            int dataStart = (int) buffer.readerIndex(), dataEnd = dataStart;
            firstCh = buffer.readByte();
            if (firstCh == CR || firstCh == LF) {
                firstCh = buffer.readByte();
                dataStart++;
            }
            while (true) {
                for (int ch = firstCh; ch != CR && ch != LF; ch = buffer.readByte()) {
                }
                buffer.markReaderIndex();
                buffer.readBytes(bytes);
                try {
                    str = new String(bytes, StandardCharsets.US_ASCII);
                    if (str.startsWith("--") && str.substring(2).equals(boundary)) {
                        break;
                    }
                } finally {
                    buffer.resetReaderIndex();
                }
                dataEnd = (int) buffer.readerIndex();
            }
            int dataLength = dataEnd - dataStart;
            if (dataLength <= 0) {
                throw new IOException("Invalid form field data for field '%s'".formatted(fieldName));
            }
            if (fieldType instanceof INettyContentType) {
                readAndPutNetty(data, fieldName, fileName, providedFieldType, fieldType, dataHeaders, buffer.slice(dataStart, dataLength));
            } else {
                byte[] dataArray = new byte[dataLength];
                buffer.getBytes(dataStart, dataArray);
                data.readAndPut(fieldName, fileName, providedFieldType, fieldType, dataHeaders,
                    new FastByteArrayInputStream(dataArray, 0, dataLength));
            }

        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private <T> void readAndPutNetty(MultiFormData data, String key, String fileName, String providedType, HttpContentType<T> type,
        HttpHeaders headers, ByteBuf input) throws IOException {
        data.put(key, fileName, providedType, type, ((INettyContentType<T>) type).readNetty(headers, input));
    }

    private String rawContentTypeOf(HttpHeaders headers) {
        IHeaderArgs type = headers.getArguments("Content-Type");
        if (type != null && type.hasUnnamed()) {
            return type.getUnnamed(0);
        }
        return "text/plain";
    }

    public HttpContentType<?> supportedTypeFor(String rawType) {
        for (HttpContentType<?> type : supported) {
            for (String pattern : type.acceptPattern()) {
                if (SelectorUtils.match(pattern, rawType, false)) {
                    return type;
                }
            }
        }
        return null;
    }

    private HttpHeaders parseHeaders(ByteBuf input) throws IOException {
        Headers hdrs = new Headers();
        char[] charBuf = new char[10];
        int firstCh = input.readByte(), length = 0;
        if (firstCh == CR || firstCh == LF) {
            int ch = input.readByte();
            if (ch == CR || ch == LF) {
                return new HttpHeaders(hdrs, false);
            }
            firstCh = ch;
        }
        while (firstCh != LF && firstCh != CR && firstCh >= 0) {
            int keyEnd = -1, ch;
            boolean inKey = firstCh > ' ';
            charBuf[length++] = (char) firstCh;
            parse:
            {
                while ((ch = input.readByte()) >= 0) {
                    switch (ch) {
                    /*fallthrough*/
                    case ':':
                        if (inKey && length > 0)
                            keyEnd = length;
                        inKey = false;
                        break;
                    case '\t':
                        ch = ' ';
                    case ' ':
                        inKey = false;
                        break;
                    case CR:
                    case LF:
                        firstCh = input.readByte();
                        if (ch == CR && firstCh == LF) {
                            firstCh = input.readByte();
                            if (firstCh == CR)
                                firstCh = input.readByte();
                        }
                        if (firstCh == LF || firstCh == CR || firstCh > ' ')
                            break parse;
                        /* continuation */
                        ch = ' ';
                        break;
                    }
                    if (length >= charBuf.length) {
                        char ns[] = new char[charBuf.length * 2];
                        System.arraycopy(charBuf, 0, ns, 0, length);
                        charBuf = ns;
                    }
                    charBuf[length++] = (char) ch;
                }
                firstCh = -1;
            }
            while (length > 0 && charBuf[length - 1] <= ' ')
                length--;
            String key;
            if (keyEnd <= 0) {
                key = null;
                keyEnd = 0;
            } else {
                key = String.copyValueOf(charBuf, 0, keyEnd);
                if (keyEnd < length && charBuf[keyEnd] == ':')
                    keyEnd++;
                while (keyEnd < length && charBuf[keyEnd] <= ' ')
                    keyEnd++;
            }
            String value;
            if (keyEnd >= length)
                value = new String();
            else
                value = String.copyValueOf(charBuf, keyEnd, length - keyEnd);
            if (hdrs.size() >= 12) {
                throw new IOException("Maximum number of form data headers exceeded, 12.");
            }
            if (key == null) {  // Headers disallows null keys, use empty string
                key = "";       // instead to represent invalid key
            }
            hdrs.add(key, value);
            length = 0;
        }
        return new HttpHeaders(hdrs, false);
    }

    @Override
    public void writeNetty(IHeaderArgs typeArgs, MultiFormData value, ObjectList<Object> output) throws IOException {
        StringBuilder builder = new StringBuilder("--");
        for (int length = 24; length > 0; length--) {
            builder.append(BOUNDARY_CHARS[BOUNDARY_RANDOM.nextInt(BOUNDARY_CHARS.length)]);
        }
        String boundary = builder.toString();
        typeArgs.set("boundary", boundary);
        ObjectArrayList<Object> bufferOutput = new ObjectArrayList<>();
        for (FormData<?> data : value.fields()) {
            bufferOutput.clear();
            builder = new StringBuilder();
            builder.append("--").append(boundary).append("\r\n");
            builder.append("Content-Disposition: form-data; name=").append('"').append(data.key()).append('"');
            if (data.fileName() != null) {
                builder.append("; filename=").append('"').append(data.fileName()).append('"');
            }
            builder.append("\r\n");
            IHeaderArgs args = HttpHeaders.modifiableArgs();
            args.addUnnamed(data.type().name());
            writeData(data, args, bufferOutput);
            output.add(charset());
            output.add(builder.append("Content-Type: ").append(args.asHeaderValue()).append("\r\n").append("\r\n").toString());
            output.add(data.type().charset());
            output.addAll(bufferOutput);
            output.add("\r\n");
        }
        output.add(charset());
        output.add(new StringBuilder("--").append(boundary).append("--").append("\r\n").toString());
    }

    @SuppressWarnings("unchecked")
    private <T> void writeData(FormData<T> data, IHeaderArgs args, ObjectArrayList<Object> output) throws IOException {
        HttpContentType<T> contentType = data.type();
        if (contentType instanceof INettyContentType nettyType) {
            ((INettyContentType<T>) nettyType).writeNetty(args, data.value(), output);
            return;
        }
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        contentType.write(args, outputStream, data.value());
        output.add(Unpooled.wrappedBuffer(outputStream.array, 0, outputStream.length));
    }

}
