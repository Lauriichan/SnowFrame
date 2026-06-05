package me.lauriichan.snowframe.util.logger;

final record CombinedDelegate(IDelegateLogger[] loggers) implements IDelegateLogger {

    @Override
    public void info(String message) {
        for (IDelegateLogger logger : loggers) {
            logger.info(message);
        }
    }

    @Override
    public void warning(String message) {
        for (IDelegateLogger logger : loggers) {
            logger.warning(message);
        }
    }

    @Override
    public void error(String message) {
        for (IDelegateLogger logger : loggers) {
            logger.error(message);
        }
    }

    @Override
    public void debug(String message) {
        for (IDelegateLogger logger : loggers) {
            logger.debug(message);
        }
    }

    @Override
    public void track(String message) {
        for (IDelegateLogger logger : loggers) {
            logger.track(message);
        }
    }

}
