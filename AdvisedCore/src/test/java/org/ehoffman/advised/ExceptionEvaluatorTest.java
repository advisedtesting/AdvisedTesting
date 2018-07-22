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
package org.ehoffman.advised;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ehoffman.advised.internal.ExceptionEvaluator;
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
