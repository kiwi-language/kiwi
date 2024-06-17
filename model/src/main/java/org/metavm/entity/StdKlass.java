package org.metavm.entity;

import org.metavm.api.ChildList;
import org.metavm.api.Interceptor;
import org.metavm.api.ValueList;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.natives.*;
import org.metavm.object.type.Klass;
import org.metavm.util.IteratorImpl;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum StdKlass {

    entity(Entity.class),
    enum_(Enum.class),
    record(Record.class),
    collection(Collection.class),
    list(List.class, false, ListNative.class),
    arrayList(ArrayList.class, false, ListNative.class),
    childList(ChildList.class, false, ListNative.class),
    valueList(ValueList.class, false, ListNative.class),
    iterator(Iterator.class),
    iterable(Iterable.class),
    iteratorImpl(IteratorImpl.class, false, IteratorImplNative.class),
    set(Set.class, false, SetNative.class),
    map(Map.class, false, MapNative.class),
    interceptor(Interceptor.class),
    httpRequest(HttpRequest.class),
    httpResponse(HttpResponse.class),
    httpCookie(HttpCookie.class),
    throwable(Throwable.class),
    exception(Exception.class, false, ExceptionNative.class),
    nullPointerException(NullPointerException.class, false, NullPointerExceptionNative.class),
    runtimeException(RuntimeException.class, false, RuntimeExceptionNative.class),
    illegalArgumentException(IllegalArgumentException.class, false, IllegalStateExceptionNative.class),
    illegalStateException(IllegalStateException.class, false, IllegalStateExceptionNative.class),
    consumer(Consumer.class),
    predicate(Predicate.class),
    supplier(Supplier.class, true, null);
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
        this.klassHolder = new DirectValueHolder<>();
    }

    public static void initialize(DefContext defContext) {
        for (StdKlass def : values()) {
            def.init(defContext);
        }
    }

    public static void setDefaultMode() {
        for (StdKlass def : values()) {
            def.setKlassHolder(new DirectValueHolder<>());
        }
    }

    public static void setThreadLocalMode() {
        for (StdKlass def : values()) {
            def.setKlassHolder(new ThreadLocalValueHolder<>());
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

    void set(Klass klass) {
        klass.setNativeClass(nativeClass);
        klassHolder.set(klass);
    }

    void setKlassHolder(ValueHolder<Klass> klassHolder) {
        this.klassHolder = klassHolder;
    }

    public void init(DefContext defContext) {
        var klass = defContext.getKlass(javaClass);
        set(klass);
    }
}
