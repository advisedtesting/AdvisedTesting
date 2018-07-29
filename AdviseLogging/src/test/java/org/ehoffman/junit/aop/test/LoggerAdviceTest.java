/*
 * The MIT License
 * Copyright © 2016 Rex Hoffman
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.ehoffman.advised.logback.CaptureLogging;
import org.ehoffman.junit.aop.Junit4AopClassRunner;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerAdviceTest {

  private static final Logger logger = LoggerFactory.getLogger(LoggerAdviceTest.class);

  private static ThreadLocal<Boolean> shouldFail = new ThreadLocal<>();
  
  @Test
  public void runFailingTestVerifyMessageContainsLogs() {
    logger.debug("test");
    shouldFail.set(true);
    try {
      final Result result = JUnitCore.runClasses(new Class[] { Junit4AopRunner.class });
      assertThat(result.getFailures()).hasSize(1);
      Failure failure = result.getFailures().get(0);
      assertThat(failure.getException().getMessage()).contains("Test Logs: \n[INFO] CAPTURED");
    } finally {
      shouldFail.set(null);
    }
  }

  @RunWith(Junit4AopClassRunner.class)
  public static class Junit4AopRunner {

    private static final String CAPTURED = "CAPTURED";

    private static class ILog {
      private static final Logger logger = LoggerFactory.getLogger(ILog.class);

      public static final void doSomething() {
        logger.info(CAPTURED);
      }
    }

    @Test
    @CaptureLogging
    public void simpleLoggerTest2() {
      ILog.doSomething();
      if (shouldFail.get() != null && shouldFail.get()) {
        fail("This inner test should fail.");
      }
    }
    
    @Test
    @CaptureLogging
    public void simpleLoggerTest() {
      ILog.doSomething();     
    }
    
  }
}
