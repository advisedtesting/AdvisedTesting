package org.ehoffman.junit.aop.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehoffman.junit.aop.Junit4AOPClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Junit4AOPClassRunner.class)
public class TestIoCContextAdvice {

    @Test
    @IoCContext(classes = { AppConfiguration.class })
    public void testTestIoCContext(TestBean bean) {
        assertThat(bean).isNotNull();
    }
}
