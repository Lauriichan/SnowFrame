package me.lauriichan.snowframe.util.http.server;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.type.HttpContentType;

public class BasicHttpServer implements AutoCloseable {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String address = "localhost";
        private int port = 80, backlog = 0;

        private String sslKeyphrase;
        private Path sslKeystorePath;

        private Builder() {}

        public int backlog() {
            return backlog;
        }

        public Builder backlog(int backlog) {
            this.backlog = backlog;
            return this;
        }

        public String address() {
            return address;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public int port() {
            return port;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public String sslKeyphrase() {
            return sslKeyphrase;
        }

        public Builder sslKeyphrase(String sslKeyphrase) {
            this.sslKeyphrase = sslKeyphrase;
            return this;
        }

        public Path sslKeystorePath() {
            return sslKeystorePath;
        }

        public Builder sslKeystorePath(Path sslKeystorePath) {
            this.sslKeystorePath = sslKeystorePath;
            return this;
        }

        public BasicHttpServer build() throws Exception {
            return new BasicHttpServer(new InetSocketAddress(address, port), backlog, sslKeyphrase, sslKeystorePath);
        }

    }

    private final HttpServer server;
    private final Object2ObjectArrayMap<String, HttpContext> contexts = new Object2ObjectArrayMap<>();

    private volatile ISimpleLogger logger;
    private volatile boolean running = false;
    private volatile boolean ready = true;

    private volatile int delay = 5;

    private BasicHttpServer(InetSocketAddress address, int backlog, String sslKeyphrase, Path sslKeystorePath) throws Exception {
        HttpsServer tmp = null;
        if (sslKeyphrase != null && sslKeystorePath != null) {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            char[] password = sslKeyphrase.toCharArray();
            KeyStore store = KeyStore.getInstance("JKS");
            try (InputStream keystoreInput = sslKeystorePath.getFileSystem().provider().newInputStream(sslKeystorePath)) {
                store.load(keystoreInput, password);
            }
            KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
            keyManager.init(store, password);
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
            trustManager.init(store);

            sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
            tmp = HttpsServer.create();
            tmp.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        sslParameters.setNeedClientAuth(false);
                        sslParameters.setCipherSuites(engine.getEnabledCipherSuites());
                        sslParameters.setProtocols(engine.getEnabledProtocols());
                        
                        params.setSSLParameters(sslParameters);
                    } catch (Exception ex) {
                    }
                }
            });
        }
        server = tmp == null ? HttpServer.create() : tmp;
        server.bind(address, backlog);
    }

    public BasicHttpServer delay(int delay) {
        this.delay = Math.max(delay, 0);
        return this;
    }

    public int delay() {
        return delay;
    }

    public BasicHttpServer logger(ISimpleLogger logger) {
        this.logger = logger;
        return this;
    }

    public ISimpleLogger logger() {
        return logger;
    }

    public BasicHttpServer executor(Executor executor) {
        server.setExecutor(executor);
        return this;
    }

    public Executor executor() {
        return server.getExecutor();
    }

    public BasicHttpServer open() {
        if (running) {
            return this;
        }
        running = true;
        server.start();
        return this;
    }

    public boolean isRunning() {
        return running;
    }

    public <T> BasicHttpServer route(String path, HttpContentType<T> bodyType, IHttpSimpleHandler<T> handler, HttpMethod... methods) {
        return routeInternal(path, bodyType, handler, null, methods);
    }

    public <T> BasicHttpServer route(String path, HttpContentType<T> bodyType, IHttpSimpleHandler<T> handler, Consumer<HttpContext> contextSetup, HttpMethod... methods) {
        return routeInternal(path, bodyType, handler, contextSetup, methods);
    }

    public <T> BasicHttpServer route(String path, HttpContentType<T> bodyType, IHttpHandler<T> handler, HttpMethod... methods) {
        return routeInternal(path, bodyType, handler, null, methods);
    }

    public <T> BasicHttpServer route(String path, HttpContentType<T> bodyType, IHttpHandler<T> handler, Consumer<HttpContext> contextSetup, HttpMethod... methods) {
        return routeInternal(path, bodyType, handler, contextSetup, methods);
    }

    private <T> BasicHttpServer routeInternal(String path, HttpContentType<T> bodyType, IHttpHandler<T> handler, Consumer<HttpContext> contextSetup, HttpMethod[] methods) {
        if (!ready) {
            return this;
        }
        if (contexts.containsKey(path)) {
            server.removeContext(contexts.remove(path));
        }
        HttpContext context = server.createContext(path, new BasicHttpHandler<>(logger, bodyType, handler, methods));
        if (contextSetup != null) {
            contextSetup.accept(context);
        }
        contexts.put(path, context);
        return this;
    }

    public BasicHttpServer unrouteBase(String basePath) {
        if (!ready) {
            return this;
        }
        String[] routes = contexts.keySet().stream().filter(str -> str.startsWith(basePath)).toArray(String[]::new);
        if (routes.length == 0) {
            return this;
        }
        for (String route : routes) {
            server.removeContext(contexts.remove(route));
        }
        return this;
    }

    @Override
    public void close() {
        if (!running) {
            return;
        }
        ready = false;
        running = false;
        contexts.clear();
        server.stop(delay);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }
    }

}
