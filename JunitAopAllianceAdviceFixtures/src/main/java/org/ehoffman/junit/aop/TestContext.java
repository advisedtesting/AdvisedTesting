package org.ehoffman.junit.aop;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a context meant to be used with testing classes, which decouples the ability to add meaningful annotations
 * to a test method from the test runner.  There are some incidental couplings to be aware of however.
 * First the annotation must specify a {@link MethodInterceptor} it wishes to advise a test.
 * Second that {@link MethodInterceptor} must have a zero argument constructor.
 * Third any state the {@link MethodInterceptor} should be done so in a thread safe way. Either all in the zero length argument
 * constructor, or with synchronized logic.
 * Fourth if a {@link MethodInterceptor} needs to tear down state it has constructed, it should implement {@link Closeable}
 * Fifth the marking {@link Annotation} should have a single parameter IMPLEMENTED_BY with a default value of the Class of the
 * {@link MethodInterceptor} the author wishes to use.
 * 
 * @author rex
 */
public class TestContext {
    
    private final Logger LOGGER = LoggerFactory.getLogger(TestContext.class);
    private final Map<Class<? extends Annotation>, MethodInterceptor> ANNOTATION_CLASS_TO_ADVICE
            = Collections.synchronizedMap(new HashMap<Class<? extends Annotation>, MethodInterceptor>());
    private AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * when the context is constructed, it is registered for destruction.
     */
    TestContext() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                TestContext.this.close();
            }
        })); 
    }
    
    /**
     * Build and cache, or retrieve, a {@link MethodInterceptor} associated with the annotationClass input, as specified, by
     * {@link TestContext} class documentation.
     * 
     * @param annotationClass
     * @return an advice instance singleton from the annotationClass's IMPLEMENTED_BY parameter if any, and is constructible, or null.
     */
    public MethodInterceptor getAdviceFor(Annotation annotation) {
        if (closed.get()) {
            return null;
        } else {
            MethodInterceptor advice = ANNOTATION_CLASS_TO_ADVICE.get(annotation.annotationType());
            if (advice == null) {
               Class<MethodInterceptor> adviceClass = extractAdviceClass(annotation);
               if (advice == null && adviceClass != null) {
                 synchronized (ANNOTATION_CLASS_TO_ADVICE) {
                   if (ANNOTATION_CLASS_TO_ADVICE.get(annotation.annotationType()) == null) {
                     advice = callZeroLengthConstructor(adviceClass);
                     ANNOTATION_CLASS_TO_ADVICE.put(annotation.annotationType(), advice);
                   }
                 }               
               }
            }
            return advice;
        }
    }
    
    /**
     * Call all close methods implemented by any {@link MethodInterceptor} instances stored
     * in local cache.
     */
    public void close() {
        if (!closed.getAndSet(true)) {
            for (MethodInterceptor advice : ANNOTATION_CLASS_TO_ADVICE.values()) {
                if (Closeable.class.isAssignableFrom(advice.getClass())) {
                    try {
                       ((Closeable)advice).close();
                    } catch (IOException e) {
                        LOGGER.error("Error closing advice methods", e);
                    }
                }
            }
        }
    }
    
    /**
     * Extract the advice class (implements {@link MethodInterceptor}) from the marking {@link Annotation} 
     * @param clazz the 
     * @return 
     */
    @SuppressWarnings("unchecked")
    private Class<MethodInterceptor> extractAdviceClass(Annotation annotation) {
       try {
          return (Class<MethodInterceptor>) annotation.annotationType().getMethod("IMPLEMENTED_BY").invoke(annotation);
       } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          LOGGER.info("Annotations of type "+annotation.annotationType().getSimpleName()+" do not have an usable IMPLEMENTED_BY field (references a class that implements MethodInterceptor)");
       }
       return null;
    }
    
    private <T> T callZeroLengthConstructor(Class<T> clazz) {
        Constructor<T> constructor;
        try {
            constructor = clazz.getConstructor(new Class[]{});
            constructor.setAccessible(true);
            return constructor.newInstance(new Object[] {});
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }
        return null;
    }

   
}
