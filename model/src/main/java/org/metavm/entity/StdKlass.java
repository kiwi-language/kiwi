package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.ChildList;
import org.metavm.api.Index;
import org.metavm.api.Interceptor;
import org.metavm.api.ValueList;
import org.metavm.api.entity.*;
import org.metavm.entity.natives.*;
import org.metavm.flow.FlowRef;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpHeaderImpl;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.core.StringInstance;
import org.metavm.object.type.*;
import org.metavm.util.DummyAny;
import org.metavm.util.IteratorImpl;
import org.metavm.util.MvObjectInputStream;
import org.metavm.util.MvObjectOutputStream;

import javax.annotation.Nullable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
public enum StdKlass implements ValueHolderOwner<Klass> {

    entity(Entity.class),
    enum_(java.lang.Enum.class, false, EnumNative.class),
    record(Record.class),
    collection(Collection.class, false, CollectionNative.class),
    list(List.class, false, ArrayListNative.class),
    sequencedCollection(SequencedCollection.class, false, SequencedCollectionNative.class),
    abstractCollection(AbstractCollection.class, false, AbstractCollectionNative.class),
    abstractList(AbstractList.class, false, AbstractListNative.class),
    arrayList(ArrayList.class, false, ArrayListNative.class),
    childList(ChildList.class, false, ChildListNative.class),
    valueList(ValueList.class, false, ArrayListNative.class),
    iterator(Iterator.class),
    iterable(Iterable.class),
    iteratorImpl(IteratorImpl.class, false, IteratorImplNative.class),
    set(Set.class, false, HashSetNative.class),
    abstractSet(AbstractSet.class, false, AbstractSetNative.class),
    hashSet(HashSet.class, false, HashSetNative.class),
    sortedSet(SortedSet.class, false, SortedSetNative.class),
    treeSet(TreeSet.class, false, TreeSetNative.class),
    map(Map.class, false, MapNative.class),
    abstractMap(AbstractMap.class, false, AbstractMapNative.class),
    hashMap(HashMap.class, false, HashMapNative.class),
    interceptor(Interceptor.class),
    httpRequest(HttpRequest.class),
    httpResponse(HttpResponse.class),
    httpRequestImpl(HttpRequestImpl.class, false, HttpRequestImplNative.class),
    httpResponseImpl(HttpResponseImpl.class, false, HttpResponseImplNative.class),
    httpCookie(HttpCookie.class),
    httpHeader(HttpHeader.class),
    throwable(Throwable.class),
    exception(Exception.class, false, ExceptionNative.class),
    nullPointerException(NullPointerException.class, false, NullPointerExceptionNative.class),
    arithmeticException(ArithmeticException.class, false, ArithmeticExceptionNative.class),
    runtimeException(RuntimeException.class, false, RuntimeExceptionNative.class),
    illegalArgumentException(IllegalArgumentException.class, false, IllegalArgumentExceptionNative.class),
    illegalStateException(IllegalStateException.class, false, IllegalStateExceptionNative.class),
    indexOutOfBoundsException(IndexOutOfBoundsException.class, false, IndexOutOfBoundsExceptionNative.class),
    arrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException.class, false, ArrayIndexOutOfBoundsExceptionNative.class),
    classCastException(ClassCastException.class, false, ClassCastExceptionNative.class),
    consumer(Consumer.class),
    predicate(Predicate.class),
    supplier(Supplier.class, true, null),
    comparable(Comparable.class),
    comparator(Comparator.class),
    serializable(Serializable.class),
    type(Type.class),
    primitiveType(PrimitiveType.class),
    classType(ClassType.class),
    anyType(AnyType.class),
    neverType(NeverType.class),
    nullType(NullType.class),
    capturedType(CapturedType.class),
    variableType(VariableType.class),
    arrayType(ArrayType.class),
    klassType(KlassType.class),
    functionType(FunctionType.class),
    unionType(UnionType.class),
    intersectionType(IntersectionType.class),
    uncertainType(UncertainType.class),
    genericDeclarationRef(GenericDeclarationRef.class),
    fieldRef(FieldRef.class),
    flowRef(FlowRef.class),
    methodRef(MethodRef.class),
    functionRef(FunctionRef.class),
    lambdaRef(LambdaRef.class),
    indexRef(IndexRef.class),
    klass(Klass.class),
    any(DummyAny.class),
    stringBuilder(StringBuilder.class, false, StringBuilderNative.class),
    outputStream(OutputStream.class, false, OutputStreamNative.class),
    objectOutputStream(ObjectOutputStream.class, false, ObjectOutputStreamNative.class),
    objectInputStream(ObjectInputStream.class, false, ObjectInputStreamNative.class),
    index(Index.class, false, IndexNative.class),
    charSequence(CharSequence.class, false, CharSequenceNative.class),
    number(Number.class),
    mvObjectOutputStream(MvObjectOutputStream.class, false, MvObjectOutputStreamNative.class),
    mvObjectInputStream(MvObjectInputStream .class, false, MvObjectInputStreamNative.class),
    httpCookieImpl(HttpCookieImpl.class),
    httpHeaderImpl(HttpHeaderImpl.class),
    byte_(Byte.class, false, ByteNative.class),
    short_(Short.class, false, ShortNative.class),
    integer(Integer.class, false, IntegerNative.class),
    long_(Long.class, false, LongNative.class),
    float_(Float.class, false, FloatNative.class),
    double_(Double.class, false, DoubleNative.class),
    character(Character.class, false, CharacterNative.class),
    boolean_(Boolean.class, false, BooleanNative.class),
    string(String.class, false, StringInstance.class),
    mvObject(MvObject.class, false, MvObjectNative.class)
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

    public void set(Klass klass) {
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
