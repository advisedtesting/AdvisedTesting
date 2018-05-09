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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.ehoffman.classloader.data.ContainsStaticFinalLiteral;
import org.ehoffman.classloader.data.ContainsStaticFinalNonLiteral;
import org.ehoffman.classloader.data.ContainsStaticLiteralNonFinal;
import org.ehoffman.classloader.data.NestedContainsStaticNonFinalOrNonLiteral;
import org.ehoffman.classloader.data.StaticInitBlockClass;
import org.ehoffman.junit.aop.Junit4AopClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.instrument.classloading.ShadowingClassLoader;

@RunWith(Junit4AopClassRunner.class)
public class TestStaticInitializationEvictionJunit4 {

  @Test
  public void testClassContainsStaticInitializationPredicate() throws IOException {
    ClassContainsStaticInitialization asmScanner = new ClassContainsStaticInitialization();
    assertThat(asmScanner.test(ContainsStaticLiteralNonFinal.class.getName()))
        .describedAs("Static literal non final fields should cause classes should be evicted").isTrue();
    assertThat(asmScanner.test(ContainsStaticFinalNonLiteral.class.getName()))
        .describedAs("Static final non literal fields should cause class to be evicted").isTrue();
    assertThat(asmScanner.test(StaticInitBlockClass.class.getName()))
        .describedAs("Static init block should cause class to be evicted").isTrue();
    assertThat(asmScanner.test(NestedContainsStaticNonFinalOrNonLiteral.Nested.class.getName()))
        .describedAs("Nested classes are evicted as well").isTrue();
    assertThat(asmScanner.test(ContainsStaticFinalLiteral.class.getName()))
        .describedAs("Static final literal containing classes are not evicted").isFalse();
    assertThat(asmScanner.test(NestedContainsStaticNonFinalOrNonLiteral.class.getName()))
        .describedAs("Classes that contain bad nested classes are not prevented").isFalse();
    assertThat(asmScanner.test(TestStaticInitializationEvictionJunit4.class.getName()))
        .describedAs("This class is also good").isFalse();
  }

  @Test
  public void testSimpleClassloaderChecks() throws ClassNotFoundException {
    ShadowingClassLoader loader = new ShadowingClassLoader(Thread.currentThread().getContextClassLoader());
    loader.addTransformer(new EvictingStaticTransformer());
    loader.loadClass(TestStaticInitializationEvictionJunit4.class.getName());
    assertThatThrownBy(() -> loader.loadClass(ContainsStaticLiteralNonFinal.class.getName())).isInstanceOf(ClassFormatError.class);
  }

  @Test
  @RestrictiveClassloader
  public void shoudlFailUsingAClassWithAStaticInit() {
    try {
      new StaticInitBlockClass();
      fail("Class should not have been loadable.");
    } catch (ClassFormatError cfe) {
      System.out.println("yup. bad class. life is good.");
    }
  }
  
}
