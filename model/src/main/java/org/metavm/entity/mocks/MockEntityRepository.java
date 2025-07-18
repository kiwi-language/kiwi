package org.metavm.entity.mocks;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityMemoryIndex;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.*;
import org.metavm.util.IdentitySet;
import org.metavm.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MockEntityRepository implements EntityRepository {
    private long nextTreeId = 1000000;

    private final IdentitySet<Object> objects = new IdentitySet<>();
    private final Map<Id, Entity> entities = new HashMap<>();
    private final EntityMemoryIndex index = new EntityMemoryIndex();

    @Override
    public <T> T getEntity(Class<T> entityType, Id id) {
        return entityType.cast(entities.get(id));
    }

    @Override
    public Reference createReference(Id id) {

        return new EntityReference(id, () -> {
            var e = entities.get(id);
            if (e == null) {
                throw new NullPointerException("Cannot find entity for ID " + id);
            }
            return e;
        });
    }

    @Override
    public <T extends Instance> T bind(T entity) {
        Utils.require(objects.add(entity));
        if (entity instanceof Entity e) {
            e.forEachDescendant(d -> {
                if (d.tryGetId() != null && d instanceof Entity e1)
                    entities.put(d.getId(), e1);
            });
        }
        index.save(entity);
        return entity;
    }

    @Override
    public void updateMemoryIndex(ClassInstance entity) {
        index.save(entity);
    }

    @Override
    public long allocateTreeId() {
        return nextTreeId++;
    }

    @Override
    public boolean remove(Instance instance) {
        if (objects.remove(instance)) {
            if (instance.tryGetId() != null)
                entities.remove(instance.getId());
            index.remove(instance);
            return true;
        }
        else
            return false;
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Value... values) {
        return index.selectByKey(indexDef, List.of(values));
    }

}
