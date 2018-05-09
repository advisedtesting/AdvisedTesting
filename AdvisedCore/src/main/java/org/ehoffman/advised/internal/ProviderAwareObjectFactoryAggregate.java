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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
    return contexts.entrySet().stream().map((entry) -> entry.getValue().getObject(type)).filter(o -> o != null).findFirst()
            .orElseGet(() -> null);
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
      String requestedInstanceName = getInstanceIfPresent(annotation);
      if (requestedInstanceName != null && !"".equals(requestedInstanceName)) {
        return invokeOnFoundObjectFactory(annotation, o -> o.getObject(requestedInstanceName, argumentType));
      }
      return invokeOnFoundObjectFactory(annotation, o -> o.getObject(argumentType));
    }
    return this.getObject(argumentType);
  }

  private <T, X> X invokeOnFoundObjectFactory(Annotation annotation, Function<ObjectFactory, X> function) {
    String desiredName = getNameIfPresent(annotation);
    for (Entry<Annotation, ObjectFactory> context : contexts.entrySet()) {
      if (context.getKey().annotationType().isAssignableFrom(annotation.annotationType())
              && (desiredName == null || desiredName.equals(getNameIfPresent(context.getKey())))) {
        return function.apply(context.getValue());
      }
    }
    return null;
  }

  private String getInstanceIfPresent(Annotation annotation) {
    String value = getValueIfPresent(annotation, "instance", String.class);
    if (!"__default".equals(value)) {
      return value;
    } else {
      return null;
    }
  }

  private String getNameIfPresent(Annotation annotation) {
    String value = getValueIfPresent(annotation, "name", String.class);
    if (!"__default".equals(value)) {
      return value;
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getValueIfPresent(Annotation annotation, String name, Class<T> output) {
    for (Method method : annotation.annotationType().getMethods()) {
      String methodName = method.getName();
      if (name.equals(methodName) && output.isAssignableFrom(method.getReturnType())) {
        try {
          return (T) method.invoke(annotation, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          // moving right along.
        }
      }
    }
    return null;
  }

}
