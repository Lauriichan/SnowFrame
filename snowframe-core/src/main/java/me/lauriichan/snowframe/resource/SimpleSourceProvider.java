package me.lauriichan.snowframe.resource;

import java.nio.file.Path;

import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.resource.source.IDataSource;
import me.lauriichan.snowframe.resource.source.PathDataSource;

final class SimpleSourceProvider<T extends ISnowFrameApp<T>> implements ISourceProvider<T> {

    private final Path basePath;

    public SimpleSourceProvider(final Path basePath) {
        this.basePath = basePath;
    }

    public Path basePath() {
        return basePath;
    }

    @Override
    public IDataSource provide(SnowFrame<T> snowFrame, String path) {
        return new PathDataSource(basePath.resolve(path));
    }

}
