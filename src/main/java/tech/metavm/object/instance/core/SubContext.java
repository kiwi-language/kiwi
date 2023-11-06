package tech.metavm.object.instance.core;

import tech.metavm.entity.Value;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.Type;

import java.util.*;

public final class SubContext {
    private final Set<Value> values = new LinkedHashSet<>();
    private final IdentityHashMap<InstancePO, InstancePO> entities = new IdentityHashMap<>();
    private final Map<Long, InstancePO> entityMap = new HashMap<>();
    private final Set<ReferencePO> references = new HashSet<>();

    public InstancePO get(long id) {
        return entityMap.get(id);
    }

    public void add(InstancePO instancePO, Type type) {
        Objects.requireNonNull(instancePO);
        if (instancePO.getId() != null) {
            InstancePO existing = entityMap.remove(instancePO.getId());
            if (existing != null) {
                entities.remove(existing);
            }
            entityMap.put(instancePO.getId(), instancePO);
        }
        entities.put(instancePO, instancePO);
        references.addAll(type.extractReferences(instancePO));
    }

    public Set<ReferencePO> getReferences() {
        return references;
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

    public void clear() {
        entities.clear();
        entityMap.clear();
        references.clear();
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

    List<InstancePO> getEntities() {
        return new ArrayList<>(entities.values());
    }

}
