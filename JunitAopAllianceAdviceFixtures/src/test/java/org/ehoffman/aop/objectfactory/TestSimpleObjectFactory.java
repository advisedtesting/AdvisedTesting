package org.ehoffman.aop.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;

public class TestSimpleObjectFactory {

    @Test
    public void getAndSetTests() {
        SimpleObjectFactory factory = new SimpleObjectFactory();
        factory.put("testInt", 1);
        factory.put("testString", "1");
        factory.put("testLong", 1L);
        assertThat(factory.getObject(Integer.class)).isEqualTo(1);
        assertThat(factory.getObject(String.class)).isEqualTo("1");
        assertThat(factory.getObject(Long.class)).isEqualTo(1L);
        assertThat(factory.getObject(Double.class)).isEqualTo(null);
    }

    @Test
    public void getAndSetMultipleTests() {
        SimpleObjectFactory factory = new SimpleObjectFactory();
        factory.put("testInt", 1);
        factory.put("testString1", "1");
        factory.put("testString2", "2");
        factory.put("testString3", "3");
        factory.put("testLong", 1L);
        assertThat(factory.getObject(Integer.class)).isEqualTo(1);
        assertThat(factory.getAllObjects(String.class))
                .containsEntry("testString1", "1")
                .containsEntry("testString2", "2")
                .containsEntry("testString3", "3");
        assertThat(factory.getObject(Long.class)).isEqualTo(1L);
        
        try {
            factory.getObject(String.class);
            fail("Expected runtime exception due to multiple beans of same type");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("testString1").contains("testString2").contains("testString3");
        }
    }
    
}
