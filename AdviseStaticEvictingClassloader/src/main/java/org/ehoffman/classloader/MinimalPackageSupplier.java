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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.instrument.classloading.ShadowingClassLoader;

/**
 * Spring's {@link ShadowingClassLoader} will still use it's defaults {@link ShadowingClassLoader#DEFAULT_EXCLUDED_PACKAGES}.
 * The packages will not be evicted from classloading by the {@link RunInClassLoaderInterceptor}.
 * The covered classes will be loaded by the parent (URLClassLoader) provided by the jvm to junit.
 * @author rex
 */
public class MinimalPackageSupplier implements Supplier<Stream<String>> {

  @Override
  public Stream<String> get() {
    return Stream.of(
      "org.ehoffman.advised",
      "org.ehoffman.junit.aop",
      "org.ehoffman.aop.context",
      "org.ehoffman.classloader",
      "org.springframework",
      "org.assertj",
      "org.junit",
      "org.aopalliance");//it supplies the shadowing classloader....
  }

}
