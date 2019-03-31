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

/**
 * This needs refactored -- should be provided by junit runner, in the context.
 * @author rex
 */
public class InDeveloperEnvironment {
  
  /**
   * This is sloppy, should look for a class added in to the classpath to pass information back to the ide.
   * @return true if running in an ide.
   */
  public static boolean inDev() {
    boolean inEclipse = false;
    try {
      InDeveloperEnvironment.class.getClassLoader().loadClass("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner");
      inEclipse = true;
    } catch (ClassNotFoundException ex) {
      //standard failure.
    }
    return System.getProperty("java.class.path").contains("idea_rt")
            || inEclipse
            || System.getProperty("java.class.path").contains("org.eclipse.osgi");
  }

}
