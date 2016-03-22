/*
 * The MIT License
 * Copyright (c) 2015 Rex Hoffman
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
/*
f * The MIT License
 * Copyright (c) 2015 Rex Hoffman
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
package org.ehoffman.aopalliance.extensions.scope;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.NamedThreadLocal;

public class SpringTestScope implements Scope {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringTestScope.class);

    public SpringTestScope() {
        initialize();
    }

    private ThreadLocal<HashMap<String, Object>> values = null;
    private ThreadLocal<Map<String, Runnable>> destroyCallbacks = null;
    private ThreadLocal<Map<String, List<String>>> dependencies = null;
    private final Set<Map<String, Runnable>> destroyCallbackMaps 
        = Collections.synchronizedSet(new HashSet<Map<String, Runnable>>());
    private ThreadLocal<Integer> count = null;


    private void initialize() {
        values = new NamedThreadLocal<HashMap<String, Object>>("value") {
            @Override
            protected HashMap<String, Object> initialValue() {
                return new HashMap<String, Object>();
            }
        };

        destroyCallbacks = new ThreadLocal<Map<String, Runnable>>() {
            @Override
            protected Map<String, Runnable> initialValue() {
                final HashMap<String, Runnable> destroyCallbackMap = new HashMap<String, Runnable>();
                destroyCallbackMaps.add(destroyCallbackMap);
                return destroyCallbackMap;
            }
        };

        dependencies = new ThreadLocal<Map<String, List<String>>>() {
            @Override
            protected Map<String, List<String>> initialValue() {
                return new HashMap<String, List<String>>();
            }
        };
        
        count = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
        
    }

    @Override
    public Object get(final String name, final ObjectFactory<?> objectFactory) {
        final Map<String, Object> scope = values.get();
        try {
            final Field field = objectFactory.getClass().getDeclaredField("val$mbd");
            field.setAccessible(true);
            final RootBeanDefinition def = (RootBeanDefinition) field.get(objectFactory);
            if (def != null && def.getDependsOn() != null) {
                dependencies.get().put(name, Arrays.asList(def.getDependsOn()));
                LOGGER.warn("Adding dependencies for " + name + " which are" + Arrays.asList(def.getDependsOn()));
            }
        } catch (final Throwable t) {
            LOGGER.error("Spring has changed, time to update this method");
        }
        Object object = scope.get(name);
        if (object == null) {
            object = objectFactory.getObject();
            scope.put(name, object);
        }
        return object;
    }

    public boolean exists(final String name) {
        final Map<String, Object> scope = values.get();
        return scope.get(name) != null;
    }

    public List<String> dependsOnMe(final String name) {
        final List<String> output = new ArrayList<String>();
        for (final Entry<String, List<String>> dependencList : dependencies.get().entrySet()) {
            if (dependencList.getValue().contains(name)) {
                output.add(dependencList.getKey());
            }
        }
        return output;
    }

    @Override
    public Object remove(final String name) {
        Object removed = null;
        if (exists(name)) {
            final Runnable callback = destroyCallbacks.get().remove(name);
            removed = values.get().remove(name);
            LOGGER.info("removing " + name + " but will remove " + dependsOnMe(name) + " first");
            for (final String dependensOnTarget : dependsOnMe(name)) {
                if (exists(dependensOnTarget)) {
                    remove(dependensOnTarget);
                }
            }
            try {
                if (callback != null) {
                    callback.run();
                }
            } catch (final Throwable t) {
                LOGGER.error("running of callback failed for bean of name: " + name, t);
            }
        }
        return removed;
    }

    @Override
    public void registerDestructionCallback(final String name, final Runnable callback) {
        destroyCallbacks.get().put(name, callback);
    }

    @Override
    public Object resolveContextualObject(final String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return Thread.currentThread().getName() + count.get();
    }

    public void wipeScope() {
        callAllDestroyCallbacks(destroyCallbacks.get());
        values.get().clear();
        dependencies.get().clear();
        count.set(count.get() + 1);
    }

    private void callAllDestroyCallbacks(final Map<String, Runnable> destroyCallbackMap) {
        try {
            synchronized (destroyCallbackMap) {
                for (final String name : new ArrayList<String>(destroyCallbackMap.keySet())) {
                    try {
                        remove(name);
                    } catch (final Throwable t) {
                        LOGGER.error("Exception thrown while destroying bean", t);
                        t.printStackTrace();
                    }
                }
                destroyCallbackMap.clear();
                destroyCallbackMaps.remove(destroyCallbackMap);
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public void wipeAllScopes() {
        for (final Map<String, Runnable> destroyCallbackMap : destroyCallbackMaps) {
            callAllDestroyCallbacks(destroyCallbackMap);
        }
        initialize();
    }
}