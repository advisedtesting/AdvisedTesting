/*
 * Copyright © 2016, Saleforce.com, Inc
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
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

public class AnnotationUtils {

  public static boolean hasAdviceClass(final Annotation annotation) {
    try {
      Method method = annotation.annotationType().getMethod("implementedBy");
      return method != null && Class.class.isAssignableFrom(method.getReturnType())
              && MethodInterceptor.class.isAssignableFrom((Class<?>) method.invoke(annotation, (Object[]) null));
    } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      return false;
    }
  }

  /**
   * Get a list of annotations in order (based on input) that have implementedBy field that is a class.
   * implementedBy's class must implement {@link MethodInterceptor}.
   * @param annotations array of annotations.
   * @return List of Advice annotations meant for tests.
   */
  public static List<Annotation> inspect(Annotation... annotations) {
    List<Annotation> output = new ArrayList<>();
    for (Annotation annotation : annotations) {
      if (hasAdviceClass(annotation)) {
        output.add(annotation);
      } else {
        for (Method method : annotation.annotationType().getMethods()) {
          if (Annotation.class.isAssignableFrom(method.getReturnType())) {
            try {
              output.addAll(inspect((Annotation) method.invoke(annotation, (Object[]) null)));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              // TODO Auto-generated catch block
            }
          }
          if (method.getReturnType().getComponentType() != null
                  && Annotation.class.isAssignableFrom(method.getReturnType().getComponentType())) {
            try {
              output.addAll(inspect((Annotation[]) method.invoke(annotation, (Object[]) null)));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              // TODO Auto-generated catch block
            }
          }
        }
      }
    }
    return output;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Exception> T convertExceptionIfPossible(Throwable thowable, Class<T> exceptionType) {
    if (exceptionType.isAssignableFrom(thowable.getClass())) {
      return (T) thowable;
    } else {
      if (thowable.getCause() == null) {
        return null;
      } else {
        return convertExceptionIfPossible(thowable.getCause(), exceptionType);
      }
    }
  }

}
