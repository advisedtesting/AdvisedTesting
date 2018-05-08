/*
 * Copyright © 2016, Saleforce.com, Inc
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
   * Names can be used to identify specific ObjectFactories for method injection on a test, if no name is specified, then a default
   * name will be used, and parameters of the test will fail if the reference an annotation with a name that does not exits.
   * 
   * @return the name used to look up entities in this context
   */
  String name() default "__default";

  /**
   * Only meaningful when used as Parameter annotation.
   * Specifies the name of an instance in a the object factory related to this annotation we desire to be passed to the test as
   * input at runtime.
   * 
   * @return the name of an instance in an {@link IoCContext}
   */
  String instance() default "__default";

  /**
   * Classes that define the context we wish to work with.
   * 
   * @return an array of classes used to build an IoCContext (the means of doing so is up to the {@link #objectFactoryClass()})
   */
  Class<?>[] classes() default {};

  /**
   * Classes of object factory we wish to use, if none is specified a discovery of object factories will ensue, if only one is
   * supported by the current classloader, it will be used.
   * 
   * @return the implementation of {@link ObjectFactory} we wish to use to build the context
   */
  Class<? extends ObjectFactory> objectFactoryClass() default ObjectFactory.class;

  /**
   * {@link #IMPLEMENTED_BY()} returns a Class that implements {@link org.aopalliance.intercept.MethodInterceptor}. This field will
   * be accessed via reflection so the name must be exact. If the class also implements {@link java.io.Closeable} the
   * {@link java.io.Closeable#close()} method will be called at the close of the global context.
   * 
   * @return the {@link MethodInterceptor} used to build the context, should never need to be changed.
   */
  Class<? extends MethodInterceptor> IMPLEMENTED_BY() default IoCContextAdvice.class;
}
