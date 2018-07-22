/*
 * Copyright © 2016, Rex Hoffman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ehoffman.advised.internal;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;

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
    Class<?> implementedBy() default Object.class;
  }
  
  @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface RightType {
    Class<? extends MethodInterceptor> implementedBy() default MethodInterceptor.class;
  }

  @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface Container {
    WrongType[] list() default {};
    RightType single() default @RightType;
  }
  
  
  
  public static class MethodItercepticator implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      System.out.println("did the thing");
      return invocation.proceed();
    }
  }
  
  
  @SuppressWarnings("serial")
  @Test
  public void testAnnotationInspection() throws IllegalAccessException, InstantiationException, AnnotationFormatException {
    WrongType wrongType = TypeFactory.annotation(WrongType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.inspect(wrongType)).isEmpty();
    RightType rightTypeNoValue = TypeFactory.annotation(RightType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.inspect(rightTypeNoValue)).isEmpty();
    RightType rightTypeRightValue = TypeFactory.annotation(RightType.class, 
            new HashMap<String, Object>() {{ put("implementedBy", MethodItercepticator.class);}});
    assertThat(AdviceAnnotationEvaluator.inspect(rightTypeRightValue)).hasSize(1).contains(rightTypeRightValue);
    Container emptyContainer = TypeFactory.annotation(Container.class, new HashMap<>());
    assertThat(AdviceAnnotationEvaluator.inspect(emptyContainer)).hasSize(0);
    Container container = TypeFactory.annotation(Container.class,
            new HashMap<String, Object>() {{ put("single", rightTypeRightValue);}});
    assertThat(AdviceAnnotationEvaluator.inspect(container)).contains(rightTypeRightValue).hasSize(1);
  }
  
  @Test
  public void testAnnotationField() throws AnnotationFormatException {
    RightType rightTypeNoValue = TypeFactory.annotation(RightType.class, new HashMap<String, Object>());
    assertThat(AdviceAnnotationEvaluator.getNameIfPresent(rightTypeNoValue)).isNull();
    assertThat(AdviceAnnotationEvaluator.getInstanceIfPresent(rightTypeNoValue)).isNull();
  }
  
}
