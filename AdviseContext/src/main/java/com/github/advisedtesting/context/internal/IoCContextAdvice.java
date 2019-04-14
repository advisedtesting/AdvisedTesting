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
package com.github.advisedtesting.context.internal;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.github.advisedtesting.context.IoCContext;
import com.github.advisedtesting.core.ContextAwareMethodInvocation;
import com.github.advisedtesting.core.ObjectFactory;

public class IoCContextAdvice implements MethodInterceptor {
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (ContextAwareMethodInvocation.class.isAssignableFrom(invocation.getClass())) {
      ContextAwareMethodInvocation cinvocation = ((ContextAwareMethodInvocation) invocation);
      ObjectFactory objectFactory =
              new SpringContextObjectFactory(sanitize(((IoCContext) cinvocation.getTargetAnnotation()).classes()));
      cinvocation.registerObjectFactory(objectFactory);
      return invocation.proceed();
    } else {
      throw new IllegalStateException(
              "This MethodInterceptor must be passed an instance of " + ContextAwareMethodInvocation.class.getName());
    }
  }
  
  private List<Class<?>> sanitize(Class<?>... classes) throws ClassNotFoundException {
    List<Class<?>> output = new ArrayList<>();
    for (Class<?> clazz : classes) {
      output.add(Thread.currentThread().getContextClassLoader().loadClass(clazz.getName()));
    }
    return output;
  }
  
}
