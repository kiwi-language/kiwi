package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.util.TypeReference;

public interface IEntityContext extends InstanceMap {
    <T> T get(Class<T> klass, Instance instance);

    boolean containsInstance(Instance instance);

    boolean containsKey(EntityKey entityKey);

    <T extends Entity> T getEntity(TypeReference<T> typeReference, long id);

    <T extends Entity> T getEntity(Class<T> entityType, long id);

    void finish();

}
