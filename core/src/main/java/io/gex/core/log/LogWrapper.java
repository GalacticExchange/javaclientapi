package io.gex.core.log;

import io.gex.core.exception.GexAuthException;
import io.gex.core.exception.GexException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

public class LogWrapper extends ExtendedLoggerWrapper {

    private final ExtendedLoggerWrapper logger;
    private static final String FQCN = LogWrapper.class.getName();

    private LogWrapper(final Logger logger) {
        super((AbstractLogger) logger, logger.getName(), logger.getMessageFactory());
        this.logger = this;
    }

    public static LogWrapper create() {
        final Logger wrapped = LogManager.getLogger();
        return new LogWrapper(wrapped);
    }

    public static LogWrapper create(final Class<?> loggerName) {
        final Logger wrapped = LogManager.getLogger(loggerName);
        return new LogWrapper(wrapped);
    }

    public void logInfo(String message, LogType type) {
        logger.logIfEnabled(FQCN, Level.INFO, MarkerManager.getMarker(type.toString()), message);
    }

    public void logDebug(String message) {
        logger.logIfEnabled(FQCN, Level.DEBUG, MarkerManager.getMarker(LogType.DEBUG.toString()), message);
    }

    public void logDebug(String message, LogType type) {
        logger.logIfEnabled(FQCN, Level.DEBUG, MarkerManager.getMarker(type.toString()), message);
    }

    public void logWarn(String message, LogType type) {
        logger.logIfEnabled(FQCN, Level.WARN, MarkerManager.getMarker(type.toString()), message);
    }

    public void logWarn(String message, Throwable exception, LogType type) {
        logger.logIfEnabled(FQCN, Level.WARN, MarkerManager.getMarker(type.toString()), message, exception);
    }

    public void logError(String message, LogType type) {
        logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), message);
    }

    public void logError(Throwable exception, LogType type) {
        if (!(exception instanceof GexException)) {
            logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage(), exception);
        }
    }

    public void logError(String message, Throwable exception, LogType type) {
        if (!(exception instanceof GexException)) {
            logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), message, exception);
        }
    }

    public void logErrorForce(String message, Throwable exception, LogType type) {
        logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), message, exception);
    }

    public GexException logAndReturnException(String message, LogType type) {
        GexException exception = new GexException(message);
        logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage());
        return exception;
    }

    public GexException logAndReturnException(String message, Throwable exception, LogType type) {
        if (exception instanceof GexException) {
            return (GexException) exception;
        } else {
            logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage());
            return new GexException(message, exception);
        }
    }

    public GexException logAndReturnException(Throwable exception, LogType type) {
        if (exception instanceof GexException) {
            return (GexException) exception;
        } else {
            logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage());
            return new GexException(exception);
        }
    }

    public GexAuthException logAndReturnAuthException(String message, LogType type) {
        GexAuthException exception = new GexAuthException(message);
        logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage());
        return exception;
    }

    public GexAuthException logAndReturnAuthException(String message, Throwable exception, LogType type) {
        if (exception instanceof GexAuthException) {
            return (GexAuthException) exception;
        } else {
            logger.logIfEnabled(FQCN, Level.ERROR, MarkerManager.getMarker(type.toString()), exception.getMessage());
            return new GexAuthException(message, exception);
        }
    }

}
