package org.ehoffman.junit.aop.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean(name = "bill")
    public String getBill() {
        return new String("bi" + "ll"); //hack to force string to not be interned.
    }
    
    @Bean(name = "ted")
    public String getTed() {
        return "ted";
    }
    
    @Bean(name = "testBean")
    public TestBean testbean() {
        return new TestBean();
    }
    
    public static class TestBean {
    }

}
