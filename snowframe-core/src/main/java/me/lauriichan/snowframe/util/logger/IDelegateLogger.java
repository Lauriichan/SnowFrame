package me.lauriichan.snowframe.util.logger;

public interface IDelegateLogger {
    
    final static IDelegateLogger SYS_OUT = new SysOutDelegate();
    
    default void custom(String message) {}

    void info(String message);

    void warning(String message);

    void error(String message);

    void track(String message);

    void debug(String message);

}
