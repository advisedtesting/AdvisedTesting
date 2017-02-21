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
package org.ehoffman.aop.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.ehoffman.junit.aop.test.CaptureLogging;
import org.ehoffman.junit.aop.test.IoCContext;
import org.junit.Test;

public class TestProviderAwareObjectFactoryAggregate {

    private static final String TEST_LONG = "testLong";
    private static final String TEST_INT2 = "testInt2";
    private static final String TEST_STRING4 = "testString4";
    //context ids
    private static final String TED = "ted";
    private static final String BOB = "bob";
    
    //entity names
    private static final String TEST_STRING3 = "testString3";
    private static final String TEST_STRING2 = "testString2";
    private static final String TEST_STRING1 = "testString1";

    private IoCContext getInstanceOfIoCContext(final String name) {
        IoCContext annotation = new IoCContext() {
            
            @Override
            public String instance() {
                return null;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return IoCContext.class;
            }
            
            @Override
            public Class<? extends ObjectFactory> objectFactoryClass() {
                return null;
            }
            
            @Override
            public String name() {
                return name;
            }
            
            @Override
            public Class<?>[] classes() {
                return null;
            }
            
            @Override
            public Class<? extends MethodInterceptor> IMPLEMENTED_BY() {
                return null;
            }
        };
        return annotation;
    }
    
    private CaptureLogging getInstanceOfCaptureLogging() {
        return new CaptureLogging() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CaptureLogging.class;
            }

            @Override
            public Class<? extends MethodInterceptor> IMPLEMENTED_BY() {
                return null;
            }
        };
    }    
    
    private static class TestClass {
        
        @SuppressWarnings("unused")
        public void method(@IoCContext(name = BOB, instance = TEST_STRING3) String string, Long longish) {
            
        }
    }
    
    @Test
    public void testAggregateReturnedValues() {
        SimpleObjectFactory factory1 = new SimpleObjectFactory();
        factory1.put(TEST_STRING1, "1");
        factory1.put(TEST_STRING2, "2");
        factory1.put(TEST_STRING3, "3");
        factory1.put("testInt1", 2);
        
        SimpleObjectFactory factory2 = new SimpleObjectFactory();
        factory2.put(TEST_INT2, 1);
        factory2.put(TEST_STRING4, "4"); //first declarations is winning
        factory2.put(TEST_LONG, 1L);
        
        ProviderAwareObjectFactoryAggregate aggregate = new ProviderAwareObjectFactoryAggregate();
        aggregate.register(getInstanceOfIoCContext(BOB), factory1);
        aggregate.register(getInstanceOfCaptureLogging(), factory2);
               
        assertThat(aggregate.getObject(Integer.class)).isEqualTo(2);
        assertThat(aggregate.getAllObjects(String.class))
                .containsEntry(TEST_STRING1, "1")
                .containsEntry(TEST_STRING2, "2")
                .containsEntry(TEST_STRING3, "3")
                .hasSize(3);
        assertThat(aggregate.getObject(Long.class)).isEqualTo(1L);
        assertThat(aggregate.getObject(Long.class)).isEqualTo(1L);
        
        assertThat(aggregate.getAllObjects(getInstanceOfCaptureLogging(), String.class))
                .containsEntry(TEST_STRING4, "4")
                .hasSize(1);
        
        assertThat(aggregate.getAllObjects(getInstanceOfIoCContext(BOB), String.class))
                .containsEntry(TEST_STRING1, "1")
                .containsEntry(TEST_STRING2, "2")
                .containsEntry(TEST_STRING3, "3")
                .hasSize(3);
        
        assertThat(aggregate.getObject(getInstanceOfIoCContext(BOB), Integer.class))
                .isEqualTo(2);
        
        assertThat(aggregate.getAllObjects(getInstanceOfIoCContext(TED), Integer.class))
                .isNull();
                
        try {
            aggregate.getObject(String.class);
            fail("Expected runtime exception due to multiple beans of same type");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains(TEST_STRING1).contains(TEST_STRING2).contains(TEST_STRING3);
        }
        
        Method method = Arrays.asList(TestClass.class.getDeclaredMethods()).stream()
                        .filter(m -> m.getName().equals("method"))
                        .findFirst()
                        .orElse(null);
        
        assertThat(aggregate.getArgumentsFor(method)).contains("3", 1L);
    }
    
    
    
}
