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
package org.ehoffman.classloader;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ehoffman.advised.ContextAwareMethodInvocation;

public class RunInClassLoaderInterceptor implements MethodInterceptor {

  private final Map<RestrictiveClassloader, ClassLoader> classloaderBySupplier 
      = new ConcurrentHashMap<>();

  
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (ContextAwareMethodInvocation.class.isAssignableFrom(invocation.getClass())) {
      ContextAwareMethodInvocation cinvocation = ((ContextAwareMethodInvocation) invocation);
      RestrictiveClassloader rc = (RestrictiveClassloader) cinvocation.getTargetAnnotation();
      Class<? extends  Supplier<Stream<String>>> supplierClass = rc.delegatingPackagesSupplier();
      Supplier<Stream<String>> packageSupplier = supplierClass.newInstance();
      ClassLoader targetClassLoader = classloaderBySupplier.computeIfAbsent(rc, targetClass -> {
        boolean warnOnly = rc.warnOnly() && InDeveloperEnvironment.inDev();
        EvictingStaticTransformer transformer = new EvictingStaticTransformer(warnOnly, rc.logStatics());
        return new EvictingClassLoader(packageSupplier.get().collect(Collectors.toList()),
                transformer, this.getClass().getClassLoader());
        /*
        SimpleInstrumentableClassLoader classloader 
            = new SimpleInstrumentableClassLoader(this.getClass().getClassLoader());
        classloader.addTransformer(new EvictingStaticTransformer(warnOnly, rc.logStatics()));
        packageSupplier.get().forEach(packageName -> classloader.excludePackage(packageName));
        return classloader; */
      });
      try {
        Thread.currentThread().setContextClassLoader(targetClassLoader);
        return invocation.proceed();
      } catch (InvocationTargetException ite) {
        throw ite.getCause();
      } finally {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      }
    } else {
      throw new IllegalStateException(
              "This MethodInterceptor must be passed an instance of " + RestrictiveClassloader.class.getName());
    }
  }
}
