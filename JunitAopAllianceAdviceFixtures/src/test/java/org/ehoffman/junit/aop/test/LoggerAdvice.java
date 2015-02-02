package org.ehoffman.junit.aop.test;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class LoggerAdvice implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
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
