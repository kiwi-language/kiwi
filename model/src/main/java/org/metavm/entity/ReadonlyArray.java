package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReadonlyArray<T> implements Iterable<T> {

    final List<T> table;
    private Type elementType;
    private ModelIdentity identifier;

    public ReadonlyArray(Class<T> klass, Collection<? extends T> data) {
        this(TypeReference.of(klass).getGenericType(), data);
    }

    public ReadonlyArray(Class<T> klass) {
        this(TypeReference.of(klass).getType(), List.of());
    }

    private ReadonlyArray(Type elementType, Collection<? extends T> data) {
        this.elementType = elementType;
        table = new ArrayList<>(data);
    }


    @NoProxy
    public void initialize(ParameterizedType type, Collection<? extends T> data) {
        this.elementType = type.getActualTypeArguments()[0];
        table.clear();
        table.addAll(data);
    }

    public T get(int index) {
        return table.get(index);
    }

    public <K> T get(IndexMapper<? super T, K> keyMapper, K key) {
        return Utils.find(table, t -> Objects.equals(keyMapper.apply(t), key));
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
        return Utils.safeCall(identifier, ModelIdentity::name);
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

    // Should only be used as WeakHashMap keys and CopyVisitor
    public List<T> secretlyGetTable() {
        return table;
    }

    public Stream<T> stream() {
        return table.stream();
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ReadonlyArray<?> thatArray)
            return table.equals(thatArray.table);
        else
            return false;
    }

}
