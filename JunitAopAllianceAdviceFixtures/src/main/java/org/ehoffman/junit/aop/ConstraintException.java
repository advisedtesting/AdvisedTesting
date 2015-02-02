package org.ehoffman.junit.aop;

public class ConstraintException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConstraintException() {
        super();
    }

    public ConstraintException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintException(String message) {
        super(message);
    }

    public ConstraintException(Throwable cause) {
        super(cause);
    }

}
