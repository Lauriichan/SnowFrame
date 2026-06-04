package me.lauriichan.snowframe.util.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

final class BasicHttpHandler<T> implements HttpHandler {

    private final ISimpleLogger logger;

    private final HttpContentType<T> bodyType;
    private final IHttpHandler<T> handler;

    private final ObjectArrayList<String> allowedMethods = new ObjectArrayList<>();

    public BasicHttpHandler(ISimpleLogger logger, HttpContentType<T> bodyType, IHttpHandler<T> handler, HttpMethod[] methods) {
        this.logger = logger;
        this.bodyType = Objects.requireNonNull(bodyType, "bodyType");
        this.handler = Objects.requireNonNull(handler, "handler");
        for (HttpMethod method : methods) {
            if (method == null || allowedMethods.contains(method.name())) {
                continue;
            }
            allowedMethods.add(method.name());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Response response = redirectRequest(exchange);
        if (response == null) {
            response = Response.of(HttpCode.NOT_IMPLEMENTED);
        }
        response.saveTo(exchange.getResponseHeaders());
        try {
            Response.Content<?> content = response.content();
            if (content == null) {
                exchange.sendResponseHeaders(response.code().code(), -1);
                return;
            }
            int length = 0;
            byte[] data;
            HttpHeaders headers = new HttpHeaders(exchange.getResponseHeaders());
            IHeaderArgs originalArgs = headers.getArguments("Content-Type");
            IHeaderArgs args;
            if (originalArgs == null) {
                args = HttpHeaders.modifiableArgs();
                args.addUnnamed(content.type().name());
            } else {
                args = HttpHeaders.modifiableArgs(originalArgs);
            }
            try (FastByteArrayOutputStream output = new FastByteArrayOutputStream()) {
                content.write(args, output);
                data = output.array;
                length = output.length;
            } catch (Exception exception) {
                if (logger != null) {
                    logger.error("Failed to handle request", exception);
                }
                exchange.sendResponseHeaders(HttpCode.INTERNAL_SERVER_ERROR.code(), -1);
                return;
            }
            if (data.length == 0) {
                exchange.sendResponseHeaders(response.code().code(), -1);
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", args.asHeaderValue());
            exchange.sendResponseHeaders(response.code().code(), length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(data, 0, length);
            stream.flush();
        } catch (Throwable exp) {
            if (logger != null) {
                logger.error("Failed to handle request", exp);
                exchange.close();
            }
        } finally {
            try {
                exchange.getResponseBody().close();
            } catch (IOException exp) {
                if (logger != null) {
                    logger.error("Failed to close request", exp);
                    exchange.close();
                }
            }
        }
    }

    private Response redirectRequest(HttpExchange exchange) throws IOException {
        if (allowedMethods.isEmpty() || !allowedMethods.contains(exchange.getRequestMethod())) {
            return Response.of(HttpCode.METHOD_NOT_ALLOWED);
        }
        int length = 0;
        byte[] data = null;
        try (InputStream stream = exchange.getRequestBody()) {
            int maxBytes = readContentLength(exchange);
            try (FastByteArrayOutputStream buffer = new FastByteArrayOutputStream()) {
                byte[] buf = new byte[32768];
                int tmp = buf.length;
                if (maxBytes != -1) {
                    tmp = Math.min(maxBytes, buf.length);
                }
                while (tmp != 0 && (length = stream.read(buf, 0, tmp)) != -1) {
                    buffer.write(buf, 0, length);
                    if (maxBytes != -1) {
                        tmp = Math.min(maxBytes - buffer.length, buf.length);
                    }
                }
                data = buffer.array;
                length = buffer.length;
            }
        }
        HttpHeaders headers = new HttpHeaders(exchange.getRequestHeaders());
        HttpData<T> body;
        try (FastByteArrayInputStream input = new FastByteArrayInputStream(data, 0, length)) {
            body = new HttpData<>(bodyType.read(headers, input), null);
        } catch (IOException exp) {
            body = new HttpData<>(null, exp);
        } catch (RuntimeException exp) {
            if (logger != null) {
                logger.error("Failed to handle request", exp);
            }
            return Response.of(HttpCode.INTERNAL_SERVER_ERROR);
        }

        try {
            return handler.handle(exchange, exchange.getRemoteAddress().getAddress(), headers, new HttpQuery(exchange), body);
        } catch (Exception exception) {
            if (logger != null) {
                logger.error("Failed to handle request", exception);
            }
            return Response.of(HttpCode.INTERNAL_SERVER_ERROR);
        }
    }

    private int readContentLength(HttpExchange exchange) {
        String lengthStr = exchange.getRequestHeaders().getFirst("content-length");
        if (lengthStr == null || lengthStr.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(lengthStr);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

}
