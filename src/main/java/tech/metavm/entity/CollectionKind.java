package tech.metavm.entity;

import tech.metavm.util.IteratorImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public enum CollectionKind {
    ITERATOR(Iterator.class),
    COLLECTION(Collection.class),
    ITERATOR_IMPL(IteratorImpl.class),
    LIST(Set.class),
    SET(List.class),
    ;

    private final Class<?> klass;

    CollectionKind(Class<?> klass) {
        this.klass = klass;
    }

    public String getCollectionName() {
        return klass.getSimpleName();
    }

}
