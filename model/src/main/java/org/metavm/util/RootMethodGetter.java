package org.metavm.util;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RootMethodGetter {

    private static final RootMethodGetter INSTANCE = new RootMethodGetter();

    public static Method getRootMethod(Method method) {
        return INSTANCE.get(method);
    }

    private final Map<Method, Method> rootMethodMap = new HashMap<>();

    private Method get(Method method) {
        return rootMethodMap.computeIfAbsent(method, this::computeRootMethod);
    }

    private Method computeRootMethod(Method method) {
        return computeRootMethod(method, method.getDeclaringClass());
    }

    @Nullable
    private Method computeRootMethod(Method method, Class<?> klass) {
        if(!klass.isInterface()) {
            var supers = getSupers(klass);
            for (Class<?> s : supers) {
                var r = computeRootMethod(method, s);
                if (r != null) {
                    return r;
                }
            }
        }
        return getMatchingMethod(method, klass);
    }

    @Nullable
    private Method getMatchingMethod(Method method, Class<?> klass) {
        var declaredMethods = Arrays.asList(klass.getDeclaredMethods());
        return NncUtils.find(declaredMethods, m -> isMethodSignatureMatched(m, method));
    }

    private boolean isMethodSignatureMatched(Method m1, Method m2) {
        return ReflectionUtils.getMethodSignature(m1).equals(ReflectionUtils.getMethodSignature(m2));
    }

    private List<Class<?>> getSupers(Class<?> klass) {
        List<Class<?>> supers = Arrays.asList(klass.getInterfaces());
        if(klass.getSuperclass() != null) {
            supers = NncUtils.prepend(klass.getSuperclass(), supers);
        }
        return supers;
    }

}
