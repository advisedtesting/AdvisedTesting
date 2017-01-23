package org.ehoffman.junit.aop.test;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TestBean.class)
public class AppConfiguration {

}
