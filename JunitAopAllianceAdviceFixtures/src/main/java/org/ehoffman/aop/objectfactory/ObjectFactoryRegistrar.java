package org.ehoffman.aop.objectfactory;

import java.lang.annotation.Annotation;

public interface ObjectFactoryRegistrar {

    void register(Annotation annotation, ObjectFactory factory);
    
}
