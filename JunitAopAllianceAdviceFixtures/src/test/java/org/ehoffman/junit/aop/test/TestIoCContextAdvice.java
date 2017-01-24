package org.ehoffman.junit.aop.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehoffman.junit.aop.Junit4AOPClassRunner;
import org.ehoffman.junit.aop.test.AppConfiguration.TestBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

@RunWith(Junit4AOPClassRunner.class)
public class TestIoCContextAdvice {

    @Test
    @IoCContext(classes = { AppConfiguration.class })
    public void testTestIoCContext(TestBean bean) {
        assertThat(bean).isNotNull();
    }
    
    @Test
    @IoCContext(name = "bob", classes = { AppConfiguration.class })
    @IoCContext(name = "ted", classes = { AppConfiguration.class })
    public void testTestIoCContext2(ApplicationContext context, TestBean bean) {
        assertThat(context).isNotNull();
        assertThat(bean).isNotNull();
    }
    
    @Test
    @IoCContext(name = "bob", classes = { AppConfiguration.class })
    @IoCContext(name = "ted", classes = { AppConfiguration.class })
    public void testTestIoCContext3(@IoCContext(name = "bob") ApplicationContext context, 
                                    @IoCContext(name = "ted") ApplicationContext context2,
                                    @IoCContext(name = "bob") TestBean bean,
                                    @IoCContext(name = "ted") TestBean bean2,
                                    @IoCContext(name = "bob", instance = "bill") String bill,
                                    @IoCContext(name = "bob", instance = "ted") String ted,
                                    @IoCContext(name = "ted", instance = "bill") String tedsBill) {
        assertThat(context).isNotNull().hasSameClassAs(context2).isNotEqualTo(context2);
        assertThat(bean).isNotNull().hasSameClassAs(bean2).isNotEqualTo(bean2);
        assertThat(bill).isNotNull().isEqualTo("bill").isNotSameAs(tedsBill);
        assertThat(ted).isSameAs("ted");
    }
}
