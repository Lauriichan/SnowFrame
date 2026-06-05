package me.lauriichan.snowframe.resource.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public interface IDataSource {

    final IDataSource[] EMPTY = new IDataSource[0];

    /**
     * Check if the source exists
     * 
     * @return if the source exists or not
     */
    boolean exists();

    /**
     * Checks if the source is a resource
     * 
     * @return if the source is a resource
     */
    default boolean isResource() {
        return false;
    }

    /**
     * Checks if the source is a container
     * 
     * @return if the source is a container
     */
    default boolean isContainer() {
        return false;
    }

    /**
     * Gets the contents of this container
     * 
     * @return the contents of the container
     */
    default IDataSource[] getContents() {
        return EMPTY;
    }

    /**
     * Get the source object
     * 
     * @return the source object
     */
    Object getSource();

    /**
     * Gets the relative path of this object
     * 
     * @return the relative path
     */
    String getPath();

    /**
     * Gets the name of the source target
     * 
     * @return the name
     */
    default String name() {
        throw new UnsupportedOperationException();
    }

    /**
     * Resolves a source relative to this source
     * 
     * @param  path
     * 
     * @return      the relative source
     */
    default IDataSource resolve(String path) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the time that the source was last modified at
     * 
     * @return the time in ms
     */
    default long lastModified() {
        return -1L;
    }

    /**
     * Gets the size of the source in bytes
     * 
     * @return the size of the source in bytes
     */
    default long size() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the data source can be written to
     * 
     * @return @{code true} if the source can be written to otherwise @{code false}
     */
    default boolean isWritable() {
        return false;
    }

    /**
     * Tries to create the data source as container
     * 
     * @throws IOException if an I/O error occurs (or if the source exists as
     *                     resource already)
     */
    default void createAsContainer() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes the data source target
     * 
     * @throws IOException if an I/O error occurs
     */
    default void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Open a writable stream for the source
     * 
     * @return             the output stream
     * 
     * @throws IOException if an I/O error occurs
     */
    default OutputStream openWritableStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Open a buffered writer for the source
     * 
     * @return             the buffered writer
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedWriter openWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(openWritableStream()));
    }

    /**
     * Open a buffered writer for the source
     * 
     * @param  charset     the charset to use
     * 
     * @return             the buffered writer
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedWriter openWriter(Charset charset) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(openWritableStream(), charset));
    }

    /**
     * Checks if the data source can be read from
     * 
     * @return @{code true} if the source can be read from otherwise @{code false}
     */
    default boolean isReadable() {
        return false;
    }

    /**
     * Open a readable stream for the source
     * 
     * @return             the input stream
     * 
     * @throws IOException if an I/O error occurs
     */
    default InputStream openReadableStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Open a buffered reader for the source
     * 
     * @return             the buffered reader
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedReader openReader() throws IOException {
        return new BufferedReader(new InputStreamReader(openReadableStream()));
    }

    /**
     * Open a buffered reader for the source
     * 
     * @param  charset     the charset to use
     * 
     * @return             the buffered reader
     * 
     * @throws IOException if an I/O error occurs
     */
    default BufferedReader openReader(Charset charset) throws IOException {
        return new BufferedReader(new InputStreamReader(openReadableStream(), charset));
    }

    /**
     * Copies the data of this data source to the target data source.
     * 
     * Doesn't enforce the content in the target source to 100% match the source.
     * 
     * @param  targetSource the target source
     * 
     * @throws IOException  if an I/O error occurs
     */
    default void transferTo(IDataSource targetSource) throws IOException {
        transferTo(targetSource, false);
    }

    /**
     * Copies the data of this data source to the target data source
     * 
     * @param  targetSource       the target source
     * @param  enforceSameContent if the content between both source should be same,
     *                            no more no less.
     * 
     * @throws IOException        if an I/O error occurs
     */
    default void transferTo(IDataSource targetSource, boolean enforceSameContent) throws IOException {
        if (!exists()) {
            throw new IOException("Source data source doesn't exist: " + getPath());
        }
        if (!isContainer()) {
            try (InputStream inputStream = openReadableStream()) {
                try (OutputStream outputStream = targetSource.openWritableStream()) {
                    inputStream.transferTo(outputStream);
                }
            }
            return;
        }
        targetSource.createAsContainer();
        IDataSource[] contents = getContents();
        for (IDataSource child : contents) {
            child.transferTo(targetSource.resolve(child.name()));
        }
        if (!enforceSameContent) {
            return;
        }
        IDataSource[] targetContents = targetSource.getContents();
        if (contents.length == targetContents.length) {
            return;
        }
        for (IDataSource tmp1 : targetContents) {
            boolean found = false;
            for (IDataSource tmp2 : contents) {
                if (tmp1.name().equals(tmp2.name())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                tmp1.delete();
            }
        }
    }

    /**
     * Copies the data of this data source to the target stream
     * 
     * @param  targetStrean the target stream
     * 
     * @throws IOException  if an I/O error occurs
     */
    default void transferTo(OutputStream targetStream) throws IOException {
        try (InputStream inputStream = openReadableStream()) {
            inputStream.transferTo(targetStream);
        }
    }

}