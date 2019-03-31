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
package org.advisedtesting.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.advisedtesting.core.internal.SimpleObjectFactory;
import org.junit.Test;

public class SimpleObjectFactoryTests {

  @Test
  public void emptyProviderAwareObjectFactoryTest() {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    assertThat(factory.getAllObjects(Object.class)).isEmpty();
    assertThat(factory.getObject("bob", Class.class)).isNull();
    assertThat(factory.getObject(Class.class)).isNull();
  }
  
  @Test
  public void providerAwareObjectFactoryItemsTest() {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    String key = "Bob";
    String value = "Dobb";
    factory.add(key, value);
    assertThat(factory.getAllObjects(CharSequence.class)).containsKey(key).containsValue(value).hasSize(1);
    assertThat(factory.getObject("bob", Class.class)).isNull();
    assertThat(factory.getObject(Class.class)).isNull();
    assertThat(factory.getObject(String.class)).isEqualTo(value);
  }
  
  @Test
  public void providerAwareObjectFactoryTest() {
    Map<String, Object> initial = new HashMap<>();
    initial.put("int", 1);
    SimpleObjectFactory factory = new SimpleObjectFactory(initial);
    assertThat(factory.getAllObjects(CharSequence.class)).hasSize(0);
    assertThat(factory.getObject("bob", Class.class)).isNull();
    assertThat(factory.getObject(Class.class)).isNull();
    assertThat(factory.getObject(Integer.class)).isEqualTo(1);
  }
}
