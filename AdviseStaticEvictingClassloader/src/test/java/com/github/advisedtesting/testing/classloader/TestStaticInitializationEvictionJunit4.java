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
package com.github.advisedtesting.testing.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import com.github.advisedtesting.classloader.ClassContainsStaticInitialization;
import com.github.advisedtesting.classloader.EvictingClassLoader;
import com.github.advisedtesting.classloader.EvictingStaticTransformer;
import com.github.advisedtesting.classloader.RestrictiveClassloader;
import com.github.advisedtesting.context.IoCContext;
import com.github.advisedtesting.junit4.Junit4AopClassRunner;

import test.classloader.data.BadAppConfig;
import test.classloader.data.ContainsAssertion;
import test.classloader.data.ContainsStaticFinalLiteral;
import test.classloader.data.ContainsStaticFinalNonLiteral;
import test.classloader.data.ContainsStaticLiteralNonFinal;
import test.classloader.data.NestedContainsStaticNonFinalOrNonLiteral;
import test.classloader.data.StaticInitBlockClass;

@RunWith(Junit4AopClassRunner.class)
public class TestStaticInitializationEvictionJunit4 {
  
  @Test
  public void testClassContainsStaticInitializationPredicate() throws IOException {
    ClassContainsStaticInitialization asmScanner = new ClassContainsStaticInitialization();
    assertThat(asmScanner.apply("test.classloader.data.ContainsStaticUnsetVar"))
        .describedAs("Classes that contains statics should be evicted")
        .contains("Disallowed static field with name \"object\" on class: test.classloader.data.ContainsStaticUnsetVar"); 
   
    assertThat(asmScanner.apply(ContainsStaticLiteralNonFinal.class.getName()))
        .describedAs("Static literal non final fields should cause classes should be evicted")
        .contains("Disallowed static field with name \"o\" on class: " + ContainsStaticLiteralNonFinal.class.getName())
        .contains("Disallowed <cinit> method (does more than enable the assert keyword) on class: "
                  + ContainsStaticLiteralNonFinal.class.getName()); 
    
    assertThat(asmScanner.apply(ContainsStaticFinalNonLiteral.class.getName()))
        .describedAs("Static non literal final fields should cause classes should be evicted")
        .contains("Disallowed static field with name \"o\" on class: " + ContainsStaticFinalNonLiteral.class.getName())
        .contains("Disallowed <cinit> method (does more than enable the assert keyword) on class: "
              + ContainsStaticFinalNonLiteral.class.getName()); 
    
    assertThat(asmScanner.apply(StaticInitBlockClass.class.getName()))
        .describedAs("Static init block should cause class to be evicted")
        .contains("Disallowed <cinit> method (does more than enable the assert keyword) on class: "
              + StaticInitBlockClass.class.getName()); 
    
    assertThat(asmScanner.apply(NestedContainsStaticNonFinalOrNonLiteral.Nested.class.getName()))
        .describedAs("Nested classes are evicted as well")
        .contains("Disallowed static field with name \"o\" on class: "
              + NestedContainsStaticNonFinalOrNonLiteral.Nested.class.getName())
        .contains("Disallowed <cinit> method (does more than enable the assert keyword) on class: "
              + NestedContainsStaticNonFinalOrNonLiteral.Nested.class.getName()); 
        
    assertThat(asmScanner.apply(ContainsStaticFinalLiteral.class.getName()))
        .describedAs("Static final literal containing classes are not evicted")
        .isEmpty();
    
    assertThat(asmScanner.apply(NestedContainsStaticNonFinalOrNonLiteral.class.getName()))
        .describedAs("Classes that contain bad nested classes are not prevented")
        .isEmpty();

    assertThat(asmScanner.apply(ContainsAssertion.class.getName()))
        .describedAs("Classes with assertions are permitted")
        .isEmpty();
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
  public void rewriteThrownExceptionWithCachedCause() {
    JUnitCore junit = new JUnitCore();
    Result result = junit.run(Embedded.class);
    assertThat(result.getFailures()).allMatch(f -> f.getMessage().contains("Disallowed <cinit> method"));
  }
  
  /**
   * See {@link TestStaticInitializationEvictionJunit4#rewriteThrownExceptionWithCachedCause()} above.
   */
  @RunWith(Junit4AopClassRunner.class)
  public static class Embedded {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    @RestrictiveClassloader
    public void shoudlFailUsingAClassWithAStaticInit() throws IOException {
      assertThat(folder.newFolder()).isDirectory().canRead().canWrite();
      try {
        Class.forName("test.classloader.data.StaticInitBlockClass");
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }
  
    @Test
    @RestrictiveClassloader
    public void shoudlFailThreeTimes() throws IOException {
      Runnable run = () -> { 
        new Object();
        try {
          Class.forName("test.classloader.data.StaticInitBlockClass");
        } catch (ClassNotFoundException ee) {
          ee.printStackTrace();
        }
      };
     
      assertThatThrownBy(() -> run.run())
          .isExactlyInstanceOf(ClassFormatError.class)
          .hasMessageContaining("Disallowed <cinit> method");
      //notice the different error message, I couldn't find an easy way around it.
      
      
      assertThatThrownBy(() -> run.run())
          .isExactlyInstanceOf(ClassFormatError.class)
          .hasMessageContaining("Disallowed <cinit> method");
      //so the @RestrictiveClassloader.implementedBy advice RunInClassLoaderInterceptor
      //translates the thrown exception and replays the error message.
      run.run();
    }
  }
}
