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
  Class<? extends Supplier<Stream<String>>> delegatingPackagesSupplier() default MinimalPackageSupplier.class;
  
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
   * <p>
   * Log all static fields, static init blocks found on the class, default is false.
   * </p>
   * @return true if we will log, defaalts to false.
   */
  boolean logStatics() default false;
  
  /**
   * {@link #implementedBy()} returns a Class that implements {@link org.aopalliance.intercept.MethodInterceptor}.
   * This field will be accessed via reflection so the name must be exact.  If the class also implements
   * {@link java.io.Closeable} the {@link java.io.Closeable#close()} method will be called at the close of the global context.
   * @return the {@link MethodInterceptor} used to build the context, should never need to be changed.
   */
  Class<? extends MethodInterceptor> implementedBy() default RunInClassLoaderInterceptor.class;
  
}
