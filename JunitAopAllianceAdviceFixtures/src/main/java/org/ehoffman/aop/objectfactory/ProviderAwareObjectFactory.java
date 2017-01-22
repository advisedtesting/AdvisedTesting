package org.ehoffman.aop.objectfactory;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ProviderAwareObjectFactory extends ObjectFactory {

    <T> T getObject(Class<? extends Annotation> annotationClass, Class<T> type);
    
    <T> T getObject(Class<? extends Annotation> annotationClass, String name, Class<T> type);
    
    <T> Map<String, ? extends T> getAllObjects(Class<? extends Annotation> annotationClass, Class<T> type);
    
}
