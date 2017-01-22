package org.ehoffman.junit.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.ehoffman.aop.objectfactory.ObjectFactory;

public interface ContextAwareMethodInvocation extends MethodInvocation {

    void registerObjectFactory(ObjectFactory factory);
    
    ObjectFactory getCurrentContextFactory();
    
}
