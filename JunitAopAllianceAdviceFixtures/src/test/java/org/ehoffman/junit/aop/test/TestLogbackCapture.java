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

import static org.assertj.core.api.Assertions.assertThat;

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
