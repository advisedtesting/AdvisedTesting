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
package org.ehoffman.aop.objectfactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.ehoffman.advised.ObjectFactory;

public class SimpleObjectFactory implements ObjectFactory {

  private final Map<String, Object> contents = new LinkedHashMap<>();

  public void put(String name, Object object) {
    contents.put(name, object);
  }

  public void putAll(Map<String, Object> additions) {
    contents.putAll(additions);
  }

  public SimpleObjectFactory() {
  }

  public SimpleObjectFactory(Map<String, Object> contents) {
    putAll(contents);
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
      throw new RuntimeException("Multiple objects from the ObjectFactory can satisfy that type, but we"
              + " are searching for a singleton. Matching names are " + possibleOutputs.keySet());
    }
    return null;
  }

  @Override
  public <T> T getObject(String name, Class<T> type) {
    @SuppressWarnings("unchecked")
    Map<String, ? extends T> possibleOutputs = (Map<String, ? extends T>) contents.entrySet().stream()
            .filter(o -> type.isAssignableFrom(o.getValue().getClass())).filter(o -> o.getKey().equals(name))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    if (possibleOutputs.size() == 1) {
      return possibleOutputs.values().iterator().next();
    }
    if (possibleOutputs.size() > 1) {
      throw new RuntimeException("Multiple objects from the ObjectFactory can satisfy that type, but we"
              + " are searching for a singleton. Matching names are " + possibleOutputs.keySet());
    }
    return null;
  }

  @Override
  public <T> Map<String, T> getAllObjects(Class<T> type) {
    @SuppressWarnings("unchecked")
    Map<String, T> output = (Map<String, T>) contents.entrySet().stream()
            .filter(o -> type.isAssignableFrom(o.getValue().getClass()))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    return output;
  }

}
