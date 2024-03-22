package tech.metavm.object.instance;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(ChangeList<InstancePO> diff);

    List<Long> getVersions(List<Id> ids);

    List<Version> getRootVersions(List<Id> ids, IInstanceContext context);

    void saveReferences(ChangeList<ReferencePO> refChanges);

    ReferencePO getFirstReference(long appId, Set<Id> targetIds, Set<Id> excludedSourceIds);

    List<ReferencePO> getAllStrongReferences(long appId, Set<Id> targetIds, Set<Id> excludedSourceIds);

    List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context);

    long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context);

    List<Id> query(InstanceIndexQuery query, IInstanceContext context);

    long count(InstanceIndexQuery query, IInstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, IInstanceContext context);

    List<Id> getByReferenceTargetId(Id targetId, Id startIdExclusive, long limit, IInstanceContext context);

    default List<InstancePO> getByTypeIds(Collection<Id> typeIds, IInstanceContext context) {
        return queryByTypeIds(
                NncUtils.map(typeIds, typeId -> new ByTypeQuery(typeId, null, BY_TYPE_LIMIT)),
                context
        );
    }

    List<InstancePO> loadForest(Collection<Id> ids, IInstanceContext context);

    List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context);

    List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context);

    void updateSyncVersion(List<VersionPO> versions);

    Set<Id> getAliveInstanceIds(long appId, Set<Id> instanceIds);

}
