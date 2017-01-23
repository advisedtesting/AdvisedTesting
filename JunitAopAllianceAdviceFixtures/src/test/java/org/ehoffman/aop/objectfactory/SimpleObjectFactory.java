package org.ehoffman.aop.objectfactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleObjectFactory implements ObjectFactory {

    private final LinkedHashMap<String, Object> contents = new LinkedHashMap<>();
    
    public void put(String name, Object object) {
        contents.put(name, object);
    }

    public void putAll(Map<String, Object> additions) {
        contents.putAll(additions);
    }

    public SimpleObjectFactory() {
    }
    
    public SimpleObjectFactory(Map<String, Object> contents) {
        putAll(contents);
    }
    
    @Override
    public <T> T getObject(Class<T> type) {
        @SuppressWarnings("unchecked")
        Map<String, ? extends T> possibleOutputs = (Map<String, ? extends T>) contents.entrySet().stream()
                        .filter(o -> type.isAssignableFrom(o.getValue().getClass()))
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        if (possibleOutputs.size() == 1) {
            return possibleOutputs.values().iterator().next();
        }
        if (possibleOutputs.size() > 1) {
            throw new RuntimeException("Multiple objects from the ObjectFactory can satisfy that type, but we"
                            + " are searching for a singleton. Matching names are " + possibleOutputs.keySet());
        }
        return null;
    }

    @Override
    public <T> T getObject(String name, Class<T> type) {
        @SuppressWarnings("unchecked")
        Map<String, ? extends T> possibleOutputs = (Map<String, ? extends T>) contents.entrySet().stream()
                        .filter(o -> type.isAssignableFrom(o.getValue().getClass()))
                        .filter(o -> o.getKey().equals(name))
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        if (possibleOutputs.size() == 1) {
            return possibleOutputs.values().iterator().next();
        } 
        if (possibleOutputs.size() > 1) {
            throw new RuntimeException("Multiple objects from the ObjectFactory can satisfy that type, but we"
                            + " are searching for a singleton. Matching names are " + possibleOutputs.keySet());
        }
        return null;
    }

    @Override
    public <T> Map<String, T> getAllObjects(Class<T> type) {
        @SuppressWarnings("unchecked")
        Map<String, T> output = (Map<String, T>) contents.entrySet().stream()
             .filter(o -> type.isAssignableFrom(o.getValue().getClass()))
             .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        return output;
    }

}
