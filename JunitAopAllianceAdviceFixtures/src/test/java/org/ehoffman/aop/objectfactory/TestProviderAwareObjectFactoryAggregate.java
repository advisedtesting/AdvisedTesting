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
        public void method(@IoCContext(name = "bob", instance = "testString3") String string, Long longish) {
            
        }
    }
    
    @Test
    public void testAggregateReturnedValues() {
        SimpleObjectFactory factory1 = new SimpleObjectFactory();
        factory1.put("testString1", "1");
        factory1.put("testString2", "2");
        factory1.put("testString3", "3");
        factory1.put("testInt1", 2);
        
        SimpleObjectFactory factory2 = new SimpleObjectFactory();
        factory2.put("testInt2", 1);
        factory2.put("testString4", "4"); //first declarations is winning
        factory2.put("testLong", 1L);
        
        ProviderAwareObjectFactoryAggregate aggregate = new ProviderAwareObjectFactoryAggregate();
        aggregate.register(getInstanceOfIoCContext("bob"), factory1);
        aggregate.register(getInstanceOfCaptureLogging(), factory2);
               
        assertThat(aggregate.getObject(Integer.class)).isEqualTo(2);
        assertThat(aggregate.getAllObjects(String.class))
                .containsEntry("testString1", "1")
                .containsEntry("testString2", "2")
                .containsEntry("testString3", "3")
                .hasSize(3);
        assertThat(aggregate.getObject(Long.class)).isEqualTo(1L);
        assertThat(aggregate.getObject(Long.class)).isEqualTo(1L);
        
        assertThat(aggregate.getAllObjects(getInstanceOfCaptureLogging(), String.class))
                .containsEntry("testString4", "4")
                .hasSize(1);
        
        assertThat(aggregate.getAllObjects(getInstanceOfIoCContext("bob"), String.class))
                .containsEntry("testString1", "1")
                .containsEntry("testString2", "2")
                .containsEntry("testString3", "3")
                .hasSize(3);
        
        assertThat(aggregate.getObject(getInstanceOfIoCContext("bob"), Integer.class))
                .isEqualTo(2);
        
        assertThat(aggregate.getAllObjects(getInstanceOfIoCContext("ted"), Integer.class))
                .isNull();
                
        try {
            aggregate.getObject(String.class);
            fail("Expected runtime exception due to multiple beans of same type");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("testString1").contains("testString2").contains("testString3");
        }
        
        Method method = Arrays.asList(TestClass.class.getDeclaredMethods()).stream()
                        .filter(m -> m.getName().equals("method"))
                        .findFirst()
                        .orElse(null);
        
        assertThat(aggregate.getArgumentsFor(method)).contains("3", 1L);
    }
    
    
    
}
