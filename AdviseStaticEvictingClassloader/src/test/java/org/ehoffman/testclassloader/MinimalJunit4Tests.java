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
  @RestrictiveClassloader(delegatingPackagesSupplier = TestPackageSupplier.class)
  public void shoudlNotFailUsingAClassWithAStaticInit() throws IOException {
    try {
      assertThat(folder.newFolder()).isDirectory().canRead().canWrite();
      new StaticInitBlockClass();
    } catch (ClassFormatError cfe) {
      fail("Class should be loadable");
    }
  }
  
  @Test(expected = ClassFormatError.class)
  @RestrictiveClassloader(logStatics = true)
  public void logsInitBlock() {
    new StaticInitBlockClass();
  }
  
  @Test(expected = ClassFormatError.class)
  @RestrictiveClassloader(logStatics = true)
  public void logsNonLiteral() {
    new ContainsStaticUnsetVar();
  }
}
