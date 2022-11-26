package tech.metavm.entity;

import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Type;

import java.util.*;
import java.util.function.Function;

public final class SubContext {
    private final Set<Value> values = new LinkedHashSet<>();
    private final IdentityHashMap<InstancePO, InstancePO> entities = new IdentityHashMap<>();
    private final Map<Long, InstancePO> entityMap = new HashMap<>();

    public InstancePO get(long id) {
        return entityMap.get(id);
    }

    public void add(InstancePO entity) {
        Objects.requireNonNull(entity);
        if (entity.getId() != null) {
            InstancePO existing = entityMap.remove(entity.getId());
            if (existing != null) {
                entities.remove(existing);
            }
            entityMap.put(entity.getId(), entity);
        }
        entities.put(entity, entity);
    }

    public void addValue(Value value) {
        values.add(value);
    }

    public Collection<Value> values() {
        return values;
    }

    public Collection<InstancePO> entities() {
        return entities.values();
    }

//    public void initIds(java.util.function.Function<Map<Long, Integer>, Map<Long, List<Long>>> idGenerator) {
//        List<EntityPO> newEntities = NncUtils.filter(entities(), e -> e.getId() == null);
//        if (NncUtils.isEmpty(newEntities)) {
//            return;
//        }
//        Map<Long, Integer> countMap = NncUtils.mapAndCount(newEntities, EntityPO::getTypeId);
//        Map<Long, List<Long>> typeId2Ids = idGenerator.apply(countMap);
//        Map<Long, List<EntityPO>> typeId2Entities = NncUtils.toMultiMap(newEntities, EntityPO::getTypeId);
//        typeId2Entities.forEach((typeId, entities) -> {
//            List<Long> ids = typeId2Ids.get(typeId);
//            NncUtils.biForEach(entities, ids, e -> e.setId());
//        });
//        for (Entity newEntity : newEntities) {
//            entityMap.put(newEntity.key(), newEntity);
//        }
//    }

    public void clear() {
        entities.clear();
        entityMap.clear();
    }

    public boolean remove(InstancePO entity) {
        InstancePO removed = entities.remove(entity);
        if (removed != null) {
            if (removed.getId() != null) {
                entityMap.remove(removed.getId());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(Value value) {
        return values.remove(value);
    }

//    public List<Entity> getFiltered(Predicate<Entity> filter) {
//        return NncUtils.filter(entities(), filter);
//    }
//
//    public void rebuildFrom(SubContext that) {
//        clear();
//        for (EntityPO entity : that.getEntities()) {
//            entity.setPersisted(true);
//            add(entity.copy());
//        }
//    }

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

    List<InstancePO> getEntities() {
        return new ArrayList<>(entities.values());
    }

}
