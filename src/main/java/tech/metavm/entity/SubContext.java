package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Predicate;

public final class SubContext<T extends Identifiable> {
    private final IdentityHashMap<T, T> entities = new IdentityHashMap<>();
    private final Map<EntityKey, T> entityMap = new HashMap<>();

    public T get(EntityKey key) {
        return entityMap.get(key);
    }

    public void add(T entity) {
        Objects.requireNonNull(entity);
        if (entity.getId() != null) {
            T existing = entityMap.remove(entity.key());
            if (existing != null) {
                entities.remove(existing);
            }
            entityMap.put(entity.key(), entity);
        }
        entities.put(entity, entity);
    }

    public Collection<T> entities() {
        return entities.values();
    }

    public void initIds(java.util.function.Function<Integer, List<Long>> idGenerator) {
        List<T> newEntities = NncUtils.filter(entities(), Identifiable::isIdNull);
        if (NncUtils.isEmpty(newEntities)) {
            return;
        }
        List<Long> ids = idGenerator.apply(newEntities.size());
        NncUtils.biForEach(newEntities, ids, Identifiable::initId);
        for (T newEntity : newEntities) {
            entityMap.put(newEntity.key(), newEntity);
        }
    }

    public void clear() {
        entities.clear();
        entityMap.clear();
    }

    public boolean remove(T entity) {
        T removed = entities.remove(entity);
        if (removed != null) {
            if (removed.key() != null) {
                entityMap.remove(removed.key());
            }
            return true;
        } else {
            return false;
        }
    }

    public List<T> getFiltered(Predicate<T> filter) {
        return NncUtils.filter(entities(), filter);
    }

    public void rebuildFrom(SubContext<T> that) {
        clear();
        for (T entity : that.getEntities()) {
            entity.setPersisted(true);
            add((T) entity.copy());
        }
    }

//        boolean remove(long objectId) {
//            Entity entity = entityMap.get(objectId);
//            if(entity != null) {
//                entities.remove(entity);
//                return true;
//            }
//            else {
//                return false;
//            }
//        }

    List<T> getEntities() {
        return new ArrayList<>(entities.values());
    }

}
