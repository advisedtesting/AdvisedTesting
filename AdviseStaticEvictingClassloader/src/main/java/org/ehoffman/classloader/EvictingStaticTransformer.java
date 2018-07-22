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
package org.ehoffman.classloader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvictingStaticTransformer implements ClassFileTransformer {

  private final Logger logger = Logger.getLogger(EvictingStaticTransformer.class.getName());
  
  private final boolean warnOnly;

  private final boolean logErrors;

  
  private final ClassContainsStaticInitialization asmScanner;
  
  public EvictingStaticTransformer() {
    this(false, false);
  }

  public EvictingStaticTransformer(boolean warnOnly, boolean logErrors) {
    this.warnOnly = warnOnly;
    this.logErrors = logErrors;
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
    if (logErrors) {
      List<String> errors = asmScanner.apply(classfileBuffer);
      if (errors.size() > 0) {
        logger.log(Level.WARNING, "Static state found in class " + className.replace('/', '.')
                + " in non-dev mode this will result in ClassFormatErrors");
        for (String error : errors) {
          logger.log(Level.WARNING, error);
        }
        if (!warnOnly) {
          throw new ClassFormatError("Dissallowing Statics on class " + className.replace('/', '.'));
        }
      }
    } else {
      if (asmScanner.test(classfileBuffer)) {
        if (!warnOnly) {
          throw new ClassFormatError("Dissallowing Statics on class " + className.replace('/', '.'));
        } else {
          logger.log(Level.WARNING, "Static state found in class " + className.replace('/', '.') 
                  + " in logOnly mode this will result in ClassFormatErrors");
        }
      }
    }
    return null;
  }

}
