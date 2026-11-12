package me.lauriichan.snowframe.util.http.netty.server;

public interface IHttpConfig {

    static IHttpConfig DEFAULT = new IHttpConfig() {};

    default int maxLength() {
        return Integer.MAX_VALUE;
    }

    default boolean enableDecompression() {
        return true;
    }

    default int decompressionMaxLength() {
        // 1 GB default
        return 1024 * 1024 * 1024;
    }

    default boolean enableCompression() {
        return false;
    }

    default int compressionMinLength() {
        // 8 MB default
        return 1024 * 1024 * 8;
    }

}
