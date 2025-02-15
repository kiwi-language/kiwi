package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.StoreLoadRequest;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.util.ChangeList;
import org.metavm.util.Utils;

import java.util.Collection;
import java.util.List;

public class BufferedInstanceStore implements IInstanceStore {

    private final IInstanceStore wrapped;
    private final WAL wal;

    public BufferedInstanceStore(IInstanceStore wrapped, WAL wal) {
        this.wrapped = wrapped;
        this.wal = wal;
    }

    @Override
    public void save(long appId, ChangeList<InstancePO> diff) {
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
    public void saveIndexEntries(long appId, ChangeList<IndexEntryPO> changes) {
        wal.saveIndexEntries(changes);
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context) {
        wal.saveInstanceLogs(instanceLogs);
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
    public List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context) {
        return wrapped.loadForest(ids, context);
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
        var mapper = getIndexEntryMapper(context.getAppId(), "index_entry_tmp");
        return mapper.selectByInstanceIds(context.getAppId(), Utils.map(instanceIds, Id::toBytes));
    }

    @Override
    public InstanceMapper getInstanceMapper(long appId, String table) {
        return wrapped.getInstanceMapper(appId, table);
    }

    @Override
    public IndexEntryMapper getIndexEntryMapper(long appId, String table) {
        return wrapped.getIndexEntryMapper(appId, table);
    }
}
