package org.ehoffman.junit.aop.test;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, CONSTRUCTOR })
@Retention(RUNTIME)
@Documented
public @interface IoCContexts {

    /**
     * Multiple IoCContexts
     * @return
     */
    IoCContext[] value();
    
}
