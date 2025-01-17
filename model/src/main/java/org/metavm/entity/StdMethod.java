package org.metavm.entity;

import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.flow.Method;
import org.metavm.util.BiUnion;
import org.metavm.util.Utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.*;

public enum StdMethod implements ValueHolderOwner<Method> {

    interceptorBefore(Interceptor.class, "before", List.of(HttpRequest.class, HttpResponse.class)),
    interceptorAfter(Interceptor.class, "after", List.of(HttpRequest.class, HttpResponse.class, BiUnion.createNullableType(Object.class))),
    httpRequestGetMethod(HttpRequest.class, "getMethod", List.of()),
    httpRequestGetCookie(HttpRequest.class, "getCookie", List.of(String.class)),
    arrayListAdd(ArrayList.class, "add", List.of(ArrayList.class.getTypeParameters()[0])),
    hashSetAdd(HashSet.class, "add", List.of(HashSet.class.getTypeParameters()[0])),
    comparableCompareTo(Comparable.class, "compareTo", List.of(Comparable.class.getTypeParameters()[0])),
    comparatorCompare(Comparator.class, "compare", List.of(Comparator.class.getTypeParameters()[0], Comparator.class.getTypeParameters()[0])),
    iterableIterator(Iterable.class, "iterator", List.of()),
    iteratorHasNext(Iterator.class, "hasNext", List.of()),
    iteratorNext(Iterator.class, "next", List.of()),
    collectionContains(Collection.class, "contains", List.of(Object.class)),
    outputStreamWrite(OutputStream.class, "write", List.of(int.class)),
    objectOutputStreamWriteObject(ObjectOutputStream.class, "writeObject", List.of(Object.class)),
    objectInputStreamReadObject(ObjectInputStream.class, "readObject", List.of()),
    enumOrdinal(Enum.class, "ordinal", List.of())

    ;

    private final Class<?> javaClass;
    private final String methodName;
    private final List<Type> parameterTypes;
    private ValueHolder<Method> methodHolder = new HybridValueHolder<>();

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

    public void setLocal(Method method) {
        methodHolder.setLocal(method);
    }

    public void init(DefContext defContext, boolean local) {
        var klass = defContext.getKlass(javaClass);
        var method = klass.getMethodByNameAndParamTypes(
                methodName,
                Utils.map(parameterTypes, defContext::getNullableType)
        );
        if(local)
            setLocal(method);
        else
            set(method);
    }

    public void setValueHolder(ValueHolder<Method> methodHolder) {
        this.methodHolder = methodHolder;
    }

    @Override
    public ValueHolder<Method> getValueHolder() {
        return methodHolder;
    }

    public static void initialize(DefContext defContext, boolean local) {
        for (StdMethod value : values()) {
            value.init(defContext, local);
        }
    }

}
