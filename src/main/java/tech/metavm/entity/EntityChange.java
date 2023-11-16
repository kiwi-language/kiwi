package tech.metavm.entity;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;

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

    public void addAllToInsert(List<T> inserts) {
        toInserts.addAll(inserts);
    }

    public void addAllToDelete(List<T> deletes) {
        toDelete.addAll(deletes);
    }

    public void addToUpdate(T entity) {
        toUpdate.add(entity);
    }

    public void addToDelete(T entity) {
        toDelete.add(entity);
    }

//    public void apply(EntityStore<T> store) {
//        if(NncUtils.isNotEmpty(toInserts)) {
//            store.batchInsert(toInserts);
//        }
//        if(NncUtils.isNotEmpty(toUpdate)) {
//            store.batchUpdate(toUpdate);
//        }
//        if(NncUtils.isNotEmpty(toDelete)) {
//            store.batchDelete(toDelete);
//        }
//    }

    public List<T> inserts() {
        return toInserts;
    }

    public List<T> updates() {
        return toUpdate;
    }

    public List<T> deletes() {
        return toDelete;
    }

    public List<T> insertsAndUpdates() {
        return NncUtils.union(inserts(), updates());
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
