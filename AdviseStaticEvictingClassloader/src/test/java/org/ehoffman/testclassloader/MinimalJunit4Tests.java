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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.ehoffman.classloader.EvictingStaticTransformer;
import org.ehoffman.classloader.RestrictiveClassloader;
import org.ehoffman.junit.aop.Junit4AopClassRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.instrument.classloading.ShadowingClassLoader;

import test.classloader.data.ContainsStaticLiteralNonFinal;
import test.classloader.data.ContainsStaticUnsetVar;
import test.classloader.data.StaticInitBlockClass;

@RunWith(Junit4AopClassRunner.class)
public class MinimalJunit4Tests {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  @Test
  public void testSimpleClassloaderChecks() throws ClassNotFoundException {
    ShadowingClassLoader loader = new ShadowingClassLoader(this.getClass().getClassLoader());
    loader.addTransformer(new EvictingStaticTransformer());
    loader.loadClass(TestStaticInitializationEvictionJunit4.class.getName());
    assertThatThrownBy(() -> loader.loadClass(ContainsStaticLiteralNonFinal.class.getName())).isInstanceOf(ClassFormatError.class);
  }
  
  @Test
  @RestrictiveClassloader
  public void shoudlFailUsingAClassWithAStaticInit() throws IOException {
    try {
      assertThat(folder.newFolder()).isDirectory().canRead().canWrite();
      new StaticInitBlockClass();
      fail("Class should not have been loadable.");
    } catch (ClassFormatError cfe) {
      System.out.println("yup. bad class. life is good.");
    }
  }
  
  
  @Test
  @RestrictiveClassloader(delegatingPackagesSuppliers = TestPackageSupplier.class)
  public void shoudlNotFailUsingAClassWithAStaticInit() throws IOException {
    try {
      assertThat(folder.newFolder()).isDirectory().canRead().canWrite();
      new StaticInitBlockClass();
    } catch (ClassFormatError cfe) {
      fail("Class should be loadable");
    }
  }
  
  @Test(expected = ClassFormatError.class)
  @RestrictiveClassloader
  public void logsInitBlock() {
    new StaticInitBlockClass();
  }
  
  @Test(expected = ClassFormatError.class)
  @RestrictiveClassloader
  public void logsNonLiteral() {
    new ContainsStaticUnsetVar();
  }
}
