package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.*;
import tech.metavm.util.LinkedList;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReadonlyArray<T> extends Entity implements IdInitializing, RuntimeGeneric, Iterable<T> {

    final List<T> table;

    public static final int DEFAULT_INDEX_BUILD_THRESHOLD = 3;

    public static <T> ReadonlyArray<T> createProxy(Class<? extends ReadonlyArray<T>> proxyClass, Type elementType) {
        return ReflectionUtils.invokeConstructor(
                ReflectionUtils.getConstructor(proxyClass, Type.class),
                elementType
        );
    }

    private final Type genericType;
    private final Type elementType;
    private ModelIdentity identifier;

    public ReadonlyArray(Class<T> klass, Collection<T> data) {
        this(TypeReference.of(klass).getGenericType(), data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(TypeReference<T> typeRef, Collection<T> data) {
        this(typeRef.getGenericType(), data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(Class<T> klass) {
        this(TypeReference.of(klass).getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(TypeReference<T> typeRef) {
        this(typeRef.getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(Type elementType) {
        this(elementType, List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    private ReadonlyArray(Type elementType, Collection<T> data, int buildIndexThreshold) {
        this.elementType = elementType;
        this.genericType = new ParameterizedTypeImpl(
                null,
                getRawClass(),
                new Type[]{ReflectionUtils.eraseType(elementType)}
        );
        table = new ArrayList<>(data);
    }

    private ReadonlyArray(Type elementType, Type genericType, List<T> table) {
        this.elementType = elementType;
        this.genericType = genericType;
        this.table = table;
    }

    @Override
    public void forEachReference(Consumer<Object> action) {
        for (T t : this) {
            if (t != null && !ValueUtil.isPrimitive(t))
                action.accept(t);
        }
    }

    @Override
    public void forEachDescendant(Consumer<Entity> action) {
        action.accept(this);
    }

    @NoProxy
    public void initialize(Collection<? extends T> data) {
        table.clear();
        table.addAll(data);
    }

    protected Class<?> getRawClass() {
        return ReadonlyArray.class;
    }

    public T get(int index) {
        return table.get(index);
    }

    public <K> T get(IndexMapper<? super T, K> keyMapper, K key) {
        return NncUtils.find(table, t -> Objects.equals(keyMapper.apply(t), key));
    }

    public <K> T remove(IndexMapper<T, K> keyMapper, K key) {
        var toRemove = get(keyMapper, key);
        if(toRemove != null) {
            table.remove(toRemove);
            return toRemove;
        }
        else
            return null;
    }

    public <K> List<T> filter(IndexMapper<T, K> keyMapper, K key) {
        return table.stream().filter(t -> keyMapper.apply(t).equals(key)).toList();
    }

    @SuppressWarnings("unused")
    public <K> void buildIndex(IndexMapper<T, K> keyMapper) {
//        table.buildIndex(keyMapper);
    }

    public ReadonlyArray<T> merge(ReadonlyArray<T> that) {
        var mergedTable = new ArrayList<>(this.table);
        mergedTable.addAll(that.table);
        return new ReadonlyArray<>(genericType, elementType, mergedTable);
    }

    public <R> List<R> map(Function<T, R> mapper) {
        return mapAndFilter(mapper, t -> true);
    }

    public <R> List<R> mapAndFilter(Function<T, R> mapper, Predicate<R> filter) {
        return table.stream().map(mapper).filter(filter).toList();
    }

    public ModelIdentity getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("unused")
    public void setIdentifier(ModelIdentity identifier) {
        this.identifier = identifier;
    }

    @SuppressWarnings("unused")
    public String getIdentifierName() {
        return NncUtils.get(identifier, ModelIdentity::name);
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of(Table.class.getTypeParameters()[0], elementType);
    }

    @Override
    public void clearId() {
        this.id = null;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return table.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        table.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return table.spliterator();
    }

    public int size() {
        return table.size();
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(table.subList(fromIndex, toIndex));
    }

    public Type getElementType() {
        return elementType;
    }

    public Class<?> getElementClass() {
        return ReflectionUtils.getRawClass(elementType);
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public int indexOf(Object value) {
        return table.indexOf(value);
    }

    public int lastIndexOf(Object o) {
        return table.lastIndexOf(o);
    }

    public boolean contains(Object value) {
        //noinspection SuspiciousMethodCalls
        return table.contains(value);
    }

    public List<T> toList() {
        return Collections.unmodifiableList(table);
    }

    public Stream<T> stream() {
        return table.stream();
    }

}
