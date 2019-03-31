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
package org.advisedtesting.classloader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

public class EvictingStaticTransformer implements ClassFileTransformer {

  private final boolean warnOnly;

  private final ClassContainsStaticInitialization asmScanner;
  
  public EvictingStaticTransformer() {
    this(false);
  }

  public EvictingStaticTransformer(boolean warnOnly) {
    this.warnOnly = warnOnly;
    this.asmScanner = new ClassContainsStaticInitialization();
  }


  /**
   * Potentially print warnings about static state, and potentially throw ClassFormatErrors when static state is found.
   * 
   * @param loader the parent class loader, ignored.
   * @param className the name of the class to verify.
   * @param classBeingRedefined ignored.
   * @param protectionDomain ignored.
   * @param classfileBuffer contains the bytes of a java class to be transformed.
   * @return always null, or a thrown {@link ClassFormatError}
   */
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
          ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    List<String> errors = asmScanner.apply(classfileBuffer);
    if (errors.size() > 0) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Static state found in class ")
            .append(className.replace('/', '.'));
      if (warnOnly) {   
        buffer.append(" in non-dev mode this will result in ClassFormatErrors\n");
      }
      int count = 0;
      for (String error : errors) {
        buffer.append("Error ").append(++count).append(" : ").append(error).append('\n');
      }
      if (warnOnly) {
        System.err.println(buffer);
      } else {
        throw new ClassFormatError(buffer.toString());
      }
    }
    return null;
  }

}
