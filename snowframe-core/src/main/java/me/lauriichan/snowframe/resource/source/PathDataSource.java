package me.lauriichan.snowframe.resource.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public final class PathDataSource implements IDataSource {

    public static final PathDataSource[] EMPTY = new PathDataSource[0];

    private final Path path;

    public PathDataSource(final Path path) {
        this.path = path;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean isResource() {
        return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public boolean isContainer() {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public PathDataSource[] getContents() {
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return EMPTY;
        }
        try {
            PathDataSource[] output = Files.list(path).map(PathDataSource::new).toArray(PathDataSource[]::new);
            if (output.length == 0) {
                return EMPTY;
            }
            return output;
        } catch (IOException e) {
            return EMPTY;
        }
    }

    @Override
    public Path getSource() {
        return path;
    }

    @Override
    public String name() {
        Path tmp = path.getFileName();
        if (tmp == null) {
            return "";
        }
        return tmp.toString();
    }

    @Override
    public PathDataSource resolve(String path) {
        return new PathDataSource(this.path.resolve(path));
    }

    @Override
    public long lastModified() {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
        } catch (final IOException e) {
            return -1L;
        }
    }

    @Override
    public boolean isWritable() {
        return Files.isWritable(path);
    }

    @Override
    public void delete() throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            deleteDir(path);
            return;
        }
        Files.deleteIfExists(path);
    }

    private void deleteDir(Path path) throws IOException {
        Iterator<Path> iter = Files.list(path).iterator();
        Path child;
        while (iter.hasNext()) {
            child = iter.next();
            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                deleteDir(child);
                return;
            }
            Files.delete(child);
        }
        Files.delete(path);
    }

    @Override
    public OutputStream openWritableStream() throws IOException {
        if (exists() && !isWritable()) {
            throw new UnsupportedOperationException("Path can not be written to");
        }
        ensureCreated();
        return path.getFileSystem().provider().newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @Override
    public boolean isReadable() {
        return Files.isReadable(path);
    }

    @Override
    public InputStream openReadableStream() throws IOException {
        return path.getFileSystem().provider().newInputStream(path, StandardOpenOption.READ);
    }

    private void ensureCreated() throws IOException {
        if (!Files.exists(path)) {
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("PathSource[path=").append(path.toString()).append("]").toString();
    }

}