package tech.metavm.entity;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.ChangeList;

import java.util.*;
import java.util.function.Consumer;

public class EntityChange<T> implements Comparable<EntityChange<?>> {

    private final Class<T> entityType;
    private final List<T> inserts = new ArrayList<>();
    private final List<T> updates = new ArrayList<>();
    private final List<T> deletes = new ArrayList<>();

    private final Map<DifferenceAttributeKey<?>, Object> attributes = new HashMap<>();

    public EntityChange(Class<T> entityType) {
        this.entityType = entityType;
    }

    public void addInsert(T entity) {
        inserts.add(entity);
    }

    public void addUpdate(T entity) {
        updates.add(entity);
    }

    public void addDelete(T entity) {
        deletes.add(entity);
    }

    public List<T> inserts() {
        return inserts;
    }

    public List<T> updates() {
        return updates;
    }
    
    public void forEachInsertOrUpdate(Consumer<T> action) {
        inserts.forEach(action);
        updates.forEach(action);
    }

    public void forEachUpdateOrDelete(Consumer<T> action) {
        updates.forEach(action);
        deletes.forEach(action);
    }

    public List<T> deletes() {
        return deletes;
    }

    public boolean isEmpty() {
        return deletes.isEmpty() && updates.isEmpty() && inserts.isEmpty();
    }

    public ChangeList<T> toChangeList() {
        return new ChangeList<>(
                inserts,
                updates,
                deletes
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
