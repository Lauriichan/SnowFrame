package me.lauriichan.snowframe.resource;

import me.lauriichan.snowframe.ISnowFrameApp;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.resource.source.IDataSource;

public interface ISourceProvider<T extends ISnowFrameApp<T>> {

    /**
     * Provides a data source related to the path
     * 
     * @param  snowFrame the resource owner
     * @param  path      the path
     * 
     * @return           the data source
     */
    IDataSource provide(SnowFrame<T> snowFrame, String path);

}
