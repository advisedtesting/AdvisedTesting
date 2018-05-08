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
