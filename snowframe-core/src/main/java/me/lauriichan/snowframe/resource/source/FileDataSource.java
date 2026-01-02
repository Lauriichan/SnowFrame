package me.lauriichan.snowframe.resource.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileDataSource implements IDataSource {

    public static final FileDataSource[] EMPTY = new FileDataSource[0];

    private final File file;

    public FileDataSource(final File file) {
        this.file = file;
    }

    @Override
    public boolean isResource() {
        return file.isFile();
    }

    @Override
    public boolean isContainer() {
        return file.isDirectory();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public FileDataSource[] getContents() {
        if (!file.isDirectory()) {
            return EMPTY;
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return EMPTY;
        }
        FileDataSource[] output = new FileDataSource[files.length];
        for (int i = 0; i < files.length; i++) {
            output[i] = new FileDataSource(files[i]);
        }
        return output;
    }

    @Override
    public File getSource() {
        return file;
    }

    @Override
    public String name() {
        return file.getName();
    }
    
    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }

    @Override
    public FileDataSource resolve(String path) {
        return new FileDataSource(new File(file, path));
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public boolean isWritable() {
        return file.isFile();
    }

    @Override
    public void delete() throws IOException {
        if (file.isDirectory()) {
            deleteDir(file);
            return;
        }
        if (file.exists()) {
            file.delete();
        }
    }

    private void deleteDir(File file) throws IOException {
        File[] files = file.listFiles();
        if (files != null && files.length != 0) {
            for (File child : files) {
                if (child.isDirectory()) {
                    deleteDir(child);
                    continue;
                }
                child.delete();
            }
        }
        file.delete();
    }

    @Override
    public FileOutputStream openWritableStream() throws IOException {
        ensureCreated();
        return new FileOutputStream(file);
    }

    @Override
    public boolean isReadable() {
        return file.isFile();
    }

    @Override
    public FileInputStream openReadableStream() throws IOException {
        return new FileInputStream(file);
    }

    private void ensureCreated() {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("FileSource[file=").append(file.getPath()).append("]").toString();
    }

}