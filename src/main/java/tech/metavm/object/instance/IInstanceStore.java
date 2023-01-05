package tech.metavm.object.instance;

import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.ChangeList;

import java.util.Collection;
import java.util.List;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(ChangeList<InstancePO> diff);

    List<Long> selectByKey(IndexKeyPO key, InstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, InstanceContext context);

    default List<InstancePO> getByTypeIds(Collection<Long> typeIds, InstanceContext context) {
        return getByTypeIds(typeIds, -1L, BY_TYPE_LIMIT * typeIds.size(), context);
    }

    List<InstancePO> getByTypeIds(Collection<Long> typeIds,
                                  long startIdExclusive,
                                  long limit,
                                  InstanceContext context);

    boolean updateSyncVersion(List<VersionPO> versions);
}
