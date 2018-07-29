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
package org.ehoffman.aop.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.ehoffman.advised.internal.SimpleObjectFactory;
import org.junit.Test;

public class TestSimpleObjectFactory {

  private static final String TEST_STRING3 = "testString3";
  private static final String TEST_STRING2 = "testString2";
  private static final String TEST_STRING1 = "testString1";
  private static final String TEST_LONG = "testLong";
  private static final String TEST_INT = "testInt";

  @Test
  public void getAndSetTests() {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    factory.add(TEST_INT, 1);
    factory.add(TEST_STRING1, "1");
    factory.add(TEST_LONG, 1L);
    assertThat(factory.getObject(Integer.class)).isEqualTo(1);
    assertThat(factory.getObject(String.class)).isEqualTo("1");
    assertThat(factory.getObject(Long.class)).isEqualTo(1L);
    assertThat(factory.getObject(Double.class)).isEqualTo(null);
  }

  @Test
  public void getAndSetMultipleTests() {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    factory.add(TEST_INT, 1);
    factory.add(TEST_STRING1, "1");
    factory.add(TEST_STRING2, "2");
    factory.add(TEST_STRING3, "3");
    factory.add(TEST_LONG, 1L);
    assertThat(factory.getObject(Integer.class)).isEqualTo(1);
    assertThat(factory.getAllObjects(String.class)).containsEntry(TEST_STRING1, "1").containsEntry(TEST_STRING2, "2")
            .containsEntry(TEST_STRING3, "3");
    assertThat(factory.getObject(Long.class)).isEqualTo(1L);

    try {
      factory.getObject(String.class);
      fail("Expected runtime exception due to multiple beans of same type");
    } catch (RuntimeException re) {
      assertThat(re.getMessage()).contains(TEST_STRING1).contains(TEST_STRING2).contains(TEST_STRING3);
    }
  }

}
