package com.nb.newstbot.exception;

/**
 * @author Nick Barban.
 */
public class InstadaException extends RuntimeException {
    public InstadaException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public InstadaException(String errorMessage) {
        super(errorMessage);
    }
}
