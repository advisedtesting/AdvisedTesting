/*
 * Copyright © 2016, Saleforce.com, Inc
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
    factory.put(TEST_INT, 1);
    factory.put(TEST_STRING1, "1");
    factory.put(TEST_LONG, 1L);
    assertThat(factory.getObject(Integer.class)).isEqualTo(1);
    assertThat(factory.getObject(String.class)).isEqualTo("1");
    assertThat(factory.getObject(Long.class)).isEqualTo(1L);
    assertThat(factory.getObject(Double.class)).isEqualTo(null);
  }

  @Test
  public void getAndSetMultipleTests() {
    SimpleObjectFactory factory = new SimpleObjectFactory();
    factory.put(TEST_INT, 1);
    factory.put(TEST_STRING1, "1");
    factory.put(TEST_STRING2, "2");
    factory.put(TEST_STRING3, "3");
    factory.put(TEST_LONG, 1L);
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
