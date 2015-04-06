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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.aopalliance.intercept.MethodInterceptor;

@Target({ METHOD, CONSTRUCTOR, FIELD })
@Retention(RUNTIME)
@Documented
public @interface CaptureLogging {
    
    /**
     * {@link #IMPLEMENTED_BY()} returns a Class that implements {@link org.aopalliance.intercept.MethodInterceptor}.
     * This field will be accessed via reflection so the name must be exact.  If the class also implements
     * {@link java.io.Closeable} the {@link java.io.Closeable#close()} method will be called at the close of the global context.
     * 
     */
    Class<? extends MethodInterceptor> IMPLEMENTED_BY() default LoggerAdvice.class;
}
