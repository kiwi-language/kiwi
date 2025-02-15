package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
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
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.metavm.util.Utils;

import java.util.Collection;
import java.util.List;

@Slf4j
public class MigrationInstanceStore implements IInstanceStore {

    private final IInstanceStore wrapped;

    public MigrationInstanceStore(IInstanceStore wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void save(long appId, ChangeList<InstancePO> diff) {
        var tracing = DebugEnv.traceMigration;
        try (var entry = ContextUtil.getProfiler().enter("InstanceStore.save")) {
            var mapper = getInstanceMapper(appId, "instance_tmp");
            entry.addMessage("numChanges", diff.inserts().size() + diff.updates().size() + diff.deletes().size());
            var upserts = Utils.merge(diff.inserts(), diff.updates());
            if (tracing) {
                log.trace("Upsert instances {} to temp instance table", Utils.join(upserts, i -> Long.toString(i.getId())));
                log.trace("Delete instances {} from temp instance table", Utils.join(diff.deletes(), i -> Long.toString(i.getId())));
            }
            mapper.batchUpsert(upserts);
            mapper.tryBatchDelete(appId, System.currentTimeMillis(), Utils.map(diff.deletes(), InstancePO::nextVersion));
        }
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
        var tracing = DebugEnv.traceMigration;
        var mapper = getIndexEntryMapper(appId, "index_entry_tmp");
        if (tracing) {
            for (IndexEntryPO entry : changes.inserts()) {
                log.trace("Inserts entry {} to temp index entry table", entry.toString());
            }
        }
        changes.apply(
                mapper::tryBatchInsert,
                i -> {},
                mapper::batchDelete
        );
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context) {
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
