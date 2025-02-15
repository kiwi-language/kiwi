package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.StoreLoadRequest;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.util.ChangeList;
import org.metavm.util.Utils;

import java.util.Collection;
import java.util.List;

public interface IInstanceStore {

    long BY_TYPE_LIMIT = 50;

    void save(long appId, ChangeList<InstancePO> diff);

    List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context);

    List<IndexEntryPO> getIndexEntriesByKeys(List<IndexKeyPO> keys, IInstanceContext context);

    void saveIndexEntries(long appId, ChangeList<IndexEntryPO> changes);

    void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context);

    List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context);

    long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context);

    default List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        return Utils.map(queryEntries(query, context), IndexEntryPO::getId);
    }

    long count(InstanceIndexQuery query, IInstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, IInstanceContext context);

    //    default List<InstancePO> getByTypeIds(Collection<Id> typeIds, IInstanceContext context) {
//        return queryByTypeIds(
//                NncUtils.map(typeIds, typeId -> new ByTypeQuery(typeId, null, BY_TYPE_LIMIT)),
//                context
//        );
//    }

    List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context);

//    List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context);

    List<Long> scan(long appId, long startId, long limit);

    void updateSyncVersion(List<VersionPO> versions);

    List<IndexEntryPO> queryEntries(InstanceIndexQuery query, IInstanceContext context);

    List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context);

    InstanceMapper getInstanceMapper(long appId, String table);

    IndexEntryMapper getIndexEntryMapper(long appId, String table);

//    Set<Id> getAliveInstanceIds(long appId, Set<Id> instanceIds);

}
