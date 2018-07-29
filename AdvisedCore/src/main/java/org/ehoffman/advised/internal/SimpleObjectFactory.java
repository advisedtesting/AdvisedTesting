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
package org.ehoffman.advised.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.ehoffman.advised.ObjectFactory;

public class SimpleObjectFactory implements ObjectFactory {

  private final Map<String, Object> contents = new LinkedHashMap<>();

  public SimpleObjectFactory() {
  }
  
  public SimpleObjectFactory(Map<String, Object> contexts) {
    contents.putAll(contexts);
  }

  public void add(String name, Object value) {
    contents.put(name, value);
  }

  @Override
  public <T> T getObject(Class<T> type) {
    @SuppressWarnings("unchecked")
    Map<String, ? extends T> possibleOutputs = (Map<String, ? extends T>) contents.entrySet().stream()
            .filter(o -> type.isAssignableFrom(o.getValue().getClass()))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    if (possibleOutputs.size() == 1) {
      return possibleOutputs.values().iterator().next();
    }
    if (possibleOutputs.size() > 1) {
      throw new IllegalStateException("Multiple objects from the ObjectFactory can satisfy that type, but we"
              + " are searching for a singleton. Matching names are " + possibleOutputs.keySet());
    }
    return null;
  }

  @Override
  public <T> T getObject(String name, Class<T> type) {
    return getAllObjects(type).get(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Map<String, T> getAllObjects(Class<T> type) {
    Map<String, T> output = new HashMap<>();
    contents.entrySet().forEach(e -> {
      if (e.getValue() != null
          && type.isAssignableFrom(e.getValue().getClass())) {
        output.put(e.getKey(), (T) e.getValue());
      }
    });
    return output;
  }
 
}
