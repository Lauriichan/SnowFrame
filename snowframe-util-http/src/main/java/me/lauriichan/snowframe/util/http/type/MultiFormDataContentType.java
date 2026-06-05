package me.lauriichan.snowframe.util.http.type;

import java.io.IOException;
import java.security.SecureRandom;

import com.sun.net.httpserver.Headers;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.data.MultiFormData;
import me.lauriichan.snowframe.util.http.data.MultiFormData.FormData;
import me.lauriichan.snowframe.util.http.plexus.SelectorUtils;

final class MultiFormDataContentType extends HttpContentType<MultiFormData> {

    private static final byte CR /* Carriage Return */ = 13, LF /* Line feed */ = 10;
    private static final SecureRandom BOUNDARY_RANDOM = new SecureRandom();

    private static final char[] BOUNDARY_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final ObjectList<HttpContentType<?>> supported;

    MultiFormDataContentType(HttpContentType<?>[] supportedTypes) {
        super("multipart/form-data", MultiFormData.class, "multipart/form-data");
        ObjectArrayList<HttpContentType<?>> supported = new ObjectArrayList<>();
        for (HttpContentType<?> supportedType : supportedTypes) {
            if (supportedType == null || supported.contains(supportedType) || supportedType instanceof MultiFormDataContentType) {
                continue;
            }
            supported.add(supportedType);
        }
        this.supported = ObjectLists.unmodifiable(supported);
    }

    @Override
    public MultiFormData read(HttpHeaders headers, FastByteArrayInputStream input) throws IOException {
        String boundary = headers.getArguments("Content-Type").get("boundary");
        if (boundary == null) {
            throw new IOException("No boundary set");
        }
        MultiFormData data = new MultiFormData();
        int boundarySize = boundary.length(), boundaryStart = boundarySize + 2;
        byte[] bytes = new byte[boundaryStart];
        while (input.available() != 0) {
            int firstCh = input.peek();
            if (firstCh == CR || firstCh == LF) {
                input.skip(1);
            }
            if (input.available() < boundaryStart) {
                throw new IOException("Invalid multi form data");
            }
            input.read(bytes);
            String str = new String(bytes);
            if (str.startsWith("--") && str.substring(2).equals(boundary)) {
                int b1 = input.read();
                int b2 = input.read();
                if (b1 == '-' && b2 == '-') {
                    // Boundary end
                    break;
                }
            }
            input.mark(0);
            HttpHeaders dataHeaders = parseHeaders(input);
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
            int dataStart = (int) input.position(), dataEnd = dataStart;
            firstCh = input.read();
            if (firstCh == CR || firstCh == LF) {
                firstCh = input.read();
                dataStart++;
            }
            while (true) {
                for (int ch = firstCh; ch != CR && ch != LF; ch = input.read()) {
                }
                input.mark(0);
                input.read(bytes);
                try {
                    str = new String(bytes);
                    if (str.startsWith("--") && str.substring(2).equals(boundary)) {
                        break;
                    }
                } finally {
                    input.reset();
                }
                dataEnd = (int) input.position();
            }
            int dataLength = dataEnd - dataStart;
            if (dataLength <= 0) {
                throw new IOException("Invalid form field data for field '%s'".formatted(fieldName));
            }
            data.readAndPut(fieldName, fileName, providedFieldType, fieldType, dataHeaders,
                new FastByteArrayInputStream(input.array, dataStart, dataLength));
        }
        return data;
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

    private HttpHeaders parseHeaders(FastByteArrayInputStream input) throws IOException {
        Headers hdrs = new Headers();
        char[] charBuf = new char[10];
        int firstCh = input.read(), length = 0;
        if (firstCh == CR || firstCh == LF) {
            int ch = input.read();
            if (ch == CR || ch == LF) {
                return new HttpHeaders(hdrs);
            }
            firstCh = ch;
        }
        while (firstCh != LF && firstCh != CR && firstCh >= 0) {
            int keyEnd = -1, ch;
            boolean inKey = firstCh > ' ';
            charBuf[length++] = (char) firstCh;
            parse:
            {
                while ((ch = input.read()) >= 0) {
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
                        firstCh = input.read();
                        if (ch == CR && firstCh == LF) {
                            firstCh = input.read();
                            if (firstCh == CR)
                                firstCh = input.read();
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
        return new HttpHeaders(hdrs);
    }

    @Override
    public void write(IHeaderArgs typeArgs, FastByteArrayOutputStream outputStream, MultiFormData value) throws IOException {
        StringBuilder builder = new StringBuilder("--");
        for (int length = 24; length > 0; length--) {
            builder.append(BOUNDARY_CHARS[BOUNDARY_RANDOM.nextInt(BOUNDARY_CHARS.length)]);
        }
        String boundary = builder.toString();
        typeArgs.set("boundary", boundary);
        FastByteArrayOutputStream dataStream = new FastByteArrayOutputStream();
        for (FormData<?> data : value.fields()) {
            dataStream.reset();
            outputStream.writeChars("--");
            outputStream.writeChars(boundary);
            outputStream.writeChar(CR);
            outputStream.writeChar(LF);
            outputStream.writeChars("Content-Disposition: form-data; name=\"");
            outputStream.writeChars(data.key());
            outputStream.writeChar('"');
            if (data.fileName() != null) {
                outputStream.writeChars("; filename=\"");
                outputStream.writeChars(data.fileName());
                outputStream.writeChar('"');
            }
            outputStream.writeChar(CR);
            outputStream.writeChar(LF);
            outputStream.writeChars("Content-Type: ");
            IHeaderArgs args = HttpHeaders.modifiableArgs();
            writeData(data, args, dataStream);
            outputStream.writeChars(args.asHeaderValue());
            outputStream.writeChar(CR);
            outputStream.writeChar(LF);
            outputStream.writeChar(CR);
            outputStream.writeChar(LF);
            outputStream.write(dataStream.array, 0, dataStream.length);
            outputStream.writeChar(CR);
            outputStream.writeChar(LF);
        }
        outputStream.writeChars("--");
        outputStream.writeChars(boundary);
        outputStream.writeChars("--");
        outputStream.writeChar(CR);
        outputStream.writeChar(LF);
    }

    private <T> void writeData(FormData<T> data, IHeaderArgs args, FastByteArrayOutputStream output) throws IOException {
        data.type().write(args, output, data.value());
    }

}
