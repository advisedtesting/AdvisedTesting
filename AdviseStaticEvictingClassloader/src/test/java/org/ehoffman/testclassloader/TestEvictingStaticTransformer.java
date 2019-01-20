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

import java.io.IOException;

import org.ehoffman.classloader.EvictingStaticTransformer;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import test.classloader.data.ContainsEnumerationSwitchStatement;
import test.classloader.data.ContainsStaticLiteralNonFinal;

public class TestEvictingStaticTransformer {

  /**
   * Doesn't work on embedded classes, I was a tad lazy.
   * @param clazz the class the transformer will analyze.
   * @return bytes representing class.
   * @throws IOException if the class file is not found, not readable, or an inner class's bytes.
   */
  private byte[] getBytesOfClass(Class<?> clazz) throws IOException {
    String resourceName = clazz.getName().replaceAll("\\.", "/") + ".class";
    return FileCopyUtils.copyToByteArray(this.getClass().getClassLoader().getResourceAsStream(resourceName));
  }
  
  public byte[] transform(Boolean warnOnly, Class<?> clazz) throws IOException {
    EvictingStaticTransformer transformer = new EvictingStaticTransformer(warnOnly);
    byte[] classBytes = getBytesOfClass(clazz);
    return transformer.transform(Thread.currentThread().getContextClassLoader(), clazz.getName(), null, null, classBytes);
  }
  
  @Test
  public void testWarningAndLogMode() throws IOException {
    assertThat(transform(true, ContainsStaticLiteralNonFinal.class)).isNull();
  }
  
  @Test
  public void testWontFailOnEnumSwitch() throws IOException {
    assertThat(transform(true, ContainsEnumerationSwitchStatement.class)).isNull();
  }
}
