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
package org.ehoffman.aopalliance.extensions.scope;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;

/**
 * A simple thread-backed {@link Scope} implementation.
 *
 * <p><strong>Note:</strong> {@code SimpleThreadScope} <em>does not clean up
 * any objects</em> associated with it. As such, it is typically preferable to
 * use RequestScope
 * in web environments.
 *
 * <p>For an implementation of a thread-based {@code Scope} with support for
 * destruction callbacks, refer to the
 * <a href="http://www.springbyexample.org/examples/custom-thread-scope-module.html">
*  Spring by Example Custom Thread Scope Module</a>.
 *
 * <p>Thanks to Eugene Kuleshov for submitting the original prototype for a thread scope!
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public class STScope implements Scope {

    private static final Log logger = LogFactory.getLog(STScope.class);

    private final ThreadLocal<Map<String, Object>> threadScope =
        new NamedThreadLocal<Map<String, Object>>("SimpleThreadScope") {
            @Override
            protected Map<String, Object> initialValue() {
                return new HashMap<String, Object>();
            }
        };


    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> scope = this.threadScope.get();
        Object object = scope.get(name);
        if (object == null) {
            object = objectFactory.getObject();
            scope.put(name, object);
        }
        return object;
    }

    @Override
    public Object remove(String name) {
        Map<String, Object> scope = this.threadScope.get();
        return scope.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        logger.warn("SimpleThreadScope does not support destruction callbacks. "
                + "Consider using RequestScope in a web environment.");
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return Thread.currentThread().getName();
    }

}