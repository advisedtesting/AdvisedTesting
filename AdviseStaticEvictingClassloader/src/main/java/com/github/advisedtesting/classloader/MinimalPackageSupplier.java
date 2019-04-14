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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.instrument.classloading.ShadowingClassLoader;

/**
 * Spring's {@link ShadowingClassLoader} will still use it's defaults {@link ShadowingClassLoader#DEFAULT_EXCLUDED_PACKAGES}.
 * The packages will not be evicted from classloading by the {@link RunInClassLoaderInterceptor}.
 * The covered classes will be loaded by the parent (URLClassLoader) provided by the jvm to junit.
 * In any class loaded by junit itself, the class must be loaded from the parent classloader.
 * @author rex
 */
public class MinimalPackageSupplier implements Supplier<Stream<String>> {

  @Override
  public Stream<String> get() {
    return Stream.of(
      "com.github.advisedtesting.core",
      "com.github.advisedtesting.junit4",
      "com.github.advisedtesting.context",
      "com.github.advisedtesting.classloader",
      "org.springframework",
      "org.assertj",
      "org.junit",
      "org.aopalliance",
      "org.hamcrest",
      //for code coverage reports
      "org.jacoco");
  }

}
