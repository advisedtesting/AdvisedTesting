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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvictingClassLoader extends ClassLoader {

  //Spring's shadowing classloader had this info...
  public static final String[] DEFAULT_EXCLUDED_PACKAGES =
          new String[] {"java.", "javax.", "sun.", "oracle.", "com.sun.", "com.ibm.", "COM.ibm.",
              "org.w3c.", "org.xml.", "org.dom4j.", "org.eclipse", "org.aspectj.", "net.sf.cglib",
              "org.springframework.cglib", "org.apache.xerces.", "org.apache.commons.logging."};
  
  private final List<String> whiteList;

  private final ClassFileTransformer transformer;

  private final Map<String, String> classNameToError = new HashMap<>();
  
  public EvictingClassLoader(List<String> whiteList, ClassFileTransformer transformer, ClassLoader parent) {
    super(parent);
    this.whiteList = whiteList;
    whiteList.addAll(Arrays.asList(DEFAULT_EXCLUDED_PACKAGES));
    this.transformer = transformer;
  }

  

  
  private Class<?> getClass(String name) throws ClassNotFoundException {
    String file = name.replace('.', File.separatorChar) + ".class";
    byte[] bytes = null;
    try {
      bytes = loadClassData(file);
      try {
        transformer.transform(null, name, null, null, bytes);
      } catch (ClassFormatError error) {
        classNameToError.put(name, error.getMessage());
        throw error;
      } catch (IllegalClassFormatException icfe) {
        throw new ClassNotFoundException(name, icfe);
      }
      Class<?> loaded = super.findLoadedClass(name);
      if (loaded != null) {
        return loaded;
      } else {   
        Class<?> cl = defineClass(name, bytes, 0, bytes.length);
        resolveClass(cl);
        return cl;
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    }
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    boolean shouldLoad = true;
    for (String prefix : whiteList) {
      shouldLoad = shouldLoad && !name.startsWith(prefix);
    }
    if (shouldLoad) {
      return getClass(name);
    }
    return super.loadClass(name);
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    boolean shouldLoad = true;
    for (String prefix : whiteList) {
      shouldLoad = shouldLoad && !name.startsWith(prefix);
    }
    if (shouldLoad) {
      return getClass(name);
    }
    return super.loadClass(name, resolve);
  }
  
  /**
   * Loads a given file (presumably .class) into a byte array. The file should be accessible as a resource, for example it could be
   * located on the classpath.
   *
   * @param name
   *          File name to load
   * @return Byte array read from the file
   * @throws IOException
   *           Is thrown when there was some problem reading the file
   */
  private byte[] loadClassData(String name) throws IOException {
    InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
    int size = stream.available();
    byte[] buff = new byte[size];
    DataInputStream in = new DataInputStream(stream);
    in.readFully(buff);
    in.close();
    return buff;
  }
  
  /**
   * <p>
   * If this restrictive class loader didn't allow the class this will return the reason the class was evicted. 
   * </p>
   * <p> 
   * Useful for transforming NoClassDefFoundErrors in subsequent calls to a class that attempted to
   * link to a class that triggered a ClassFormatError in an imported class.
   * </p>
   * @param className the class we suspect was evicted.
   * @return errorMessage of eviction, or null.
   */
  public String getError(String className) {
    return classNameToError.get(className);
  }
}
