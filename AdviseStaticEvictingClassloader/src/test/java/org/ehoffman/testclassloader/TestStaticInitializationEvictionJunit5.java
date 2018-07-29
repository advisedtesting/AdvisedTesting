/*
 * The MIT License
 * Copyright © 2016 Rex Hoffman
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
package org.ehoffman.testclassloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;

import org.ehoffman.classloader.ClassContainsStaticInitialization;
import org.ehoffman.classloader.EvictingStaticTransformer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.instrument.classloading.ShadowingClassLoader;

import test.classloader.data.ContainsStaticFinalLiteral;
import test.classloader.data.ContainsStaticFinalNonLiteral;
import test.classloader.data.ContainsStaticLiteralNonFinal;
import test.classloader.data.NestedContainsStaticNonFinalOrNonLiteral;
import test.classloader.data.StaticInitBlockClass;

class TestStaticInitializationEvictionJunit5 {

  @Test
  @DisplayName("Detect non final, and non literal statics in classes")
  public void testClassContainsStaticInitializationPredicate() throws IOException {
    ClassContainsStaticInitialization asmScanner = new ClassContainsStaticInitialization();
    assertAll(
        () -> assertThat(asmScanner.test(ContainsStaticLiteralNonFinal.class.getName()))
            .describedAs("Static literal non final fields should cause classes should be evicted").isTrue(),
        () -> assertThat(asmScanner.test(ContainsStaticFinalNonLiteral.class.getName()))
            .describedAs("Static final non literal fields should cause class to be evicted").isTrue(),
        () -> assertThat(asmScanner.test(StaticInitBlockClass.class.getName())).isTrue(),
        () -> assertThat(asmScanner.test(NestedContainsStaticNonFinalOrNonLiteral.Nested.class.getName())).isTrue(),
        () -> assertThat(asmScanner.test(ContainsStaticFinalLiteral.class.getName())).isFalse(),
        () -> assertThat(asmScanner.test(NestedContainsStaticNonFinalOrNonLiteral.class.getName())).isFalse(),
        () -> assertThat(asmScanner.test(TestStaticInitializationEvictionJunit5.class.getName())).isFalse());
  }

  @Test
  @DisplayName("Classloader refuses static non literal, non final, fields, and static initalization blocks.")
  public void testSimpleClassloaderChecks() throws ClassNotFoundException {
    ShadowingClassLoader loader = new ShadowingClassLoader(Thread.currentThread().getContextClassLoader());
    loader.addTransformer(new EvictingStaticTransformer());
    loader.loadClass(TestStaticInitializationEvictionJunit5.class.getName());
    assertThatThrownBy(() -> loader.loadClass(ContainsStaticLiteralNonFinal.class.getName())).isInstanceOf(ClassFormatError.class);
  }
  
}
