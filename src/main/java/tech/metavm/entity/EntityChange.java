package tech.metavm.entity;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.ChangeList;

import java.util.*;
import java.util.function.Consumer;

public class EntityChange<T> implements Comparable<EntityChange<?>> {

    private final Class<T> entityType;
    private final List<T> toInserts = new ArrayList<>();
    private final List<T> toUpdate = new ArrayList<>();
    private final List<T> toDelete = new ArrayList<>();

    private final Map<DifferenceAttributeKey<?>, Object> attributes = new HashMap<>();

    public EntityChange(Class<T> entityType) {
        this.entityType = entityType;
    }

    public void addToInsert(T entity) {
        toInserts.add(entity);
    }

    public void addToUpdate(T entity) {
        toUpdate.add(entity);
    }

    public void addToDelete(T entity) {
        toDelete.add(entity);
    }

    public List<T> inserts() {
        return toInserts;
    }

    public List<T> updates() {
        return toUpdate;
    }

    public void forEachInsertOrUpdate(Consumer<T> action) {
        for (T insert : inserts()) {
            action.accept(insert);
        }
        for (T update : updates()) {
            action.accept(update);
        }
    }

    public void forEachUpdateOrDelete(Consumer<T> action) {
        for (T update : updates()) {
            action.accept(update);
        }
        for (T delete : deletes()) {
            action.accept(delete);
        }
    }

    public List<T> deletes() {
        return toDelete;
    }

    public boolean isEmpty() {
        return toDelete.isEmpty() && toUpdate.isEmpty() && toInserts.isEmpty();
    }

    public ChangeList<T> toChangeList() {
        return new ChangeList<>(
                toInserts,
                toUpdate,
                toDelete
        );
    }

    private static Integer getEntityPriority(Class<?> entityType) {
        if(entityType.equals(ClassType.class)) {
            return 1;
        }
        if(entityType.equals(Field.class)) {
            return 2;
        }
        return 100;
    }

    @Override
    public int compareTo(EntityChange<?> that) {
        return getEntityPriority(entityType).compareTo(getEntityPriority(that.entityType));
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public <K> void setAttribute(DifferenceAttributeKey<K> key, K value) {
        attributes.put(key, value);
    }

    public <K> K getAttribute(DifferenceAttributeKey<K> key) {
        return key.getType().cast(attributes.get(key));
    }

    public Map<DifferenceAttributeKey<?>, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
