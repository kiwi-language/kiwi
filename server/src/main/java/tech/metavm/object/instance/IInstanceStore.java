package tech.metavm.object.instance;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(ChangeList<InstancePO> diff);

    List<Long> getVersions(List<Long> ids);

    List<Version> getRootVersions(List<Long> ids, IInstanceContext context);

    void saveReferences(ChangeList<ReferencePO> refChanges);

    ReferencePO getFirstReference(long appId, Set<Long> targetIds, Set<Long> excludedSourceIds);

    List<ReferencePO> getAllStrongReferences(long appId, Set<Long> targetIds, Set<Long> excludedSourceIds);

    List<Long> query(InstanceIndexQuery query, IInstanceContext context);

    long count(InstanceIndexQuery query, IInstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, IInstanceContext context);

    List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context);

    default List<InstancePO> getByTypeIds(Collection<Long> typeIds, IInstanceContext context) {
        return queryByTypeIds(
                NncUtils.map(typeIds, typeId -> new ByTypeQuery(typeId, 0L, BY_TYPE_LIMIT)),
                context
        );
    }

    List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context);

    List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context);

    List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context);

    void updateSyncVersion(List<VersionPO> versions);

    Set<Long> getAliveInstanceIds(long appId, Set<Long> instanceIds);

}
