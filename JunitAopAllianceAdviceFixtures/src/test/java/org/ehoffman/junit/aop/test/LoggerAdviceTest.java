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

import java.util.ArrayList;
import java.util.List;

import org.ehoffman.junit.aop.Junit4AOPClassRunner;
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
        classes.add(Junit4AOPRunner.class);
        final Result result = JUnitCore.runClasses(classes.toArray(new Class[classes.size()]));
        for (final Failure f : result.getFailures()) {
            f.getException().printStackTrace();
            assertThat(f.getException().getMessage()).contains("Test Logs: \n[INFO] CAPTURED");
        }
    }

    @RunWith(Junit4AOPClassRunner.class)
    public static class Junit4AOPRunner {

        private static final String CAPTURED = "CAPTURED";

        private static class ILog {
            private static final Logger logger = LoggerFactory.getLogger(ILog.class);

            public static final void doSomething() {
                logger.info(CAPTURED);
            }
        }

        @Test
        @CaptureLogging
        public void simpleLoggerTest() {
            ILog.doSomething();
            throw new RuntimeException("Unwrapped Message");
        }

    }

}
