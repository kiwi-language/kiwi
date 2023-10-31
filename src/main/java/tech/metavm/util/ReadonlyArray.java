package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.entity.NoProxy;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReadonlyArray<T> implements IdInitializing, RuntimeGeneric, Iterable<T> {

    final Table<T> table;

    public static final int DEFAULT_INDEX_BUILD_THRESHOLD = 3;

    public static <T> ReadonlyArray<T> createProxy(Class<? extends ReadonlyArray<T>> proxyClass, Type elementType) {
        return ReflectUtils.invokeConstructor(
                ReflectUtils.getConstructor(proxyClass, Type.class),
                elementType
        );
    }

    private Long id;
    private final Type genericType;
    private final Type elementType;
    private ModelIdentity identifier;

    public ReadonlyArray(Class<T> klass, Collection<T> data) {
        this(TypeReference.of(klass), data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(TypeReference<T> typeRef, Collection<T> data) {
        this(typeRef, data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(Class<T> klass, int buildIndexThreshold) {
        this(TypeReference.of(klass), List.of(), buildIndexThreshold);
    }

    public ReadonlyArray(TypeReference<T> typeRef, int buildIndexThreshold) {
        this(typeRef, List.of(), buildIndexThreshold);
    }

    public ReadonlyArray(Class<T> klass) {
        this(TypeReference.of(klass).getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(TypeReference<T> typeRef) {
        this(typeRef.getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public ReadonlyArray(TypeReference<T> typeRef, Collection<T> data, int buildIndexThreshold) {
        this(typeRef.getGenericType(), data, buildIndexThreshold);
    }

    public ReadonlyArray(Type elementType) {
        this(elementType, List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    private ReadonlyArray(Type elementType, Collection<T> data, int buildIndexThreshold) {
        this.elementType = elementType;
        this.genericType = new ParameterizedTypeImpl(
                null,
                getRawClass(),
                new Type[]{ReflectUtils.eraseType(elementType)}
        );
        table = new Table<T>(elementType, data, buildIndexThreshold);
    }

    private ReadonlyArray(Type elementType, Type genericType, Table<T> table) {
        this.elementType = elementType;
        this.genericType = genericType;
        this.table = table;
    }

    @NoProxy
    public void initialize(Collection<? extends T> data) {
        table.initialize(data);
    }

    protected Class<?> getRawClass() {
        return ReadonlyArray.class;
    }

    public void initId(long id) {
        NncUtils.requireNull(this.id, "id already initialized");
        this.id = id;
    }

    public T get(int index) {
        return table.get(index);
    }

    public <K> T get(IndexMapper<? super T, K> keyMapper, K key) {
        return table.get(keyMapper, key);
    }

    public <K> T remove(IndexMapper<T, K> keyMapper, K key) {
        return table.remove(keyMapper, key);
    }

    public <K> List<T> filter(IndexMapper<T, K> keyMapper, K key) {
        return table.filter(keyMapper, key);
    }

    public Long getId() {
        return id;
    }


    @SuppressWarnings("unused")
    public <K> void buildIndex(IndexMapper<T, K> keyMapper) {
        table.buildIndex(keyMapper);
    }

    public ReadonlyArray<T> merge(ReadonlyArray<T> that) {
        var mergedTable = this.table.merge(that.table);
        return new ReadonlyArray<>(genericType, elementType, mergedTable);
    }

    public <R> List<R> map(Function<T, R> mapper) {
        return mapAndFilter(mapper, t -> true);
    }

    public <R> List<R> mapAndFilter(Function<T, R> mapper, Predicate<R> filter) {
        return table.mapAndFilter(mapper, filter);
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

    public int size() {
        return table.size();
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(table.subList(fromIndex, toIndex));
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

}
