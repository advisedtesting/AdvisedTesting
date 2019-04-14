/*
 * The MIT License
 * Copyright © 2016 AdvisedTesting
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
package com.github.advisedtesting.core.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

public class AdviceAnnotationEvaluator {

  private static boolean hasAdviceClass(final Annotation annotation) {
    Class<?> value = getValueIfPresent(annotation, "implementedBy", Class.class);
    try {
      return value != null 
              && MethodInterceptor.class.isAssignableFrom(value)
              && value.getConstructor() != null;
    } catch (NoSuchMethodException | SecurityException ex) {
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
        //get all fields on the annotation.
        for (Method method : annotation.annotationType().getMethods()) {
          method.setAccessible(true);
          //does this method return a single annotation
          if (Annotation.class.isAssignableFrom(method.getReturnType())) {
            try {
              output.addAll(inspect((Annotation) method.invoke(annotation, (Object[]) null)));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              // Should not be reachable
            }
          }
          //does this method return an array of annotations
          if (method.getReturnType().getComponentType() != null
                  && Annotation.class.isAssignableFrom(method.getReturnType().getComponentType())) {
            try {
              output.addAll(inspect((Annotation[]) method.invoke(annotation, (Object[]) null)));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
              // Should not be reachable
            }
          }
        }
      }
    }
    return output;
  }

  public static String getInstanceIfPresent(Annotation annotation) {
    String value = getValueIfPresent(annotation, "instance", String.class);
    if (!"__default".equals(value)) {
      return value;
    } else {
      return null;
    }
  }

  public static String getNameIfPresent(Annotation annotation) {
    String value = getValueIfPresent(annotation, "name", String.class);
    if (!"__default".equals(value)) {
      return value;
    } else {
      return null;
    }
  }

  /**
   * Finds the value of a field on an annotation.
   * 
   * @param <T> the type of the field.
   * @param annotation to inspect
   * @param name for a field with this name
   * @param output and a type of this Class
   * @return the field's value on the annotation if found.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getValueIfPresent(Annotation annotation, String name, Class<T> output) {
    if (annotation == null) {
      return null;
    }
    for (Method method : annotation.annotationType().getMethods()) {
      String methodName = method.getName();
      if (name.equals(methodName) && output.isAssignableFrom(method.getReturnType())) {
        try {
          method.setAccessible(true);
          return (T) method.invoke(annotation, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          // moving right along.
        }
      }
    }
    return null;
  }

}
