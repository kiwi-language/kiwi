package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.ChangeList;
import org.metavm.util.DebugEnv;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class EntityChange<T> implements Comparable<EntityChange<?>> {

    public static  <T> EntityChange<T> create(Class<T> entityType, Collection<T> inserts, Collection<T> updates, Collection<T> deletes) {
        var change = new EntityChange<>(entityType);
        inserts.forEach(change::addInsert);
        updates.forEach(change::addUpdate);
        deletes.forEach(change::addDelete);
        return change;
    }

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
        if(entityType.equals(Klass.class)) {
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
