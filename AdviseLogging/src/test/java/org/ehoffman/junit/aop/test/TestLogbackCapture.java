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

import org.ehoffman.advised.logback.internal.LogbackCapture;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLoggerFactory;

public class TestLogbackCapture {

  public TestLogbackCapture() throws InterruptedException {
    ILoggerFactory factory = LoggerFactory.getILoggerFactory();
    int count = 0;
    while ((factory == null || SubstituteLoggerFactory.class.isAssignableFrom(factory.getClass())) && count < 10) {
      Thread.sleep(50);
      factory = LoggerFactory.getILoggerFactory();
      count++;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(TestLogbackCapture.class);
  private static final String CAPTURED = "CAPTURED";
  private static final String NOT_CAPTURED = "MISSED";

  private static class ILog {
    private final Logger logger = LoggerFactory.getLogger(ILog.class);

    public final void doSomething() {
      logger.info(CAPTURED);
    }
  }

  @Test
  public void simpleLoggerTest() {
    LOGGER.info(NOT_CAPTURED);
    LogbackCapture.start();
    LogbackCapture.stop();
    LogbackCapture.start();
    final ILog log = new ILog();
    log.doSomething();
    final String logging = LogbackCapture.stop();
    LOGGER.info(NOT_CAPTURED);
    assertThat(logging).contains(CAPTURED).doesNotContain(NOT_CAPTURED);
  }

}
