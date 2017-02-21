/*
 * The MIT License
 * Copyright © 2015 Rex Hoffman
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
package org.ehoffman.junit.aop.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.ehoffman.junit.aop.Junit4AOPClassRunner;
import org.ehoffman.junit.aop.test.AppConfiguration.TestBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

@RunWith(Junit4AOPClassRunner.class)
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
                                    @IoCContext(name = TED) ApplicationContext context2,
                                    @IoCContext(name = BOB) TestBean bean,
                                    @IoCContext(name = TED) TestBean bean2,
                                    @IoCContext(name = BOB, instance = BILL) String bill,
                                    @IoCContext(name = BOB, instance = TED) String ted,
                                    @IoCContext(name = TED, instance = BILL) String tedsBill) {
        assertThat(context).isNotNull().hasSameClassAs(context2).isNotEqualTo(context2);
        assertThat(bean).isNotNull().hasSameClassAs(bean2).isNotEqualTo(bean2);
        assertThat(bill).isNotNull().isEqualTo(BILL).isNotSameAs(tedsBill);
        assertThat(ted).isSameAs(TED);
    }
}
