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
package org.advisedtesting.core;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import org.advisedtesting.core.internal.ProviderAwareObjectFactoryAggregate;
import org.advisedtesting.core.internal.SimpleObjectFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Test;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

public class ProviderAwareObjectFactoryAggregatorTests {

  @Test
  public void emptyProviderAwareObjectFactoryTest() {
    ProviderAwareObjectFactoryAggregate aggregator = new ProviderAwareObjectFactoryAggregate();
    assertThat(aggregator.getAllObjects(Object.class)).isEmpty();
    assertThat(aggregator.getObject("bob", Class.class)).isNull();
    assertThat(aggregator.getObject(null, "bob", Class.class)).isNull();
    assertThat(aggregator.getObject(Class.class)).isNull();
    assertThat(aggregator.getObject((Annotation) null, Class.class)).isNull();
    assertThat(aggregator.getAllObjects((Annotation) null, Class.class)).isNull();
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
  
  @Test
  public void providerAwareObjectFactoryTest() throws AnnotationFormatException {
    Map<String, Object> initial = new HashMap<>();
    initial.put("int", 1);
    SimpleObjectFactory factory = new SimpleObjectFactory(initial);
    ProviderAwareObjectFactoryAggregate aggregator = new ProviderAwareObjectFactoryAggregate();
    RightType rightTypeNoValue = TypeFactory.annotation(RightType.class, new HashMap<String, Object>());
    aggregator.register(rightTypeNoValue, factory);
    assertThat(aggregator.getAllObjects(Object.class)).hasSize(1);
    assertThat(aggregator.getAllObjects(CharSequence.class)).hasSize(0);
    assertThat(aggregator.getAllObjects(rightTypeNoValue, CharSequence.class)).hasSize(0);
    assertThat(aggregator.getAllObjects(rightTypeNoValue, Integer.class)).hasSize(1);
    assertThat(aggregator.getObject("bob", Class.class)).isNull();
    assertThat(aggregator.getObject(Class.class)).isNull();
    assertThat(aggregator.getObject(Integer.class)).isEqualTo(1);
    assertThat(aggregator.getObject(rightTypeNoValue, "bob", Integer.class)).isNull();
    assertThat(aggregator.getObject(rightTypeNoValue, "int", Integer.class)).isEqualTo(1);
    assertThat(aggregator.getObject(rightTypeNoValue, Integer.class)).isEqualTo(1);
    assertThat(aggregator.getObject(rightTypeNoValue, String.class)).isNull();
  }
  
  
}
