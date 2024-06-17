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

public class BuiltinKlasses {

    private static final List<BuiltinKlassDef> defs = new ArrayList<>();
    private static Supplier<ValueHolder<Klass>> klassHolderSupplier = DirectValueHolder::new;

    public static final BuiltinKlassDef entity = createDef(Entity.class);
    public static final BuiltinKlassDef enum_ = createDef(Enum.class);
    public static final BuiltinKlassDef record = createDef(Record.class);
    public static final BuiltinKlassDef collection = createDef(Collection.class);
    public static final BuiltinKlassDef list = createDef(List.class, false, ListNative.class);
    public static final BuiltinKlassDef arrayList = createDef(ArrayList.class, false, ListNative.class);
    public static final BuiltinKlassDef childList = createDef(ChildList.class, false, ListNative.class);
    public static final BuiltinKlassDef valueList = createDef(ValueList.class, false, ListNative.class);
    public static final BuiltinKlassDef iterator = createDef(Iterator.class);
    public static final BuiltinKlassDef iterable = createDef(Iterable.class);
    public static final BuiltinKlassDef iteratorImpl = createDef(IteratorImpl.class, false, IteratorImplNative.class);
    public static final BuiltinKlassDef set = createDef(Set.class, false, SetNative.class);
    public static final BuiltinKlassDef map = createDef(Map.class, false, MapNative.class);
    public static final BuiltinKlassDef interceptor = createDef(Interceptor.class);
    public static final BuiltinKlassDef httpRequest = createDef(HttpRequest.class);
    public static final BuiltinKlassDef httpResponse = createDef(HttpResponse.class);
    public static final BuiltinKlassDef httpCookie = createDef(HttpCookie.class);
    public static final BuiltinKlassDef throwable = createDef(Throwable.class);
    public static final BuiltinKlassDef exception = createDef(Exception.class, false, ExceptionNative.class);
    public static final BuiltinKlassDef nullPointerException = createDef(NullPointerException.class, false, NullPointerExceptionNative.class);
    public static final BuiltinKlassDef runtimeException = createDef(RuntimeException.class, false, RuntimeExceptionNative.class);
    public static final BuiltinKlassDef illegalArgumentException = createDef(IllegalArgumentException.class, false, IllegalStateExceptionNative.class);
    public static final BuiltinKlassDef illegalStateException = createDef(IllegalStateException.class, false, IllegalStateExceptionNative.class);
    public static final BuiltinKlassDef consumer = createDef(Consumer.class);
    public static final BuiltinKlassDef predicate = createDef(Predicate.class);
    public static final BuiltinKlassDef supplier = createDef(Supplier.class, true, null);

    public static List<BuiltinKlassDef> defs() {
        return Collections.unmodifiableList(defs);
    }

    public static void initialize(DefContext defContext) {
        defs.forEach(def -> def.init(defContext));
    }

    public static void setDefaultMode() {
        klassHolderSupplier = DirectValueHolder::new;
        defs.forEach(def -> def.setKlassHolder(klassHolderSupplier.get()));
    }

    public static void setThreadLocalMode() {
        klassHolderSupplier = ThreadLocalValueHolder::new;
        defs.forEach(def -> def.setKlassHolder(klassHolderSupplier.get()));
    }

    private static BuiltinKlassDef createDef(Class<?> javaClass) {
        return createDef(javaClass, false, null);
    }

    private static BuiltinKlassDef createDef(Class<?> javaClass, boolean autoDefine, @Nullable Class<? extends NativeBase> nativeClass) {
        var def = new BuiltinKlassDef(javaClass, autoDefine, nativeClass, klassHolderSupplier.get());
        defs.add(def);
        return def;
    }

}
