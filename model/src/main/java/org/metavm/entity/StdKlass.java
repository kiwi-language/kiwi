package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.ChildList;
import org.metavm.api.Interceptor;
import org.metavm.api.ValueList;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.natives.*;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.DummyAny;
import org.metavm.util.IteratorImpl;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
public enum StdKlass implements ValueHolderOwner<Klass> {

    entity(Entity.class),
    enum_(Enum.class, false, EnumNative.class),
    record(Record.class),
    collection(Collection.class),
    list(List.class, false, ListNative.class),
    arrayList(ArrayList.class, false, ListNative.class),
    childList(ChildList.class, false, ListNative.class),
    valueList(ValueList.class, false, ListNative.class),
    iterator(Iterator.class),
    iterable(Iterable.class),
    iteratorImpl(IteratorImpl.class, false, IteratorImplNative.class),
    set(Set.class, false, HashSetNative.class),
    hashSet(HashSet.class, false, HashSetNative.class),
    treeSet(TreeSet.class, false, TreeSetNative.class),
    map(Map.class, false, MapNative.class),
    hashMap(HashMap.class, false, MapNative.class),
    interceptor(Interceptor.class),
    httpRequest(HttpRequest.class),
    httpResponse(HttpResponse.class),
    httpRequestImpl(HttpRequestImpl.class),
    httpResponseImpl(HttpResponseImpl.class),
    httpCookie(HttpCookie.class),
    throwable(Throwable.class),
    exception(Exception.class, false, ExceptionNative.class),
    nullPointerException(NullPointerException.class, false, NullPointerExceptionNative.class),
    runtimeException(RuntimeException.class, false, RuntimeExceptionNative.class),
    illegalArgumentException(IllegalArgumentException.class, false, IllegalArgumentExceptionNative.class),
    illegalStateException(IllegalStateException.class, false, IllegalStateExceptionNative.class),
    indexOutOfBoundsException(IndexOutOfBoundsException.class, false, IndexOutOfBoundsExceptionNative.class),
    consumer(Consumer.class),
    predicate(Predicate.class),
    supplier(Supplier.class, true, null),
    comparable(Comparable.class),
    comparator(Comparator.class),
    serializable(Serializable.class),
    type(Type.class),
    klass(Klass.class),
    any(DummyAny.class),
    stringBuilder(StringBuilder.class, false, StringBuilderNative.class),
    ;

    private final Class<?> javaClass;
    private final boolean autoDefine;
    private final @Nullable Class<? extends NativeBase> nativeClass;
    private ValueHolder<Klass> klassHolder;

    StdKlass(Class<?> javaClass) {
        this(javaClass, false, null);
    }

    StdKlass(Class<?> javaClass, boolean autoDefine, @Nullable Class<? extends NativeBase> nativeClass) {
        this.javaClass = javaClass;
        this.autoDefine = autoDefine;
        this.nativeClass = nativeClass;
        this.klassHolder = new HybridValueHolder<>();
    }

    public static void initialize(DefContext defContext, boolean local) {
        for (StdKlass def : values()) {
            def.init(defContext, local);
        }
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public boolean isAutoDefine() {
        return autoDefine;
    }

    @Nullable
    public Class<?> getNativeClass() {
        return nativeClass;
    }

    public Klass get() {
        return Objects.requireNonNull(klassHolder.get(), () -> "Builtin klass not initialized: " + javaClass.getName());
    }

    public boolean isInitialized() {
        return klassHolder.get() != null;
    }

    public ClassType type() {
        return get().getType();
    }

    void set(Klass klass) {
        klass.setNativeClass(nativeClass);
        klassHolder.set(klass);
    }

    void setLocal(Klass klass) {
        klass.setNativeClass(nativeClass);
        klassHolder.setLocal(klass);
    }

    public void setValueHolder(ValueHolder<Klass> klassHolder) {
        this.klassHolder = klassHolder;
    }

    @Override
    public ValueHolder<Klass> getValueHolder() {
        return klassHolder;
    }

    public void init(DefContext defContext, boolean local) {
        var klass = defContext.getKlass(javaClass);
        if(local)
            setLocal(klass);
        else
            set(klass);
    }
}
