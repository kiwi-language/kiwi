package tech.metavm.entity;

import java.util.List;

public interface EntityStore<T extends Entity> extends ModelStore<T> {

    int batchUpdate(List<T> entities);

    List<EntityPO> load(StoreLoadRequest request, InstanceContext context);

}
