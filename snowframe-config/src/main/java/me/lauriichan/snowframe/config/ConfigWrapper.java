package me.lauriichan.snowframe.config;

import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.snowframe.ConfigModule;
import me.lauriichan.snowframe.IOModule;
import me.lauriichan.snowframe.SnowFrame;
import me.lauriichan.snowframe.io.IOManager;
import me.lauriichan.snowframe.resource.source.IDataSource;

public final class ConfigWrapper<T extends IConfig> implements IConfigWrapper<T> {

    public static final int SUCCESS = 0x00;
    public static final int SKIPPED = 0x01;

    public static final int FAIL_IO_LOAD = 0x11;
    public static final int FAIL_IO_SAVE = 0x12;

    public static final int FAIL_DATA_PROPERGATE = 0x21;
    public static final int FAIL_DATA_LOAD = 0x22;
    public static final int FAIL_DATA_SAVE = 0x23;
    public static final int FAIL_DATA_MIGRATE = 0x24;

    public static boolean isFailedState(final int state) {
        return state != SUCCESS && state != SKIPPED;
    }

    public static boolean isIOError(final int state) {
        return state == FAIL_IO_LOAD || state == FAIL_IO_SAVE;
    }

    public static boolean isDataError(final int state) {
        return state == FAIL_DATA_LOAD || state == FAIL_DATA_PROPERGATE || state == FAIL_DATA_MIGRATE || state == FAIL_DATA_SAVE;
    }

    public static <S extends ISingleConfigExtension> ConfigWrapper<S> single(final SnowFrame<?> snowFrame, final S extension) {
        return new ConfigWrapper<>(snowFrame, extension, extension.path());
    }

    private final ISimpleLogger logger;
    private final ConfigMigrator migrator;
    
    private final IOManager ioManager;

    private final String path;

    private final T config;
    private final Class<T> configType;

    private final IDataSource source;
    private final IConfigHandler handler;

    private volatile long lastTimeModified = -1L;

    @SuppressWarnings("unchecked")
    public ConfigWrapper(final SnowFrame<?> snowFrame, final T extension, final String path) {
        this.logger = snowFrame.logger();
        this.ioManager = snowFrame.module(IOModule.class).manager();
        this.migrator = snowFrame.module(ConfigModule.class).migrator();
        this.path = path;
        this.config = Objects.requireNonNull(extension, "Config extension can't be null");
        this.configType = (Class<T>) config.getClass();
        this.source = Objects.requireNonNull(snowFrame.resource(path), "Couldn't find data source at '" + path + "'");
        this.handler = Objects.requireNonNull(extension.handler(), "Config handler can't be null");
    }

    public T config() {
        return config;
    }

    public String path() {
        return path;
    }

    public IDataSource source() {
        return source;
    }

    public IConfigHandler handler() {
        return handler;
    }

    public long lastModified() {
        return lastTimeModified;
    }

    @Override
    public Class<T> configType() {
        return configType;
    }

    @Override
    public int[] reload(final boolean forceReload, final boolean wipeAfterLoad) {
        return new int[] {
            reloadSingle(forceReload, wipeAfterLoad)
        };
    }

    public int reloadSingle(final boolean forceReload, final boolean wipeAfterLoad) {
        final Configuration configuration = new Configuration();
        if (source.exists()) {
            if (!forceReload && lastTimeModified == source.lastModified() && !config.isModified()) {
                return SKIPPED;
            }
            configuration.clear();
            if (migrator != null) {
                try {
                    handler.load(ioManager, configuration, source, true);
                    lastTimeModified = source.lastModified();
                } catch (final Exception exception) {
                    logger.warning("Failed to load configuration from '{0}'!", exception, path);
                    return FAIL_IO_LOAD;
                }
                int version = configuration.getInt("version", 0);
                if (migrator.needsMigration(configType, version)) {
                    try {
                        int newVersion = migrator.migrate(logger, version, configuration, config);
                        configuration.set("version", newVersion);
                    } catch (ConfigMigrationFailedException exception) {
                        logger.warning("Failed to migrate configuration data of '{0}'!", exception, path);
                        return FAIL_DATA_MIGRATE;
                    }
                    try {
                        handler.save(ioManager, configuration, source);
                    } catch (final Exception exception) {
                        logger.warning("Failed to save migrated configuration to '{0}'!", exception, path);
                        return FAIL_IO_SAVE;
                    }
                }
            }
            try {
                handler.load(ioManager, configuration, source, false);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load configuration from '{0}'!", exception, path);
                return FAIL_IO_LOAD;
            }
        } else {
            try {
                config.onPropergate(logger, configuration);
            } catch (final Exception exception) {
                logger.warning("Failed to propergate configuration data of '{0}'!", exception, path);
                return FAIL_DATA_PROPERGATE;
            }
        }
        try {
            config.onLoad(logger, configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to load configuration data of '{0}'!", exception, path);
            return FAIL_DATA_LOAD;
        }
        if (wipeAfterLoad) {
            configuration.clear();
        }
        try {
            config.onSave(logger, configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            configuration.set("version", migrator.getTargetVersion(configType));
        }
        try {
            handler.save(ioManager, configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

    @Override
    public int[] save(boolean forceSave) {
        return new int[] {
            saveSingle(forceSave)
        };
    }

    public int saveSingle(final boolean force) {
        if (!force && !config.isModified() && source.exists()) {
            return SKIPPED;
        }
        final Configuration configuration = new Configuration();
        try {
            config.onSave(logger, configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            configuration.set("version", migrator.getTargetVersion(configType));
        }
        try {
            handler.save(ioManager, configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

}
