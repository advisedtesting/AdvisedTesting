package org.ehoffman.junit.aop.test;

import org.ehoffman.junit.aop.Junit4AOPClassRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Junit4AOPClassRunner.class)
public class Junit4AOPRunnerTest {
   
    private static final String CAPTURED = "CAPTURED";
    
    private static class ILog {
        private static final Logger logger = LoggerFactory.getLogger(ILog.class);
        public final static void doSomething(){
            logger.info(CAPTURED);
        }
    }
    
    @Test
    @Ignore
    @CaptureLogging
    public void simpleLoggerTest(){
        ILog.doSomething();
        throw new RuntimeException();
    }
    
}
