package org.ehoffman.junit.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class Junit4AOPClassRunner extends BlockJUnit4ClassRunner {

    private static final TestContext CONTEXT = new TestContext();

    public Junit4AOPClassRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    public ConstraintException convertIfPossible(Throwable thowable) {
        if (ConstraintException.class.isAssignableFrom(thowable.getClass())) {
            return (ConstraintException) thowable;
        } else {
            if (thowable.getCause() == null) {
                return null;
            } else {
                return convertIfPossible(thowable.getCause());
            }
        }
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        runAdvisedLeaf(method, notifier);
    }

    /**
     * Runs a {@link Statement} that represents a leaf test.
     */
    protected final void runAdvisedLeaf(final FrameworkMethod frameworkMethod, final RunNotifier notifier) {
        final EachTestNotifier eachNotifier = new EachTestNotifier(notifier, describeChild(frameworkMethod));
        eachNotifier.fireTestStarted();
        Statement statement = methodBlock(frameworkMethod);
        for (Annotation annotation : frameworkMethod.getAnnotations()) {
            MethodInterceptor advice = CONTEXT.getAdviceFor(annotation);
            if (advice != null) {
                statement = advise(statement, advice, frameworkMethod.getMethod(), null);
            }
        }
        try {
            statement.evaluate();
        } catch (final Throwable e) {
            final ConstraintException contraintException = convertIfPossible(e);
            if (contraintException != null) {
                eachNotifier.addFailedAssumption(new AssumptionViolatedException(contraintException.getMessage(),
                                contraintException));
            } else {
                eachNotifier.addFailure(e);
            }
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    private Statement advise(final Statement advised, final MethodInterceptor advisor, final Method method,
        final Object testObject) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                advisor.invoke(new MethodInvocation() {
                    @Override
                    public Object proceed() throws Throwable {
                        advised.evaluate();
                        return null;
                    }

                    @Override
                    public Object getThis() {
                        return testObject;
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
                });
            }
        };
    }
}
