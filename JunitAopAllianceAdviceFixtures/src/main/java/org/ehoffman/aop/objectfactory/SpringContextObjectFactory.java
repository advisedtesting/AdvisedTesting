package org.ehoffman.aop.objectfactory;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContextObjectFactory implements ObjectFactory {

    @Override
    public <T> T getObject(Class<T> type) {
        return context.getBean(type);
    }

    @Override
    public <T> T getObject(String name, Class<T> type) {
        return context.getBean(name, type);
    }

    @Override
    public <T> Map<String, T> getAllObjects(Class<T> type) {
        return context.getBeansOfType(type);
    }

    private ApplicationContext context;
    
    
    public SpringContextObjectFactory(Class<?>... classes) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(classes);
        context.refresh();
        this.context = context;
    }
    
    public SpringContextObjectFactory(List<Class<?>> classes) {
        this(new Class<?>[classes.size()]);
    }
    
    
}
