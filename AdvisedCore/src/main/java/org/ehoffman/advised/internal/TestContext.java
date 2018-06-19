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

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a context meant to be used with testing classes, which decouples the ability to add meaningful annotations to a test
 * method from the test runner. There are some incidental couplings to be aware of however. First the annotation must specify a
 * {@link MethodInterceptor} it wishes to advise a test. Second that {@link MethodInterceptor} must have a zero argument
 * constructor. Third any state the {@link MethodInterceptor} should be done so in a thread safe way. Either all in the zero length
 * argument constructor, or with synchronized logic. Fourth if a {@link MethodInterceptor} needs to tear down state it has
 * constructed, it should implement {@link Closeable} Fifth the marking {@link Annotation} should have a single parameter
 * implementedBy with a default value of the Class of the {@link MethodInterceptor} the author wishes to use.
 * 
 * @author rex
 */
public class TestContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestContext.class);
  private static final ConcurrentHashMap<Class<? extends MethodInterceptor>, MethodInterceptor> INTECEPTORCLASS_TO_INSTANCE = 
          new ConcurrentHashMap<Class<? extends MethodInterceptor>, MethodInterceptor>();
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * When the context is constructed, it is registered for destruction.
   */
  public TestContext() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        TestContext.this.close();
      }
    }));
  }

  /**
   * Build and cache, or retrieve, a {@link MethodInterceptor} associated with the annotationClass input, as specified, by
   * {@link TestContext} class documentation.
   * 
   * @param annotation
   *          the annotation who's related {@link MethodInterceptor} instance will be returned.
   * 
   * @return an advice instance singleton from the annotationClass's implementedBy parameter if any, and is constructible, or null.
   */
  public MethodInterceptor getAdviceFor(final Annotation annotation, ClassLoader classLoader) {
    if (closed.get()) {
      return null;
    } else {
      Class<MethodInterceptor> adviceClass = extractAdviceClass(annotation, classLoader);
      INTECEPTORCLASS_TO_INSTANCE.computeIfAbsent(adviceClass, a -> {
        return callZeroLengthConstructor(adviceClass);
      });
      return INTECEPTORCLASS_TO_INSTANCE.get(adviceClass);
    }
  }

  /**
   * Call all close methods implemented by any {@link MethodInterceptor} instances stored in local cache.
   */
  public void close() {
    if (!closed.getAndSet(true)) {
      for (final Object advice : INTECEPTORCLASS_TO_INSTANCE.values()) {
        if (Closeable.class.isAssignableFrom(advice.getClass())) {
          try {
            ((Closeable) advice).close();
          } catch (final IOException ex) {
            LOGGER.error("Error closing advice methods", ex);
          }
        }
      }
    }
  }
  
  public boolean isAdviceAnnotation(final Annotation annotation) {
    try {
      @SuppressWarnings("unchecked")
      Class<MethodInterceptor> interceptor = (Class<MethodInterceptor>) annotation.annotationType()
              .getMethod("implementedBy").invoke(annotation);
      return interceptor != null;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException ex) {
      return false;
    }
  }

  /**
   * Extract the advice class (implements {@link MethodInterceptor}) from the marking {@link Annotation}.
   * 
   * @param annotation
   *          Annotation instance to inspect for an implementedBy field
   * @return A {@link MethodInterceptor} instance of the type held by the IMPLEMENTED_BY field, or null.
   */
  @SuppressWarnings("unchecked")
  private Class<MethodInterceptor> extractAdviceClass(final Annotation annotation, ClassLoader classloader) {
    try {
      Class<MethodInterceptor> interceptor = (Class<MethodInterceptor>) annotation.annotationType()
               .getMethod("implementedBy").invoke(annotation);
      return (Class<MethodInterceptor>) classloader.loadClass(interceptor.getName());
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException | ClassNotFoundException ex) {
      LOGGER.info("Annotations of type " + annotation.annotationType().getSimpleName()
              + " do not have an usable implementedBy field (references a class that implements MethodInterceptor)");
    }
    return null;
  }

  private <T> T callZeroLengthConstructor(final Class<T> clazz) {
    if (clazz == null) {
      return null;
    }
    Constructor<T> constructor;
    try {
      constructor = clazz.getConstructor(new Class[] {});
      constructor.setAccessible(true);
      return constructor.newInstance(new Object[] {});
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException ex) {
      return null;
    }
  }

}
