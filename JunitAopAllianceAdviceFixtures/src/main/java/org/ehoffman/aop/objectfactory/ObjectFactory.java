package org.ehoffman.aop.objectfactory;

import java.util.Map;

/**
 * This class is expected to be implemented by Annotation providers.   
 * 
 * @see {@link SpringContextObjectFactory}.
 * @see {@link org.ehoffman.junit.aop.test.IoCContextAdvice}.
 * @see {@link org.ehoffman.junit.aop.test.IoCContext}.
 * @author rex
 */
public interface ObjectFactory {

    <T> T getObject(Class<T> type);
    
    <T> T getObject(String name, Class<T> type);
    
    <T> Map<String, T> getAllObjects(Class<T> type);
    
}
