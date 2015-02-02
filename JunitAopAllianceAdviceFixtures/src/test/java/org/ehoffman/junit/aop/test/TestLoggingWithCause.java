package org.ehoffman.junit.aop.test;

public class TestLoggingWithCause extends RuntimeException {

    private static final long serialVersionUID = -2692785118677301561L;

    public TestLoggingWithCause(String logs, Throwable cause) {
        super("Test Logs: \n"+logs, cause);
    }

}
