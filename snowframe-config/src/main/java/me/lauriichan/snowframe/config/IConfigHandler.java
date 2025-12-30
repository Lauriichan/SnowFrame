package me.lauriichan.snowframe.config;

import me.lauriichan.snowframe.io.IOManager;
import me.lauriichan.snowframe.resource.source.IDataSource;

public interface IConfigHandler {

    void load(IOManager ioManager, Configuration configuration, IDataSource source, boolean onlyRaw) throws Exception;

    void save(IOManager ioManager, Configuration configuration, IDataSource source) throws Exception;

}
