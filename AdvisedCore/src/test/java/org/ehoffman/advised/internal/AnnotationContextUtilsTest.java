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
package org.ehoffman.advised.internal;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

public class AnnotationContextUtilsTest {

  @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface WrongType {
    /**
     * example.
     * @return example.
     */
    Class<?> implementedBy() default Object.class;
  }
  
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

  @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface Container {
    /**
     * example.
     * @return example.
     */
    WrongType[] list() default {};
    /**
     * example.
     * @return example.
     */
    RightType single() default @RightType;
  }
  
  public static class MethodItercepticator implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      System.out.println("did the thing");
      return invocation.proceed();
    }
  }

  private Map<String, Object> mapOf(String key, Object value) {
    Map<String, Object> output = new HashMap<>();
    output.put(key, value);
    return output;
  }

  @Test
  public void testAnnotationInspection() throws IllegalAccessException, InstantiationException, AnnotationFormatException {
    WrongType wrongType = TypeFactory.annotation(WrongType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.inspect(wrongType)).isEmpty();
    RightType rightTypeNoValue = TypeFactory.annotation(RightType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.inspect(rightTypeNoValue)).isEmpty();
    RightType rightTypeRightValue = TypeFactory.annotation(RightType.class, 
            mapOf("implementedBy", MethodItercepticator.class));
    assertThat(AdviceAnnotationEvaluator.inspect(rightTypeRightValue)).hasSize(1).contains(rightTypeRightValue);
    Container emptyContainer = TypeFactory.annotation(Container.class, new HashMap<>());
    assertThat(AdviceAnnotationEvaluator.inspect(emptyContainer)).hasSize(0);
    Container container = TypeFactory.annotation(Container.class,
            mapOf("single", rightTypeRightValue));
    assertThat(AdviceAnnotationEvaluator.inspect(container)).contains(rightTypeRightValue).hasSize(1);
  }
  
  @Test
  public void testAnnotationField() throws AnnotationFormatException {
    RightType rightTypeNoValue = TypeFactory.annotation(RightType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.getNameIfPresent(rightTypeNoValue)).isNull();
    assertThat(AdviceAnnotationEvaluator.getInstanceIfPresent(rightTypeNoValue)).isNull();
  }
  
}
