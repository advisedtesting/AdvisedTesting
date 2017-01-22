package org.ehoffman.aop.objectfactory;

import java.lang.annotation.Annotation;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProviderAwareObjectFactoryAggregate implements ObjectFactory, ObjectFactoryRegistrar {

    //registrar capability, used to build the default object factory....
    List<java.util.Map.Entry<Class<? extends Annotation>, ObjectFactory>> context = new ArrayList<>();
    
    public void register(Class<? extends Annotation> annotation, ObjectFactory objectFactory) {
        context.add(new AbstractMap.SimpleEntry<>(annotation, objectFactory));
    }
    
    
    @Override
    public <T> T getObject(Class<T> type) {
        return context.stream()
                        .map((entry) -> entry.getValue().getObject(type))
                        .filter(o -> o != null)
                        .findFirst()
                        .orElseGet(() -> null);
    }

    @Override
    public <T> T getObject(String name, Class<T> type) {
        return context.stream()
                        .map((entry) -> entry.getValue().getObject(name, type))
                        .filter(o -> o != null)
                        .findFirst()
                        .orElseGet(() -> null);
    }

    @Override
    public <T> Map<String, ? extends T> getAllObjects(Class<T> type) {
        /*
        return context.stream()
                        .filter((entry) -> entry.getValue().getAllObjects(type))
                        .map((entry) -> entry.getValue().getObject(type))
                        .filter(o -> o != null)
                        .collect(collector)
        ;*/
        return null;
    }
    
    
}
