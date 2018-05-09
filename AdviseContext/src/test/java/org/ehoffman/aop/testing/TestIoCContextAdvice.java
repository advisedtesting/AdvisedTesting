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
package org.ehoffman.aop.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehoffman.aop.context.IoCContext;
import org.ehoffman.aop.testing.AppConfiguration.TestBean;
import org.ehoffman.junit.aop.Junit4AopClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

@RunWith(Junit4AopClassRunner.class)
public class TestIoCContextAdvice {

  private static final String BILL = "bill";
  private static final String TED = "ted";
  private static final String BOB = "bob";

  @Test
  @IoCContext(classes = { AppConfiguration.class })
  public void testTestIoCContext(TestBean bean) {
    assertThat(bean).isNotNull();
  }

  @Test
  @IoCContext(name = BOB, classes = { AppConfiguration.class })
  @IoCContext(name = TED, classes = { AppConfiguration.class })
  public void testTestIoCContext2(ApplicationContext context, TestBean bean) {
    assertThat(context).isNotNull();
    assertThat(bean).isNotNull();
  }

  @Test
  @IoCContext(name = BOB, classes = { AppConfiguration.class })
  @IoCContext(name = TED, classes = { AppConfiguration.class })
  public void testTestIoCContext3(@IoCContext(name = BOB) ApplicationContext context,
          @IoCContext(name = TED) ApplicationContext context2, @IoCContext(name = BOB) TestBean bean,
          @IoCContext(name = TED) TestBean bean2, @IoCContext(name = BOB, instance = BILL) String bill,
          @IoCContext(name = BOB, instance = TED) String ted, @IoCContext(name = TED, instance = BILL) String tedsBill) {
    assertThat(context).isNotNull().hasSameClassAs(context2).isNotEqualTo(context2);
    assertThat(bean).isNotNull().hasSameClassAs(bean2).isNotEqualTo(bean2);
    assertThat(bill).isNotNull().isEqualTo(BILL).isNotSameAs(tedsBill);
    assertThat(ted).isSameAs(TED);
  }
}
