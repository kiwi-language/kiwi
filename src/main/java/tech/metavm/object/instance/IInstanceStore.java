package tech.metavm.object.instance;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(ChangeList<InstancePO> diff);

    void saveReferences(ChangeList<ReferencePO> refChanges);

    List<ReferencePO> getFirstStrongReferences(long tenantId, Set<Long> targetIds, Set<Long> excludedSourceIds);

    List<ReferencePO> getAllStrongReferences(long tenantId, Set<Long> targetIds, Set<Long> excludedSourceIds);

    List<Long> selectByKey(List<Long> tenantIds, IndexKeyPO key, IInstanceContext context);

    List<Long> query(InstanceIndexQuery query, IInstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, IInstanceContext context);

    List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context);

    default List<InstancePO> getByTypeIds(Collection<Long> typeIds, IInstanceContext context) {
        return queryByTypeIds(
                NncUtils.map(typeIds, typeId -> new ByTypeQuery(typeId, 0L, BY_TYPE_LIMIT)),
                context
        );
    }

    List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context);

    List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context);

    boolean updateSyncVersion(List<VersionPO> versions);

    Set<Long> getAliveInstanceIds(long tenantId, Set<Long> instanceIds);

}
