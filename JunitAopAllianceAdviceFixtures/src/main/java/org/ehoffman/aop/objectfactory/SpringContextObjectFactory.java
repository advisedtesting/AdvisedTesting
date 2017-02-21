/*
 * The MIT License
 * Copyright © 2015 Rex Hoffman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
