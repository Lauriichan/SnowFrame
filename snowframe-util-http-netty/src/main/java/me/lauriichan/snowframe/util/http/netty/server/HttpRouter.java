package me.lauriichan.snowframe.util.http.netty.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.HttpHeaders.IHeaderArgs;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;
import me.lauriichan.snowframe.util.http.data.HttpEncoded.MapType;
import me.lauriichan.snowframe.util.http.netty.HttpNode;
import me.lauriichan.snowframe.util.http.netty.HttpPath;
import me.lauriichan.snowframe.util.http.netty.INettyContentType;
import me.lauriichan.snowframe.util.http.server.Response;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class HttpRouter extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
    public static final AttributeKey<HttpMethod> METHOD = AttributeKey.valueOf("HttpMethod");

    private static final Function<String, ObjectList<String>> CREATOR = (_i) -> new ObjectArrayList<>();

    private static final Object2ObjectMap<io.netty.handler.codec.http.HttpMethod, HttpMethod> METHOD_MAP;

    static {
        HttpMethod[] methods = HttpMethod.values();
        Object2ObjectArrayMap<io.netty.handler.codec.http.HttpMethod, HttpMethod> methodMap = new Object2ObjectArrayMap<>(methods.length);
        for (HttpMethod method : methods) {
            methodMap.put(io.netty.handler.codec.http.HttpMethod.valueOf(method.name()), method);
        }
        METHOD_MAP = Object2ObjectMaps.unmodifiable(methodMap);
    }

    private final ISimpleLogger logger;
    private final HttpNode root;

    public HttpRouter(ISimpleLogger logger, HttpNode root) {
        this.logger = logger;
        this.root = root;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(msg.uri());
        Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>(msg.headers().size());
        for (Map.Entry<String, String> entry : msg.headers()) {
            map.computeIfAbsent(entry.getKey(), CREATOR).add(entry.getValue());
        }
        map.trim();

        String path = queryDecoder.path();
        HttpHeaders headers = new HttpHeaders(Object2ObjectMaps.unmodifiable(map));
        HttpEncoded query = HttpEncoded.parse(queryDecoder.rawQuery(), MapType.IMMUTABLE);
        ByteBuf content = msg.content();

        Response response = null;
        try {
            HttpPath resolvedPath = root.resolve(headers, query, path);
            if (!resolvedPath.valid()) {
                return;
            }
            HttpNode node = resolvedPath.node();
            if (!node.isAnyHandled()) {
                return;
            }
            HttpMethod method = METHOD_MAP.get(msg.method());
            if (method == null || !node.isHandled(method)) {
                response = Response.of(HttpCode.METHOD_NOT_ALLOWED);
                return;
            }
            ctx.channel().attr(METHOD).set(method);
            response = node.handler(method).handle(ctx.channel(), headers, query, resolvedPath, content);
        } finally {
            if (response == null) {
                response = Response.of(HttpCode.NOT_IMPLEMENTED);
            }
            writeResponse(ctx, msg.protocolVersion(), response);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeResponse(ChannelHandlerContext ctx, HttpVersion version, Response response) throws IOException {
        HttpHeaders headers = response.asHeaders();
        Response.Content<?> content = response.content();

        DefaultHttpResponse httpResponse = new DefaultHttpResponse(version, HttpResponseStatus.valueOf(response.code().code()));
        if (content == null) {
            headers.entries().forEach(entry -> httpResponse.headers().set(entry.getKey(), entry.getValue()));
            ctx.writeAndFlush(httpResponse);
            return;
        }
        IHeaderArgs originalArgs = headers.getArguments("Content-Type");
        IHeaderArgs args;
        if (originalArgs == null) {
            args = HttpHeaders.modifiableArgs();
            args.addUnnamed(response.content().type().name());
        } else {
            args = HttpHeaders.modifiableArgs(originalArgs);
        }
        ObjectArrayList<Object> output = new ObjectArrayList<>();
        writeContent(args, response.content(), output);
        headers.getArguments("Transfer-Encoding").reset().addUnnamed("chunked");
        headers.entries().forEach(entry -> httpResponse.headers().set(entry.getKey(), entry.getValue()));
        ctx.write(httpResponse);
        Charset charset = response.content().type().charset();
        if (charset == null) {
            charset = StandardCharsets.US_ASCII;
        }
        for (Object obj : output) {
            if (obj == null) {
                logger.warning("Null found in response content, skipping");
                continue;
            }
            if (obj instanceof Charset newCharset) {
                charset = newCharset;
                continue;
            }
            if (obj instanceof HttpContent httpContent) {
                if (httpContent instanceof LastHttpContent) {
                    logger.warning("LastHttpContent in response content, converting");
                    if (httpContent == LastHttpContent.EMPTY_LAST_CONTENT) {
                        continue;
                    }
                    httpContent = new DefaultHttpContent(httpContent.content());
                }
                ctx.write(httpContent);
                continue;
            }
            if (obj instanceof ByteBuf buffer) {
                ctx.write(new DefaultHttpContent(buffer));
                continue;
            }
            if (obj instanceof byte[] byteArr) {
                ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(byteArr)));
                continue;
            }
            if (obj instanceof ChunkedInput<?> chunked) {
                ctx.write(new HttpChunkedInput((ChunkedInput<ByteBuf>) chunked));
                continue;
            }
            if (obj instanceof CharSequence sequence) {
                ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(sequence, charset)));
                continue;
            }
            if (obj instanceof File file) {
                if (!file.exists()) {
                    logger.warning("Non-existent File in response content, skipping: " + file.getAbsolutePath());
                    continue;
                }
                if (file.isDirectory()) {
                    logger.warning("Directory in response content, skipping");
                    continue;
                }
                if (IS_LINUX) {
                    ctx.write(new DefaultFileRegion(file, 0, file.length()));
                } else {
                    ctx.write(new HttpChunkedInput(new ChunkedFile(file)));
                }
                continue;
            }
            logger.warning("Unsupported response content, skipping: " + obj.getClass().getName());
        }
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    @SuppressWarnings("unchecked")
    private <T> void writeContent(IHeaderArgs args, Response.Content<T> content, ObjectArrayList<Object> output) throws IOException {
        HttpContentType<T> contentType = content.type();
        if (contentType instanceof INettyContentType nettyType) {
            ((INettyContentType<T>) nettyType).writeNetty(args, content.value(), output);
            return;
        }
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        contentType.write(args, outputStream, content.value());
        output.add(Unpooled.wrappedBuffer(outputStream.array, 0, outputStream.length));
    }

}
