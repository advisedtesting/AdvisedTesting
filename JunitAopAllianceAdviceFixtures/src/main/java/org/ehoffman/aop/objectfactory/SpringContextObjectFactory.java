package org.ehoffman.aop.objectfactory;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContextObjectFactory implements ObjectFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(Class<T> type) {
        try {
            return context.getBean(type);
        } catch (BeanNotOfRequiredTypeException | NoSuchBeanDefinitionException e) {
            if (ApplicationContext.class.isAssignableFrom(type)) {
                return (T) context;
            }
            if (ObjectFactory.class.isAssignableFrom(type)) {
                return (T) this;
            }
            return null;
        }
    }

    @Override
    public <T> T getObject(String name, Class<T> type) {
        try {
            return context.getBean(name, type);
        } catch (BeanNotOfRequiredTypeException | NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Override
    public <T> Map<String, T> getAllObjects(Class<T> type) {
        try {
            return context.getBeansOfType(type);
        } catch (BeanNotOfRequiredTypeException | NoSuchBeanDefinitionException e) {
            return null;
        }
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
