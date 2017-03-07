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
package org.ehoffman.aop.context;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.aopalliance.intercept.MethodInterceptor;
import org.ehoffman.advised.ObjectFactory;
import org.ehoffman.aop.context.internal.IoCContextAdvice;

@Target({ METHOD, CONSTRUCTOR, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Repeatable(IoCContexts.class)
@SuppressWarnings("PMD.TooManyStaticImports")
public @interface IoCContext {
    
    /**
     * Names can be used to identify specific ObjectFactories for method injection on a test, if
     * no name is specified, then a default name will be used, and parameters of the test will fail
     * if the reference an annotation with a name that does not exits.
     * @return the name used to look up entities in this context
     */
    String name() default "__default";
    
    /**
     * Only meaningful when used as Parameter annotation.
     * 
     * Specifies the name of an instance in a the object factory related to this annotation we desire
     * to be passed to the test as input at runtime.
     * @return the name of an instance in an {@link IoCContext}
     */
    String instance() default "__default";
    
    /**
     * Classes that define the context we wish to work with.
     * @return an array of classes used to build an IoCContext (the means of doing so is up to the {@link #objectFactoryClass()})
     */
    Class<?>[] classes() default {};
    
    /**
     * Classes of object factory we wish to use, if none is specified a discovery of
     * object factories will ensue, if only one is supported by the current classloader, it will be used.
     * @return the implementation of {@link ObjectFactory} we wish to use to build the context
     */
    Class<? extends ObjectFactory> objectFactoryClass() default ObjectFactory.class;
    
    /**
     * {@link #IMPLEMENTED_BY()} returns a Class that implements {@link org.aopalliance.intercept.MethodInterceptor}.
     * This field will be accessed via reflection so the name must be exact.  If the class also implements
     * {@link java.io.Closeable} the {@link java.io.Closeable#close()} method will be called at the close of the global context.
     * @return the {@link MethodInterceptor} used to build the context, should never need to be changed.
     */
    Class<? extends MethodInterceptor> IMPLEMENTED_BY() default IoCContextAdvice.class;
}

