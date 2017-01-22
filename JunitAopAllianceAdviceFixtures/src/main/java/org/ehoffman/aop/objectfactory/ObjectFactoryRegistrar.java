package org.ehoffman.aop.objectfactory;

import java.lang.annotation.Annotation;

public interface ObjectFactoryRegistrar {

    void register(Class<? extends Annotation> annotation, ObjectFactory factory);
    
}
