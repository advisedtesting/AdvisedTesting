/*
 * The MIT License
 * Copyright © 2015 Rex Hoffman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        while ((factory == null || SubstituteLoggerFactory.class.isAssignableFrom(factory.getClass())) && count < 10) { 
            Thread.sleep(50);
            factory = LoggerFactory.getILoggerFactory();
            count++;
        }
    }
    
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        LOGGER.info("forcing logger to start");
        LogbackCapture.start();
        Object output = null;
        Throwable throwable = null;
        try {
            output = invocation.proceed();
        } catch (final Throwable t) {
            throwable = t;
        } finally {
            final String logging = LogbackCapture.stop();
            if (throwable != null) {
                throw new TestLoggingWithCause(logging, throwable);
            }
        }
        return output;
    }

}
