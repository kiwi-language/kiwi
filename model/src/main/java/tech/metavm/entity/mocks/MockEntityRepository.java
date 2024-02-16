package tech.metavm.entity.mocks;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityMemoryIndex;
import tech.metavm.entity.EntityRepository;
import tech.metavm.entity.IndexDef;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockEntityRepository implements EntityRepository {

    private final IdentitySet<Object> objects = new IdentitySet<>();
    private final Map<RefDTO, Entity> entities = new HashMap<>();
    private final EntityMemoryIndex index = new EntityMemoryIndex();

    @Override
    public <T> T getEntity(Class<T> entityType, RefDTO ref) {
        return entityType.cast(entities.get(ref));
    }

    @Override
    public <T> T bind(T entity) {
        NncUtils.requireTrue(objects.add(entity));
        if (entity instanceof Entity e) {
            e.forEachDescendant(d -> {
                if (d.getRef().isNotEmpty())
                    entities.put(d.getRef(), d);
            });
        }
        index.save(entity);
        return entity;
    }

    @Override
    public boolean tryBind(Object entity) {
        bind(entity);
        return true;
    }

    @Override
    public boolean remove(Object object) {
        if(object instanceof Entity entity && entity.getRef().isNotEmpty())
            entities.remove(entity.getRef());
        index.remove(object);
        return true;
    }

    @Override
    public boolean containsModel(Object object) {
        return objects.contains(object);
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values) {
        return index.selectByKey(indexDef, List.of(values));
    }

}
