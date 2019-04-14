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
package com.github.advisedtesting.classloader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aopalliance.intercept.MethodInterceptor;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestrictiveClassloader {

  /**
   * A class of type with a no arg constructor, that returns a stream of packages to delegate to the parent class loader.
   * No enforcement of statics will occur on the white listed classes.
   * @return the class representing the supplier.
   */
  Class<? extends Supplier<Stream<String>>>[] delegatingPackagesSuppliers() default MinimalPackageSupplier.class;
  
  /**
   * <p>
   * <b>THIS WILL ONLY BE HONORED IDES</b>, specifically Eclipse and Idea (others would be trivial to add).
   * </p>
   * <p>
   * If set to true all violating elements of a loaded class will be logged (java logger) as warnings, otherwise
   * classes will refuse to load.
   *</p>
   *<p>
   * Please note that the warning will only be logged once per class per test runner execution, not once per test.
   * </p>
   * <p>
   * The default is false.
   * </p>
   * @return true if we will not fail on violations, but only warn.
   */
  boolean warnOnly() default false;
  
  /**
   * If a prior class load (due to eviction) causes a NoClassDefFoundError linkage error, convert the 
   * message to display the issues with the evicted class.   The stack trace is unchanged.
   * @return true by default.
   */
  boolean transformLinkageErrors() default true;
  
  /**
   * {@link #implementedBy()} returns a Class that implements {@link org.aopalliance.intercept.MethodInterceptor}.
   * This field will be accessed via reflection so the name must be exact.  If the class also implements
   * {@link java.io.Closeable} the {@link java.io.Closeable#close()} method will be called at the close of the global context.
   * @return the {@link MethodInterceptor} used to build the context, should never need to be changed.
   */
  Class<? extends MethodInterceptor> implementedBy() default RunInClassLoaderInterceptor.class;
  
}
