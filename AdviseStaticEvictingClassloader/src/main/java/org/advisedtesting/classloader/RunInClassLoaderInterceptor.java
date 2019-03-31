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
package org.advisedtesting.classloader;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.advisedtesting.core.ContextAwareMethodInvocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class RunInClassLoaderInterceptor implements MethodInterceptor {

  private final Map<RestrictiveClassloader, EvictingClassLoader> classloaderBySupplier 
      = new ConcurrentHashMap<>();

  
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (ContextAwareMethodInvocation.class.isAssignableFrom(invocation.getClass())) {
      ContextAwareMethodInvocation cinvocation = ((ContextAwareMethodInvocation) invocation);
      RestrictiveClassloader rc = (RestrictiveClassloader) cinvocation.getTargetAnnotation();
      Supplier<Stream<String>> packageSupplier = convertToSingleSupplier(rc);
      EvictingClassLoader targetClassLoader = classloaderBySupplier.computeIfAbsent(rc, targetClass -> {
        boolean warnOnly = rc.warnOnly() && InDeveloperEnvironment.inDev();
        EvictingStaticTransformer transformer = new EvictingStaticTransformer(warnOnly);
        return new EvictingClassLoader(packageSupplier.get().collect(Collectors.toList()),
                transformer, this.getClass().getClassLoader());
      });
      try {
        Thread.currentThread().setContextClassLoader(targetClassLoader);
        return invocation.proceed();
      } catch (InvocationTargetException ite) {
        throw ite.getCause();
      } catch (NoClassDefFoundError | ClassFormatError er) {
        String name = er.getMessage().replace('/', '.');
        String errorMsg = targetClassLoader.getError(name);
        if (errorMsg != null) {
          NoClassDefFoundError error = new NoClassDefFoundError(errorMsg);
          error.setStackTrace(er.getStackTrace());
          throw error;
        } else {
          throw er;
        }
      } finally {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      }
    } else {
      throw new IllegalStateException(
              "This MethodInterceptor must be passed an instance of " + RestrictiveClassloader.class.getName());
    }
  }


  private Supplier<Stream<String>> convertToSingleSupplier(RestrictiveClassloader rc) {
    Class<? extends  Supplier<Stream<String>>>[] supplierClasses = rc.delegatingPackagesSuppliers();
    return () -> Stream.of(supplierClasses).map(s_class -> {
      try {
        return s_class.newInstance();
      } catch (InstantiationException | IllegalAccessException ex) {
        throw new IllegalArgumentException("Class could not be instantiated " + s_class, ex);
      }
    }).flatMap(s -> s.get());
  }
}
