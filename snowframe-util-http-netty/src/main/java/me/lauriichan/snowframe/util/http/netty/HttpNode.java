package me.lauriichan.snowframe.util.http.netty;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;
import me.lauriichan.snowframe.util.http.server.Response;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public final class HttpNode {

    public static record Handler<T>(HttpContentType<T> contentType, INettyHttpHandler<T> handler) {

        @SuppressWarnings("unchecked")
        public final Response handle(Channel channel, HttpHeaders headers, HttpEncoded query, HttpPath path, ByteBuf bodyBuffer) {
            if (handler instanceof IBodylessNettyHttpHandler<T> bodylessHandler) {
                return bodylessHandler.handle(channel, headers, query, path);
            }
            HttpData<T> body;
            try {
                T value;
                if (contentType instanceof INettyContentType nettyType) {
                    value = (T) nettyType.readNetty(headers, bodyBuffer);
                } else {
                    FastByteArrayInputStream inputStream = new FastByteArrayInputStream(bodyBuffer.array(),
                        bodyBuffer.arrayOffset() + bodyBuffer.readerIndex(), bodyBuffer.readableBytes());
                    value = contentType.read(headers, inputStream);
                }
                body = new HttpData<>(value, null);
            } catch (IOException exp) {
                body = new HttpData<>(null, exp);
            }
            return handler.handle(channel, headers, query, path, body);
        }

    }

    private static final Pattern PATH_PATTERN = Pattern
        .compile("\\/?(?<Segment>(?<Path>[a-z0-9\\-]+)(\\/\\{(?<Variable>[a-z0-9_]+)\\})?\\/?)");

    private final String segment;
    private final HttpNode root, parent;

    private final String id;
    private final AtomicReference<IVariableType> type = new AtomicReference<>();

    private final AtomicReference<Handler<?>> handler = new AtomicReference<>();

    private final Object2ObjectOpenHashMap<String, HttpNode> paths = new Object2ObjectOpenHashMap<>();

    public HttpNode() {
        this.root = this;
        this.parent = null;
        this.segment = "";
        this.id = null;
    }

    private HttpNode(HttpNode parent, String segment, String id) {
        this.root = parent.root;
        this.parent = parent;
        this.segment = segment;
        this.id = id;
    }

    public HttpNode root() {
        return root;
    }

    public HttpNode parent() {
        return parent;
    }

    public HttpNode add(String path) {
        if (path.startsWith("/") && parent != null) {
            return root.add(path);
        }
        Matcher matcher = PATH_PATTERN.matcher(normalize(path));
        HttpNode current = this;
        while (matcher.find()) {
            String segment = matcher.group("Path");
            String id = matcher.group("Variable");
            current = current.createNode(segment, id);
        }
        return current;
    }

    public void reset() {
        if (parent != null) {
            root.reset();
            return;
        }
        paths.clear();
        handler.set(null);
    }

    public void delete() {
        if (parent == null) {
            throw new IllegalStateException("Can not remove root node");
        }
        HttpNode node = parent.paths.get(segment);
        if (node != this) {
            throw new IllegalStateException("This node is not inside the parent anymore already");
        }
        parent.paths.remove(segment);
    }

    public HttpPath resolve(HttpHeaders headers, HttpEncoded encoded, String path) {
        path = normalize(path);
        if (!path.isEmpty() && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (!path.isEmpty() && path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return new HttpPath(this, Object2ObjectMaps.emptyMap(), true);
        }
        String[] segments = path.split("/");
        HttpPath.Builder builder = new HttpPath.Builder();
        HttpNode node = this, next;
        String varId;
        IVariableType type;
        for (int i = 0; i < segments.length; i++) {
            varId = node.varId();
            type = node.varType();
            if (varId != null && type != null) {
                builder.var(varId, type.resolve(headers, encoded, segments[i++]));
            }
            if (i >= segments.length) {
                return builder.done(node, true);
            }
            next = node.paths.get(segments[i]);
            if (next == null) {
                return builder.done(node, false);
            }
            node = next;
        }
        return builder.done(node, true);
    }

    public String path() {
        StringBuilder builder = new StringBuilder();
        HttpNode node = this;
        while (!node.segment.isEmpty()) {
            if (!builder.isEmpty()) {
                builder.insert(0, '/');
            }
            if (node.id != null) {
                builder.insert(0, '}').insert(0, node.id).insert(0, "/{");
            }
            builder.insert(0, node.segment);
            node = node.parent;
        }
        return builder.toString();
    }

    public String segment() {
        return segment;
    }

    public String varId() {
        return id;
    }

    public boolean varType(IVariableType type) {
        if (id == null) {
            return false;
        }
        if (type == null) {
            throw new IllegalArgumentException("Content type or handler can not be null");
        }
        return this.type.compareAndSet(null, type);
    }

    public IVariableType varType() {
        return this.type.get();
    }

    public boolean isVariable() {
        return id != null;
    }

    public boolean isVariableComplete() {
        return id == null || this.type.get() == null;
    }

    public <T> boolean handler(HttpContentType<T> contentType, INettyHttpHandler<T> handler) {
        if (contentType == null || handler == null) {
            throw new IllegalArgumentException("Content type or handler can not be null");
        }
        return this.handler.compareAndSet(null, new Handler<>(contentType, handler));
    }

    public Handler<?> handler() {
        return this.handler.get();
    }

    public boolean isHandled() {
        return this.handler.get() != null;
    }

    /*
     * Helper
     */

    private String normalize(String path) {
        if (!(path = path.toLowerCase()).matches("[a-z0-9/\\-{}]+")) {
            throw new IllegalArgumentException(
                "Unsupported characters contained in path '%s', has to match [a-z0-9/\\-{}]+".formatted(path));
        }
        return path.replaceAll("\\/{2,}", "/");
    }

    private HttpNode createNode(String name, String id) {
        HttpNode node = paths.get(name);
        if (node == null) {
            node = new HttpNode(this, name, id);
            paths.put(name, node);
        } else if (!Objects.equals(node.id, id)) {
            throw new IllegalArgumentException(
                "Unsupported path, Variable id of Node '%s' doesn't match provided id '%s'".formatted(name, id));
        }
        return node;
    }

}
