package org.ehoffman.junit.aop;

public class ConstraintException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConstraintException() {
        super();
    }

    public ConstraintException(final String message, final Throwable cause,
                    final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConstraintException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConstraintException(final String message) {
        super(message);
    }

    public ConstraintException(final Throwable cause) {
        super(cause);
    }

}
