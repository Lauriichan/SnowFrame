package me.lauriichan.snowframe.util.logger;

final class SysOutDelegate implements IDelegateLogger {

    SysOutDelegate() {
        if (IDelegateLogger.SYS_OUT != null) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warning(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void track(String message) {
        System.out.println(message);
    }

    @Override
    public void debug(String message) {
        System.out.println(message);
    }

}
