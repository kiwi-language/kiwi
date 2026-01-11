package org.manul.entity;

import org.manul.api.Interceptor;
import org.manul.api.entity.HttpRequest;
import org.manul.flow.Method;
import org.manul.flow.Parameter;
import org.manul.object.type.Type;
import org.manul.object.type.Types;

import java.util.List;

public enum StdMethod {

    interceptorBefore(Interceptor.class, "before", List.of(StdKlass.httpRequest.type(), StdKlass.httpResponse.type())),
    interceptorAfter(Interceptor.class, "after", List.of(StdKlass.httpRequest.type(), StdKlass.httpResponse.type(), Types.getNullableAnyType())),
    httpRequestGetMethod(HttpRequest.class, "getMethod", List.of()),
    httpRequestGetCookie(HttpRequest.class, "getCookie", List.of(StdKlass.string.type())),

;

    private final Method method;

    StdMethod(Class<?> javaClass, String methodName, List<Type> parameterTypes) {
        var klass = StdKlassRegistry.instance.getKlass(javaClass);
        method = klass.getMethod(m -> {
            if (m.getName().equals(methodName) && m.getParameters().size() == parameterTypes.size()) {
                var it = parameterTypes.iterator();
                for (Parameter parameter : m.getParameters()) {
                    var pt = it.next();
                    if (!parameter.getType().equals(pt))
                        return false;
                }
                return true;
            }
            else
                return false;
        });
    }

    public Method get() {
        return method;
    }

}
