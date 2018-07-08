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
import java.util.ArrayList;

import org.ehoffman.aop.context.IoCContext;
import org.ehoffman.classloader.ClassContainsStaticInitialization;
import org.ehoffman.classloader.EvictingClassLoader;
import org.ehoffman.classloader.EvictingStaticTransformer;
import org.ehoffman.classloader.RestrictiveClassloader;
import org.ehoffman.junit.aop.Junit4AopClassRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import test.classloader.data.BadAppConfig;
import test.classloader.data.ContainsAssertion;
import test.classloader.data.ContainsStaticFinalLiteral;
import test.classloader.data.ContainsStaticFinalNonLiteral;
import test.classloader.data.ContainsStaticLiteralNonFinal;
import test.classloader.data.ContainsStaticUnsetVar;
import test.classloader.data.NestedContainsStaticNonFinalOrNonLiteral;
import test.classloader.data.StaticInitBlockClass;

@RunWith(Junit4AopClassRunner.class)
public class TestStaticInitializationEvictionJunit4 {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  @Test
  public void testClassContainsStaticInitializationPredicate() throws IOException {
    ClassContainsStaticInitialization asmScanner = new ClassContainsStaticInitialization();
    assertThat(asmScanner.test(ContainsStaticUnsetVar.class.getName()))
        .describedAs("Classes that contains statics should be evicted").isTrue(); 
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
    assertThat(asmScanner.test(ContainsAssertion.class.getName()))
        .describedAs("Classes with assertions are permitted").isFalse();
  }

  @Test
  public void testSimpleClassloaderChecks() throws ClassNotFoundException {
    EvictingClassLoader loader = new EvictingClassLoader(new ArrayList<>(), new EvictingStaticTransformer(),
            this.getClass().getClassLoader());
    loader.loadClass(TestStaticInitializationEvictionJunit4.class.getName());
    assertThatThrownBy(() -> loader.loadClass(ContainsStaticLiteralNonFinal.class.getName())).isInstanceOf(ClassFormatError.class);
  }

  @Test
  @RestrictiveClassloader
  @IoCContext(name = "bob", classes = { test.classloader.data.AppConfiguration.class })
  @IoCContext(name = "ted", classes = { test.classloader.data.AppConfiguration.class })
  public void testGoodContext(ApplicationContext context, test.classloader.data.AppConfiguration.TestBean bean) {
    assertThat(bean.getClass().getClassLoader().getClass().getName()).isEqualTo(EvictingClassLoader.class.getName());
    assertThat(context).isNotNull();
    assertThat(bean).isNotNull();
  }
  
  /**
   * <p>
   * Even a parameter like: @IoCContext(instance = "badApple") Object bean would trip up the classloader, just
   * earlier in the test execution than the expected exception handling could deal with.
   * </p>
   * <p> 
   * Usually devs would list "badApple" by type which would cause the test class to fail to load (for all tests
   * in the restrictive class loader), but it is possible that an interface could be used to get a bean, and that
   * the implementation would trip up the rules.
   * </p>
   */
  @Test(expected = BeanCreationException.class)
  @RestrictiveClassloader
  @IoCContext(name = "bob", classes = { BadAppConfig.class })
  public void testBadContext(ApplicationContext context) {
    assertThat(context.getBean("badApple")).isNotNull();
    fail("Context should not be reachable.");
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
  
}
