package me.lauriichan.snowframe.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class HttpRequest {
    
    private static final IHttpProgressListener NOOP = (_1, _2) -> {};

    private final Object2ObjectArrayMap<String, String> headers = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<String, String> parameters = new Object2ObjectArrayMap<>();

    private volatile IHttpAuthenticator authenticator;
    private volatile IHttpProgressListener uploadListener = NOOP;
    private volatile IHttpProgressListener downloadListener = NOOP;

    private volatile String url;
    private volatile HttpMethod method = HttpMethod.GET;

    private volatile int connectTimeout = 2500, readTimeout = 5000;

    public HttpRequest() {
        setDefaultUserAgent();
    }

    public String url() {
        return url;
    }

    public HttpRequest url(String url) {
        this.url = Objects.requireNonNull(url);
        return this;
    }

    public HttpMethod method() {
        return method;
    }

    public HttpRequest method(HttpMethod method) {
        this.method = Objects.requireNonNull(method);
        return this;
    }
    
    public IHttpProgressListener downloadListener() {
        return downloadListener;
    }
    
    public HttpRequest downloadListener(IHttpProgressListener downloadListener) {
        this.downloadListener = downloadListener == null ? NOOP : downloadListener;
        return this;
    }
    
    public IHttpProgressListener uploadListener() {
        return uploadListener;
    }
    
    public HttpRequest uploadListener(IHttpProgressListener uploadListener) {
        this.uploadListener = uploadListener == null ? NOOP : uploadListener;
        return this;
    }

    public IHttpAuthenticator authenticator() {
        return authenticator;
    }

    public HttpRequest authenticator(IHttpAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public HttpRequest connectTimeout(int connectTimeout) {
        this.connectTimeout = Math.max(connectTimeout, 0);
        return this;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public HttpRequest readTimeout(int readTimeout) {
        this.readTimeout = Math.max(readTimeout, 0);
        return this;
    }

    public String param(String key) {
        return parameters.get(key);
    }

    public String paramOrDefault(String key, String fallback) {
        return parameters.getOrDefault(key, fallback);
    }

    public HttpRequest param(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Invalid key");
        }
        if (value == null) {
            parameters.remove(key);
            return this;
        }
        String stringValue;
        if (value instanceof String string) {
            stringValue = string;
        } else {
            stringValue = value.toString();
        }
        if (stringValue.isBlank()) {
            throw new IllegalArgumentException("Invalid value");
        }
        parameters.put(key, stringValue);
        return this;
    }

    public HttpRequest clearParameters() {
        parameters.clear();
        return this;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public String headerOrDefault(String key, String fallback) {
        return headers.getOrDefault(key, fallback);
    }

    public HttpRequest header(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Invalid key");
        }
        if (value == null) {
            headers.remove(key);
            return this;
        }
        String stringValue;
        if (value instanceof String string) {
            stringValue = string;
        } else {
            stringValue = value.toString();
        }
        if (stringValue.isBlank()) {
            throw new IllegalArgumentException("Invalid value");
        }
        headers.put(key, stringValue);
        return this;
    }

    public HttpRequest setDefaultUserAgent() {
        headers.put("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        return this;
    }

    public HttpRequest clearHeaders() {
        headers.clear();
        return this;
    }

    public <T> HttpResponse<T> call(HttpContentType<T> responseType) throws IOException {
        return call(responseType, null, null);
    }

    public <T> HttpResponse<T> call(HttpContentType<T> responseType, HttpContentType<T> contentType, T content) throws IOException {
        IHttpProgressListener downloadListener = this.downloadListener;
        IHttpProgressListener uploadListener = this.uploadListener;
        String url = this.url;
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Invalid url: " + url);
        }
        if (responseType == null) {
            throw new IllegalArgumentException("Invalid response type");
        }


        if (!parameters.isEmpty()) {
            UrlEncoder encoder = UrlEncoder.UTF_8;
            StringBuilder builder = new StringBuilder();
            for (Object2ObjectArrayMap.Entry<String, String> entry : parameters.object2ObjectEntrySet()) {
                if (entry.getValue() == null || entry.getValue().isBlank()) {
                    continue;
                }
                builder.append(encoder.encode(entry.getKey())).append('=').append(encoder.encode(entry.getValue())).append('&');
            }
            if (builder.length() != 0) {
                url = builder.insert(0, '?').insert(0, url).substring(0, builder.length() - 1);
            }
        }

        URL connectionUrl;
        try {
            connectionUrl = new URI(url).toURL();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid url", e);
        }

        HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
        connection.setConnectTimeout(0);
        connection.setReadTimeout(0);
        connection.setRequestMethod(method.name());
        for (Object2ObjectArrayMap.Entry<String, String> entry : headers.object2ObjectEntrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (authenticator != null) {
            authenticator.authenticate(connection);
        }
        connection.setRequestProperty("Accepts", responseType.name());

        byte[] data = null;
        int length = 0;
        if (contentType != null) {
            FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
            contentType.write(outputStream, content);
            data = outputStream.array;
            length = outputStream.length;
            if (length != 0) {
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(length);
                connection.setRequestProperty("Content-Type", contentType.name());
            }
        }

        connection.connect();

        if (length != 0) {
            OutputStream stream = connection.getOutputStream();
            int bufferSize, sent = 0;
            while ((length - sent) > 0) {
                uploadListener.progress(sent, length);
                bufferSize = Math.min(length - sent, 32768);
                stream.write(data, sent, bufferSize);
                stream.flush();
                sent += bufferSize;
            }
            uploadListener.progress(sent, length);
            stream.close();
        }

        InputStream stream;
        try {
            stream = connection.getInputStream();
            if (stream == null) {
                stream = connection.getErrorStream();
            }
        } catch (IOException ignore) {
            stream = connection.getErrorStream();
        }
        if (stream != null) {
            int maxBytes = connection.getContentLength();
            try (FastByteArrayOutputStream buffer = new FastByteArrayOutputStream()) {
                byte[] buf = new byte[32768];
                int tmp = buf.length;
                if (maxBytes != -1) {
                    tmp = Math.min(maxBytes, buf.length);
                }
                while (tmp != 0 && (length = stream.read(buf, 0, tmp)) != -1) {
                    downloadListener.progress(buffer.length, maxBytes);
                    buffer.write(buf, 0, length);
                    if (maxBytes != -1) {
                        tmp = Math.min(maxBytes - buffer.length, buf.length);
                    }
                }
                data = buffer.array;
                length = buffer.length;
                downloadListener.progress(length, length);
            }
        }

        if (data == null) {
            data = new byte[0];
            length = 0;
        }

        HttpData<T> responseData;
        try (FastByteArrayInputStream input = new FastByteArrayInputStream(data, 0, length)) {
            responseData = new HttpData<>(responseType.read(input), null);
        } catch (IOException exp) {
            responseData = new HttpData<>(null, exp);
        }
        return new HttpResponse<T>(HttpCode.byCode(connection.getResponseCode()), responseData, connection.getHeaderFields());
    }

}
