package org.ehoffman.aop.objectfactory;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ProviderAwareObjectFactory extends ObjectFactory {

    <T> T getObject(Annotation annotation, Class<T> type);
    
    <T> T getObject(Annotation annotation, String name, Class<T> type);
    
    <T> Map<String, ? extends T> getAllObjects(Annotation annotation, Class<T> type);
    
}
