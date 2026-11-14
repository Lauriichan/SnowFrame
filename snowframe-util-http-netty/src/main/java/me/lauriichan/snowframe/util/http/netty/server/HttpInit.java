package me.lauriichan.snowframe.util.http.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.util.http.netty.HttpNode;

public final class HttpInit extends ChannelInitializer<SocketChannel> {

    private final ISimpleLogger logger;
    private final IHttpConfig config;
    private final HttpNode root;

    public HttpInit(ISimpleLogger logger, IHttpConfig config, HttpNode root) {
        this.logger = logger;
        this.config = config;
        this.root = root;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();
        
        // HTTP
        
        // Inbound
        pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
        if (config.enableDecompression()) {
            pipeline.addLast("http-request-decompressor", new HttpContentDecompressor(config.decompressionMaxLength()));
        }
        pipeline.addLast("http-request-aggregator", new HttpObjectAggregator(config.maxLength(), true));
        
        // Outbound
        if (config.enableCompression()) {
            pipeline.addLast("http-response-compressor", new HttpContentCompressor(config.compressionMinLength(), (CompressionOptions[]) null));
        }
        pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
        
        // Handler
        pipeline.addLast("http-router", new HttpRouter(logger, root));
    }

}
