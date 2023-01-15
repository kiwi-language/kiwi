package tech.metavm.object.instance;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.util.ChangeList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(ChangeList<InstancePO> diff);

    void saveReferences(ChangeList<ReferencePO> refChanges);

    Set<Long> getStronglyReferencedIds(long tenantId, Set<Long> ids, Set<Long> excludedSourceIds);

    List<Long> selectByKey(IndexKeyPO key, IInstanceContext context);

    List<Long> query(InstanceIndexQuery query, IInstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, IInstanceContext context);

    List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context);

    default List<InstancePO> getByTypeIds(Collection<Long> typeIds, IInstanceContext context) {
        return getByTypeIds(typeIds, -1L, BY_TYPE_LIMIT * typeIds.size(), context);
    }

    List<InstancePO> getByTypeIds(Collection<Long> typeIds,
                                  long startIdExclusive,
                                  long limit,
                                  IInstanceContext context);

    boolean updateSyncVersion(List<VersionPO> versions);

    Set<Long> getAliveInstanceIds(long tenantId, Set<Long> instanceIds);

}
