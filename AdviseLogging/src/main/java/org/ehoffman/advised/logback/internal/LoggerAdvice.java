/*
 * Copyright © 2016, Rex Hoffman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ehoffman.advised.logback.internal;

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
    } catch (final Throwable thr) {
      throwable = thr;
    } finally {
      final String logging = LogbackCapture.stop();
      if (throwable != null) {
        throw new TestLoggingWithCause(logging, throwable);
      }
    }
    return output;
  }

}
