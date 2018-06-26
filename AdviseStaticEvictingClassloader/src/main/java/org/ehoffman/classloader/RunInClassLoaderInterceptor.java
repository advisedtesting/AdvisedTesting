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
package org.ehoffman.classloader;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.instrument.classloading.ShadowingClassLoader;

public class RunInClassLoaderInterceptor implements MethodInterceptor {

  private final ShadowingClassLoader classloader;
  
  public RunInClassLoaderInterceptor() {
    classloader = new ShadowingClassLoader(ShadowingClassLoader.class.getClassLoader(), true);
    //TODO: provide configuration mechanism, a class of a certain type?
    //Ahh!  a detector that could inspect the classpath and decide which to include.
    classloader.excludePackage("org.ehoffman.advised");
    classloader.excludePackage("org.ehoffman.junit.aop");
    classloader.excludePackage("org.ehoffman.aop.context");
    classloader.excludePackage("org.ehoffman.classloader");
    classloader.excludePackage("org.springframework");
    classloader.excludePackage("org.assertj");
    classloader.excludePackage("org.junit");
    classloader.excludeClass(MethodInterceptor.class.getName());
    classloader.excludeClass(Invocation.class.getName());
    classloader.addTransformer(new EvictingStaticTransformer());
  }
  
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      Thread.currentThread().setContextClassLoader(classloader);
      return invocation.proceed();
    } catch (InvocationTargetException ite) {
      throw ite.getCause();
    }
  }

}
