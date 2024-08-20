package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.StoreLoadRequest;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.util.ChangeList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BufferedInstanceStore implements IInstanceStore {

    private final IInstanceStore wrapped;
    private final WAL wal;

    public BufferedInstanceStore(IInstanceStore wrapped, WAL wal) {
        this.wrapped = wrapped;
        this.wal = wal;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        wal.saveInstances(diff);
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        return wrapped.getVersions(ids, context);
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByKeys(List<IndexKeyPO> keys, IInstanceContext context) {
        return wrapped.getIndexEntriesByKeys(keys, context);
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        wal.saveReferences(refChanges);
    }

    @Override
    public void saveIndexEntries(ChangeList<IndexEntryPO> changes) {
        wal.saveIndexEntries(changes);
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context) {
        wal.saveInstanceLogs(instanceLogs);
    }

    @Override
    public ReferencePO getFirstReference(long appId, Set<Id> targetIds, Set<Long> excludedSourceIds) {
        return wrapped.getFirstReference(appId, targetIds, excludedSourceIds);
    }

    @Override
    public List<ReferencePO> getAllStrongReferences(long appId, Set<Id> targetIds, Set<Long> excludedSourceIds) {
        return wrapped.getAllStrongReferences(appId, targetIds, excludedSourceIds);
    }

    @Override
    public List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return wrapped.indexScan(from, to, context);
    }

    @Override
    public long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return wrapped.indexCount(from, to, context);
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        return wrapped.query(query, context);
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        return wrapped.count(query, context);
    }

    @Override
    public List<InstancePO> load(StoreLoadRequest request, IInstanceContext context) {
        return wrapped.load(request, context);
    }

    @Override
    public List<Long> getByReferenceTargetId(Id targetId, long startIdExclusive, long limit, IInstanceContext context) {
        return wrapped.getByReferenceTargetId(targetId, startIdExclusive, limit, context);
    }

    @Override
    public List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context) {
        return wrapped.loadForest(ids, context);
    }

    @Override
    public List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context) {
        return wrapped.scan(queries, context);
    }

    @Override
    public List<Long> scan(long appId, long startId, long limit) {
        return wrapped.scan(appId, startId, limit);
    }

    @Override
    public void updateSyncVersion(List<VersionPO> versions) {
        wrapped.updateSyncVersion(versions);
    }

    @Override
    public List<IndexEntryPO> queryEntries(InstanceIndexQuery query, IInstanceContext context) {
        return wrapped.queryEntries(query, context);
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context) {
        return wrapped.getIndexEntriesByInstanceIds(instanceIds, context);
    }
}
