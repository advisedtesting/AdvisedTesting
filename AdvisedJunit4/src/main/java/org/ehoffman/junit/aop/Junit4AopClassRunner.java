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

import static org.ehoffman.advised.internal.AdviceAnnotationEvaluator.inspect;
import static org.ehoffman.advised.internal.ExceptionEvaluator.convertExceptionIfPossible;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInterceptor;
import org.ehoffman.advised.ConstraintException;
import org.ehoffman.advised.ContextAwareMethodInvocation;
import org.ehoffman.advised.ObjectFactory;
import org.ehoffman.advised.internal.ProviderAwareObjectFactoryAggregate;
import org.ehoffman.advised.internal.TestContext;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class Junit4AopClassRunner extends BlockJUnit4ClassRunner {

  private static final TestContext CONTEXT = new TestContext();
  private final Class<?> targetClass;
  
  public Junit4AopClassRunner(final Class<?> klass) throws InitializationError {
    super(klass);
    targetClass = klass;
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
    ProviderAwareObjectFactoryAggregate registrar = new ProviderAwareObjectFactoryAggregate();
    List<Annotation> annotations = adviceAnnotations(frameworkMethod);
    Collections.reverse(annotations);
    DelayedConstructionStatement delayedStatement = new DelayedConstructionStatement(frameworkMethod, targetClass, registrar);
    Statement statement = delayedStatement;
    for (Annotation annotation : annotations) {
      statement = new AdvisedStatement(statement, CONTEXT, registrar, annotation);
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

  private List<Annotation> adviceAnnotations(final FrameworkMethod frameworkMethod) {
    List<Annotation> annotations = new ArrayList<>();
    for (Annotation annotation : inspect(frameworkMethod.getMethod().getAnnotations())) {
      if (CONTEXT.isAdviceAnnotation(annotation)) {
        annotations.add(annotation);
      }
    }
    return annotations;
  }

  public static class AdvisedStatement extends Statement {

    private final Statement advised;
    private final TestContext context;
    private final ProviderAwareObjectFactoryAggregate registry;
    private final Annotation annotation;

    public AdvisedStatement(Statement advised, TestContext context, ProviderAwareObjectFactoryAggregate registry,
            Annotation annotation) {
      this.advised = advised;
      this.context = context;
      this.registry = registry;
      this.annotation = annotation;
    }

    public void evaluate() throws Throwable {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      MethodInterceptor advisor = context.getAdviceFor(annotation, classloader);
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
          advised.evaluate();
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
          return null;
        }

        @Override
        public Annotation getTargetAnnotation() {
          return annotation;
        }
      });
    }

  }
  
  /**
   * Get rules for this test object.
   * 
   * @param target
   *          the test case instance
   * @return a list of TestRules that should be applied when executing this test
   */
  @Override
  protected List<TestRule> getTestRules(Object target) {
    TestClass testClass = new TestClass(target.getClass());
    List<TestRule> result = testClass.getAnnotatedMethodValues(target, Rule.class, TestRule.class);

    result.addAll(testClass.getAnnotatedFieldValues(target, Rule.class, TestRule.class));

    return result;
  }

  public class DelayedConstructionStatement extends Statement {
    private final String testName;
    private final List<String> parameterTypes;
    private final String targetClass;
    private final ProviderAwareObjectFactoryAggregate registry;
    private boolean wrapped = false;
    private Statement wrappedStatement = null;

    public DelayedConstructionStatement(FrameworkMethod fmethod, Class<?> targetClass, ProviderAwareObjectFactoryAggregate registry) {
      this.testName = fmethod.getMethod().getName();
      this.parameterTypes = Arrays.asList(fmethod.getMethod().getParameterTypes()).stream().map(clazz -> clazz.getName())
              .collect(Collectors.toList());
      this.targetClass = targetClass.getName();
      this.registry = registry;
    }

    public void evaluate() throws Throwable {
      if (wrapped) {
        wrappedStatement.evaluate();
      } else {
        wrapped = true;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> targetClassInLoader = Class.forName(targetClass, true, loader);
        Object target = targetClassInLoader.newInstance();
        List<Class<?>> parameters = new ArrayList<>();
        for (String className : parameterTypes) {
          parameters.add(Class.forName(className, true, loader));
        }
        Method method = targetClassInLoader.getMethod(testName, parameters.toArray(new Class[] {}));
        FrameworkMethod fmethod = new FrameworkMethod(method);
        Statement newTarget = new IvokationMethodWithArguments(target, fmethod, registry.getArgumentsFor(method));
        //simulates standard statement processing by junit 4.
        newTarget = possiblyExpectingExceptions(fmethod, target, newTarget);
        newTarget = withPotentialTimeout(fmethod, target, newTarget);
        newTarget = withBefores(fmethod, target, newTarget);
        newTarget = withAfters(fmethod, target, newTarget);
        //newTarget = withRules(fmethod, target, newTarget);
        List<TestRule> testRules = getTestRules(target);
        newTarget = new RunRules(newTarget, testRules, describeChild(fmethod));
        newTarget.evaluate();
      }
    }
  }
  
  public static class IvokationMethodWithArguments extends Statement {

    private final Object target;
    private final FrameworkMethod method;
    private final Object[] arguments;
    
    
    
    public IvokationMethodWithArguments(Object target, FrameworkMethod method, Object[] arguments) {
      super();
      this.target = target;
      this.method = method;
      this.arguments = arguments;
    }

    @Override
    public void evaluate() throws Throwable {
      method.invokeExplosively(target, arguments);
    }
    
  }
}
