/*
 * #%L
 * JunitAopAllianceAdviceFixtures
 * %%
 * Copyright (C) 2015 Rex Hoffman
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
