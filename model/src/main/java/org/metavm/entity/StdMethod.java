package org.metavm.entity;

import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.natives.DirectValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.flow.Method;
import org.metavm.util.BiUnion;
import org.metavm.util.NncUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum StdMethod implements ValueHolderOwner<Method> {

    interceptorBefore(Interceptor.class, "before", List.of(HttpRequest.class, HttpResponse.class)),
    interceptorAfter(Interceptor.class, "after", List.of(HttpRequest.class, HttpResponse.class, BiUnion.createNullableType(Object.class))),
    httpRequestGetMethod(HttpRequest.class, "getMethod", List.of()),
    httpRequestGetCookie(HttpRequest.class, "getCookie", List.of(String.class)),
    arrayListAdd(ArrayList.class, "add", List.of(ArrayList.class.getTypeParameters()[0])),
    hashSetAdd(HashSet.class, "add", List.of(HashSet.class.getTypeParameters()[0])),

    ;

    private final Class<?> javaClass;
    private final String methodName;
    private final List<Type> parameterTypes;
    private ValueHolder<Method> methodHolder = new DirectValueHolder<>();

    StdMethod(Class<?> javaClass, String methodName, List<Type> parameterTypes) {
        this.javaClass = javaClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public Method get() {
        return methodHolder.get();
    }

    private void set(Method method) {
        methodHolder.set(method);
    }

    public void init(SystemDefContext defContext) {
        var klass = defContext.getKlass(javaClass);
        var method = klass.getMethodByCodeAndParamTypes(
                methodName,
                NncUtils.map(parameterTypes, defContext::getType)
        );
        set(method);
    }

    public void setValueHolder(ValueHolder<Method> methodHolder) {
        this.methodHolder = methodHolder;
    }

    public static void initialize(SystemDefContext defContext) {
        for (StdMethod value : values()) {
            value.init(defContext);
        }
    }

}
