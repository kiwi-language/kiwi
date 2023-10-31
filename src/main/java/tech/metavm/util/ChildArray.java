package tech.metavm.util;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class ChildArray<T> extends ReadonlyArray<T> {

    public ChildArray(Class<T> klass) {
        super(klass);
    }

    public ChildArray(TypeReference<T> typeRef) {
        super(typeRef);
    }

    public ChildArray(Type elementType) {
        super(elementType);
    }

    @Override
    protected Class<?> getRawClass() {
        return ChildArray.class;
    }

    public boolean remove(Object value) {
        //noinspection SuspiciousMethodCalls
        return table.remove(value);
    }

    public void addChild(T child) {
        table.add(child);
    }

    public void addChild(int index, T child) {
        table.add(index, child);
    }

    public void addChildren(Iterable<? extends T> children) {
        children.forEach(table::add);
    }

    public void resetChildren(Iterable<? extends T> children) {
        table.clear();
        addChildren(children);
    }

    public void clear() {
        table.clear();
    }

    public void removeIf(Predicate<T> filter) {
        table.removeIf(filter);
    }

    public void addChildAfter(T value, T anchor) {
        table.addAfter(value, anchor);
    }

    public void addFirstChild(T child) {
        table.addFirst(child);
    }
}
