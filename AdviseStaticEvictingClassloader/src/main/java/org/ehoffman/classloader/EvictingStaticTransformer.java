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
   * @param className the class to verify, ignored.
   * @param classBeingRedefined ignored.
   * @param protectionDomain ignored.
   * @param classfileBuffer ignored.
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
