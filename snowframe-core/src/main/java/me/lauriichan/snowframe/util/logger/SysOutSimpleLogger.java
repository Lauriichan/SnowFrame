package me.lauriichan.snowframe.util.logger;

import me.lauriichan.laylib.logger.AbstractSimpleLogger;

public final class SysOutSimpleLogger extends AbstractSimpleLogger {
    
    public static SysOutSimpleLogger INSTANCE = new SysOutSimpleLogger();
    
    private SysOutSimpleLogger() {}

    @Override
    protected void info(String message) {
        System.out.println(message);
    }

    @Override
    protected void warning(String message) {
        System.out.println(message);
    }

    @Override
    protected void debug(String message) {
        System.out.println(message);
    }

    @Override
    protected void error(String message) {
        System.err.println(message);
    }

    @Override
    protected void track(String message) {
        System.err.println(message);
    }

}
