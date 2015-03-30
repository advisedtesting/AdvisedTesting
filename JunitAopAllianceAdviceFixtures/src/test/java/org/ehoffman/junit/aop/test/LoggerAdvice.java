package org.ehoffman.junit.aop.test;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLoggerFactory;

public class LoggerAdvice implements MethodInterceptor {
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerAdvice.class);
    
    public LoggerAdvice() throws InterruptedException {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        int count = 0;
        while ((factory == null || SubstituteLoggerFactory.class.isAssignableFrom(factory.getClass()))
                && count < 10) {
            Thread.sleep(50);
            factory = LoggerFactory.getILoggerFactory();
            count++;
        }
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        LOGGER.info("forcing logger to start");
        LogbackCapture.start();
        Object output = null;
        Throwable throwable = null;
        try {
            output = invocation.proceed();
        } catch (final Throwable t) {
            throwable = t;
        }
        finally {
            String logging = LogbackCapture.stop();
            if (throwable != null) {
                throw new TestLoggingWithCause(logging, throwable);
            }
        }
        return output;
    }

}
