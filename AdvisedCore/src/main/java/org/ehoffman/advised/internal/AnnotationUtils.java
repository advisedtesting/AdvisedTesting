/*
 * The MIT License
 * Copyright © 2015 Rex Hoffman
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

public class AnnotationUtils {
    
    public static boolean hasAdviceClass(final Annotation annotation) {
        try {
            Method method = annotation.annotationType().getMethod("IMPLEMENTED_BY");
            return method != null 
                   && Class.class.isAssignableFrom(method.getReturnType())
                   && MethodInterceptor.class.isAssignableFrom((Class<?>) method.invoke(annotation, (Object[]) null));
        } catch ( IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;    
        }
    }
    
    
    public static List<Annotation> inspect(Annotation... annotations) {
        List<Annotation> output = new ArrayList<>();
        for (Annotation annotation : annotations) {
            if (hasAdviceClass(annotation)) {
                output.add(annotation);
            } else {
                for (Method method : annotation.annotationType().getMethods()) {
                    if (Annotation.class.isAssignableFrom(method.getReturnType())) {
                        try {
                            output.addAll(inspect((Annotation)method.invoke(annotation, (Object[])null)));
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            // TODO Auto-generated catch block
                        }
                    }
                    if (method.getReturnType().getComponentType() != null 
                        && Annotation.class.isAssignableFrom(method.getReturnType().getComponentType())) {
                        try {
                            output.addAll(inspect((Annotation[]) method.invoke(annotation, (Object[]) null)));
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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
