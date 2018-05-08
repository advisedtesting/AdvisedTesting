/*
 * Copyright © 2016, Saleforce.com, Inc
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
package org.ehoffman.junit.aop.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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

  @Test
  public void runFailingTestVerifyMessageContainsLogs() {
    logger.debug("test");
    final List<Class<?>> classes = new ArrayList<>();
    classes.add(Junit4AopRunner.class);
    final Result result = JUnitCore.runClasses(classes.toArray(new Class[classes.size()]));
    for (final Failure f : result.getFailures()) {
      f.getException().printStackTrace();
      assertThat(f.getException().getMessage()).contains("Test Logs: \n[INFO] CAPTURED");
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
    }
  }

}
