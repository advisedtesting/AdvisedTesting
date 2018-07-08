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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;
import java.util.List;

public class EvictingClassLoader extends ClassLoader {

  //Spring's shadowing classloader had this info...
  public static final String[] DEFAULT_EXCLUDED_PACKAGES =
          new String[] {"java.", "javax.", "sun.", "oracle.", "com.sun.", "com.ibm.", "COM.ibm.",
              "org.w3c.", "org.xml.", "org.dom4j.", "org.eclipse", "org.aspectj.", "net.sf.cglib",
              "org.springframework.cglib", "org.apache.xerces.", "org.apache.commons.logging."};
  
  private final List<String> whiteList;

  private final ClassFileTransformer transformer;

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
}
