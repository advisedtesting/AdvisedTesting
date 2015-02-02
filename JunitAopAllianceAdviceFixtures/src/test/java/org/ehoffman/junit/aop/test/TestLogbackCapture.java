package org.ehoffman.junit.aop.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogbackCapture {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestLogbackCapture.class);
    private static final String CAPTURED = "CAPTURED";
    private static final String NOT_CAPTURED = "MISSED";
    
    private static class ILog {
        private static final Logger logger = LoggerFactory.getLogger(ILog.class);
        public final static void doSomething(){
            logger.info(CAPTURED);
        }
    }
    
    @Test
    public void simpleLoggerTest(){
        LOGGER.info(NOT_CAPTURED);
        LogbackCapture.start();
        ILog.doSomething();
        String logging = LogbackCapture.stop();
        LOGGER.info(NOT_CAPTURED);
        assertThat(logging).contains(CAPTURED).doesNotContain(NOT_CAPTURED);
    }

}