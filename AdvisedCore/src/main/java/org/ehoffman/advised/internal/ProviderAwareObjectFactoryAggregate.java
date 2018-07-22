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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.ehoffman.advised.ObjectFactory;

public class ProviderAwareObjectFactoryAggregate implements ObjectFactory {

  // registrar capability, used to build the default object factory....
  private Map<Annotation, ObjectFactory> contexts = new LinkedHashMap<>();

  public void register(Annotation annotation, ObjectFactory objectFactory) {
    contexts.put(annotation, objectFactory);
  }

  @Override
  public <T> T getObject(Class<T> type) {
    for (Entry<Annotation, ObjectFactory> entry : contexts.entrySet()) {
      T found = entry.getValue().getObject(type);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @Override
  public <T> T getObject(String name, Class<T> type) {
    return contexts.entrySet().stream().map((entry) -> entry.getValue().getObject(name, type)).filter(o -> o != null).findFirst()
            .orElseGet(() -> null);
  }

  public <T> T getObject(Annotation annotation, Class<T> type) {
    return invokeOnFoundObjectFactory(annotation, o -> o.getObject(type));
  }

  public <T> T getObject(Annotation annotation, String name, Class<T> type) {
    return invokeOnFoundObjectFactory(annotation, o -> o.getObject(name, type));
  }

  public <T> Map<String, T> getAllObjects(Annotation annotation, Class<T> type) {
    return invokeOnFoundObjectFactory(annotation, o -> o.getAllObjects(type));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Map<String, T> getAllObjects(Class<T> type) {
    for (Entry<Annotation, ObjectFactory> context : contexts.entrySet()) {
      Map<String, T> matches = context.getValue().getAllObjects(type);
      if (matches != null && matches.size() > 0) {
        return matches;
      }
    }
    return Collections.unmodifiableMap(Collections.EMPTY_MAP);
  }

  public Object[] getArgumentsFor(Method method) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    Annotation[][] annotations = method.getParameterAnnotations();
    Object[] parameters = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameters[i] = getArgumentFor(parameterTypes[i], annotations[i]);
    }
    return parameters;
  }

  private Object getArgumentFor(Class<?> argumentType, Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      String requestedInstanceName = AdviceAnnotationEvaluator.getInstanceIfPresent(annotation);
      if (requestedInstanceName != null && !"".equals(requestedInstanceName)) {
        return invokeOnFoundObjectFactory(annotation, o -> o.getObject(requestedInstanceName, argumentType));
      }
      return invokeOnFoundObjectFactory(annotation, o -> o.getObject(argumentType));
    }
    return this.getObject(argumentType);
  }

  private <T, X> X invokeOnFoundObjectFactory(Annotation annotation, Function<ObjectFactory, X> function) {
    String desiredName = AdviceAnnotationEvaluator.getNameIfPresent(annotation);
    for (Entry<Annotation, ObjectFactory> context : contexts.entrySet()) {
      if (context.getKey().annotationType().isAssignableFrom(annotation.annotationType())
              && (desiredName == null || desiredName.equals(AdviceAnnotationEvaluator.getNameIfPresent(context.getKey())))) {
        return function.apply(context.getValue());
      }
    }
    return null;
  }


}
