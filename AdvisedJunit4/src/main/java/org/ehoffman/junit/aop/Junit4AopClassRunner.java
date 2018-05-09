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
package org.ehoffman.junit.aop;

import static org.ehoffman.advised.internal.AnnotationUtils.convertExceptionIfPossible;
import static org.ehoffman.advised.internal.AnnotationUtils.inspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.ehoffman.advised.ConstraintException;
import org.ehoffman.advised.ContextAwareMethodInvocation;
import org.ehoffman.advised.ObjectFactory;
import org.ehoffman.advised.internal.ProviderAwareObjectFactoryAggregate;
import org.ehoffman.advised.internal.TestContext;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class Junit4AopClassRunner extends BlockJUnit4ClassRunner {

  private static final TestContext CONTEXT = new TestContext();

  public Junit4AopClassRunner(final Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    // No Op
    // TODO: validate with the object factories?
  }

  @Override
  protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
    runContextualizedLeaf(method, notifier);
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    return getTestClass().getAnnotatedMethods(Test.class);
  }

  private final void runContextualizedLeaf(final FrameworkMethod frameworkMethod, final RunNotifier notifier) {
    final EachTestNotifier eachNotifier = new EachTestNotifier(notifier, describeChild(frameworkMethod));
    eachNotifier.fireTestStarted();
    Statement statement = methodBlock(frameworkMethod);
    ProviderAwareObjectFactoryAggregate registrar = new ProviderAwareObjectFactoryAggregate();
    for (Annotation annotation : inspect(frameworkMethod.getMethod().getAnnotations())) {
      MethodInterceptor advice = CONTEXT.getAdviceFor(annotation);
      if (advice != null) {
        statement = advise(statement, advice, frameworkMethod.getMethod(), registrar, annotation);
      }
    }
    try {
      statement.evaluate();
    } catch (final Throwable th) {
      final ConstraintException contraintException = convertExceptionIfPossible(th, ConstraintException.class);
      if (contraintException != null) {
        eachNotifier.addFailedAssumption(new AssumptionViolatedException(contraintException.getMessage(), contraintException));
      } else {
        eachNotifier.addFailure(th);
      }
    } finally {
      eachNotifier.fireTestFinished();
    }
  }

  private Statement advise(final Statement advised, final MethodInterceptor advisor, final Method method,
          final ProviderAwareObjectFactoryAggregate registry, final Annotation annotation) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        advisor.invoke(new ContextAwareMethodInvocation() {

          @Override
          public void registerObjectFactory(ObjectFactory factory) {
            registry.register(annotation, factory);

          }

          @Override
          public ObjectFactory getCurrentContextFactory() {
            return registry;
          }

          @Override
          public Object proceed() throws Throwable {
            if (method.getParameterTypes().length != 0 && InvokeMethod.class.isAssignableFrom(advised.getClass())) {
              Field testMethodField = advised.getClass().getDeclaredField("testMethod");
              testMethodField.setAccessible(true);
              FrameworkMethod fmethod = (FrameworkMethod) testMethodField.get(advised);
              Field targetField = advised.getClass().getDeclaredField("target");
              targetField.setAccessible(true);
              Object target = targetField.get(advised);
              fmethod.invokeExplosively(target, registry.getArgumentsFor(method));
            } else {
              advised.evaluate();
            }
            return null;
          }

          @Override
          public Object getThis() {
            return null;
          }

          @Override
          public AccessibleObject getStaticPart() {
            return null;
          }

          @Override
          public Object[] getArguments() {
            return new Object[] {};
          }

          @Override
          public Method getMethod() {
            return method;
          }

          @Override
          public Annotation getTargetAnnotation() {
            return annotation;
          }
        });
      }
    };
  }
}
