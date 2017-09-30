package io.gex.core.exception;

public class GexException extends Exception {

    public GexException() {
    }

    public GexException(String message) {
        super(message);
    }

    public GexException(Throwable exception) {
        super(exception);
    }

    public GexException(String message, Throwable exception) {
        super(message, exception);
    }
}
