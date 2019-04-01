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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.advisedtesting.core.internal.ExceptionEvaluator;
import org.junit.Test;

public class ExceptionEvaluatorTest {

  @Test
  public void testEvaluator() {
    RuntimeException exception = new RuntimeException("message");
    Throwable throwable = new Throwable("failed", exception);
    assertThat(ExceptionEvaluator.convertExceptionIfPossible(throwable, RuntimeException.class)).isEqualTo(exception);
    assertThat(ExceptionEvaluator.convertExceptionIfPossible(null, RuntimeException.class)).isEqualTo(null);
    assertThat(ExceptionEvaluator.convertExceptionIfPossible(exception, IllegalAccessException.class)).isEqualTo(null);
    assertThatThrownBy(() -> ExceptionEvaluator.convertExceptionIfPossible(null, null))
       .isInstanceOf(IllegalArgumentException.class);
    assertThat(new ExceptionEvaluator()).isNotNull();
  }
  
}
