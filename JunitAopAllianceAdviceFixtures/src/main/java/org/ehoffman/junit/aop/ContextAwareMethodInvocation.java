package org.ehoffman.junit.aop;

import java.lang.annotation.Annotation;

import org.aopalliance.intercept.MethodInvocation;
import org.ehoffman.aop.objectfactory.ObjectFactory;

public interface ContextAwareMethodInvocation extends MethodInvocation {

    void registerObjectFactory(ObjectFactory factory);
    
    ObjectFactory getCurrentContextFactory();
    
    Annotation getTargetAnnotation();
    
}
