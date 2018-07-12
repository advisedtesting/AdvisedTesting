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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.ehoffman.classloader.EvictingStaticTransformer;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

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
  
  public void transform(Boolean warnOnly, boolean log, Class<?> clazz) throws IOException {
    EvictingStaticTransformer transformer = new EvictingStaticTransformer(warnOnly, log);
    byte[] classBytes = getBytesOfClass(clazz);
    transformer.transform(Thread.currentThread().getContextClassLoader(), clazz.getName(), null, null, classBytes);
  }
  
  @Test
  public void testWarningAndLogMode() throws IOException {
    transform(true, true, ContainsStaticLiteralNonFinal.class);
    //verify that no exception is thrown.
    //need to capture log lines...
  }

  @Test
  public void testWarningNoLogMode() throws IOException {
    transform(true, false, ContainsStaticLiteralNonFinal.class);
    //verify that no exception is thrown.
  }

  @Test
  public void testFailNoLogMode() throws IOException {
    assertThatThrownBy(() -> 
       transform(false, false, ContainsStaticLiteralNonFinal.class)).isExactlyInstanceOf(ClassFormatError.class);
    //need to capture log lines...
  }
  
  @Test
  public void testFailLogMode() throws IOException {
    assertThatThrownBy(() -> 
       transform(false, true, ContainsStaticLiteralNonFinal.class)).isExactlyInstanceOf(ClassFormatError.class);
  }
}
