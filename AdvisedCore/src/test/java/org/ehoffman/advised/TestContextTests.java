/*
 * The MIT License
 * Copyright © 2016 Rex Hoffman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ehoffman.advised;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ehoffman.advised.internal.TestContext;
import org.junit.Test;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

public class TestContextTests {
  
  @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface RightType {
    /**
     * example.
     * @return example.
     */
    Class<? extends MethodInterceptor> implementedBy() default MethodInterceptor.class;
  }
  
  public static class MethodItercepticator implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      System.out.println("did the thing");
      return invocation.proceed();
    }
  }
  
  public static class CloseableMethodItercepticator implements MethodInterceptor, Closeable {
    
    boolean closed = false;
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      System.out.println("did the thing");
      return invocation.proceed();
    }

    @Override
    public void close() throws IOException {
      closed = true;
    }
  }
  
  private Map<String, Object> mapOf(String key, Object value) {
    Map<String, Object> output = new HashMap<>();
    output.put(key, value);
    return output;
  }
  
  @Test
  public void testContext() throws AnnotationFormatException {
    TestContext context = new TestContext();
    RightType rightTypeRightValue = TypeFactory.annotation(RightType.class, mapOf("implementedBy", MethodItercepticator.class));
    MethodInterceptor interceptor1 = context.getAdviceFor(rightTypeRightValue, Thread.currentThread().getContextClassLoader());
    assertThat(interceptor1).isNotNull().isInstanceOf(MethodItercepticator.class);
    MethodInterceptor interceptor2 = context.getAdviceFor(rightTypeRightValue, Thread.currentThread().getContextClassLoader());
    assertThat(interceptor1).isSameAs(interceptor2);
    assertThat(context.isAdviceAnnotation(rightTypeRightValue)).isTrue();
    
    RightType closeableAnnotation = TypeFactory.annotation(RightType.class,
            mapOf("implementedBy", CloseableMethodItercepticator.class));
    CloseableMethodItercepticator closeable = (CloseableMethodItercepticator) 
            context.getAdviceFor(closeableAnnotation, Thread.currentThread().getContextClassLoader());
    assertThat(closeable.closed).isFalse();
    context.close();
    assertThat(context.getAdviceFor(rightTypeRightValue, Thread.currentThread().getContextClassLoader())).isNull();
    assertThat(closeable.closed).isTrue();
  }

}
