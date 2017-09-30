package io.gex.core.exception;


public class GexAuthException extends GexException {

    public GexAuthException() {}

    public GexAuthException(String message) {
        super(message);
    }

    public GexAuthException(Throwable exception) { super(exception); }

    public GexAuthException(String message, Throwable exception) { super(message, exception); }

}
