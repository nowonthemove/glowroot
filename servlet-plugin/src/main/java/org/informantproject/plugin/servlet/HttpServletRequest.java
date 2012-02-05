/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.informantproject.plugin.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.informantproject.api.Logger;
import org.informantproject.api.LoggerFactory;
import org.informantproject.shaded.google.common.cache.CacheBuilder;
import org.informantproject.shaded.google.common.cache.CacheLoader;
import org.informantproject.shaded.google.common.cache.LoadingCache;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
class HttpServletRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequest.class);

    private static final LoadingCache<ClassLoader, ScopedMethods> methodCache = CacheBuilder
            .newBuilder().weakKeys().build(new CacheLoader<ClassLoader, ScopedMethods>() {
                @Override
                public ScopedMethods load(ClassLoader classLoader) throws Exception {
                    return new ScopedMethods(classLoader);
                }
            });

    private final Object realRequest;
    private final ScopedMethods methods;

    private HttpServletRequest(Object realRequest) {
        this.realRequest = realRequest;
        try {
            methods = methodCache.get(realRequest.getClass().getClassLoader());
        } catch (ExecutionException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    HttpSession getSession(boolean create) {
        try {
            return HttpSession.from(methods.getSessionOneArgMethod.invoke(realRequest, create));
        } catch (IllegalArgumentException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    String getMethod() {
        try {
            return (String) methods.getMethodMethod.invoke(realRequest);
        } catch (IllegalArgumentException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    String getRequestURI() {
        try {
            return (String) methods.getRequestURIMethod.invoke(realRequest);
        } catch (IllegalArgumentException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    Map<?, ?> getParameterMap() {
        try {
            return (Map<?, ?>) methods.getParameterMapMethod.invoke(realRequest);
        } catch (IllegalArgumentException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    Object getAttribute(String name) {
        try {
            return methods.getAttributeMethod.invoke(realRequest, name);
        } catch (IllegalArgumentException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            logger.error("Fatal error occurred: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    static HttpServletRequest from(Object realRequest) {
        return realRequest == null ? null : new HttpServletRequest(realRequest);
    }

    private static final class ScopedMethods {

        private final Method getSessionOneArgMethod;
        private final Method getMethodMethod;
        private final Method getRequestURIMethod;
        private final Method getParameterMapMethod;
        private final Method getAttributeMethod;

        private ScopedMethods(ClassLoader classLoader) throws ClassNotFoundException,
                SecurityException, NoSuchMethodException {

            Class<?> httpServletRequestClass = classLoader
                    .loadClass("javax.servlet.http.HttpServletRequest");
            getSessionOneArgMethod = httpServletRequestClass.getMethod("getSession", boolean.class);
            getMethodMethod = httpServletRequestClass.getMethod("getMethod");
            getRequestURIMethod = httpServletRequestClass.getMethod("getRequestURI");
            getParameterMapMethod = httpServletRequestClass.getMethod("getParameterMap");
            getAttributeMethod = httpServletRequestClass.getMethod("getAttribute", String.class);
        }
    }
}
