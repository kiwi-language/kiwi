package tech.metavm.entity.mocks;

import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockEntityRepository implements EntityRepository {

    private final IdentitySet<Object> objects = new IdentitySet<>();
    private final Map<Id, Entity> entities = new HashMap<>();
    private final EntityMemoryIndex index = new EntityMemoryIndex();
    private final TypeRegistry typeRegistry;

    public MockEntityRepository(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    public <T> T getEntity(Class<T> entityType, Id id) {
        return entityType.cast(entities.get(id));
    }

    @Override
    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    @Override
    public <T> T bind(T entity) {
        NncUtils.requireTrue(objects.add(entity));
        if (entity instanceof Entity e) {
            e.forEachDescendant(d -> {
                if (d.tryGetId() != null)
                    entities.put(d.getId(), d);
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
        if(object instanceof Entity entity && entity.tryGetId() != null)
            entities.remove(entity.getId());
        index.remove(object);
        return true;
    }

    @Override
    public boolean containsEntity(Object object) {
        return objects.contains(object);
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values) {
        return index.selectByKey(indexDef, List.of(values));
    }

}
