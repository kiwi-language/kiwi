package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

import java.util.*;
import java.util.function.ToLongFunction;

@Slf4j
public class MigrationInstanceStore implements IInstanceStore {

    private final IInstanceStore wrapped;

    public MigrationInstanceStore(@NotNull IInstanceStore wrapped) {
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
        return Utils.map(queryEntries(query, context), IndexEntryPO::getId);
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
        if (ids.isEmpty())
            return List.of();
        var mapper = getInstanceMapper(context.getAppId(), "instance_tmp");
        var migrated = mapper.selectByIds(context.getAppId(), ids);
        var removedIds = new HashSet<>(mapper.filterDeletedIds(ids));
        var unmigratedIds = Utils.exclude(ids, removedIds::contains);
        var unmigrated = wrapped.loadForest(unmigratedIds, context);
        return mergeResults(migrated, unmigrated, Comparator.comparing(InstancePO::getId), InstancePO::getVersion);
    }

    private <T> List<T> mergeResults(List<T> list1, List<T> list2, Comparator<T> comparator, ToLongFunction<T> getVersion) {
        var it1 = list1.stream().sorted(comparator).iterator();
        var it2 = list2.stream().sorted(comparator).iterator();
        var result = new ArrayList<T>();
        T i1 = null;
        T i2 = null;
        for (;;) {
            if (i1 == null && it1.hasNext())
                i1 = it1.next();
            if (i2 == null && it2.hasNext())
                i2 = it2.next();
            if (i1 == null || i2 == null)
                break;
            var r = comparator.compare(i1, i2);
            if (r < 0) {
                result.add(i1);
                i1 = null;
            } else if (r > 0) {
                result.add(i2);
                i2 = null;
            } else {
                if (getVersion.applyAsLong(i1) >= getVersion.applyAsLong(i2))
                    result.add(i1);
                else
                    result.add(i2);
                i1 = null;
                i2 = null;
            }
        }
        if (i1 != null) {
            result.add(i1);
            while (it1.hasNext())
                result.add(it1.next());
        }
        else if (i2 != null) {
            result.add(i2);
            while (it2.hasNext())
                result.add(it2.next());
        }
        return result;
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
        var mapper = getIndexEntryMapper(context.getAppId(), "index_entry_tmp");
        var migratedEntries =  mapper.query(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
        var entries = wrapped.queryEntries(query, context);
        var merged = mergeResults(migratedEntries, entries, IndexEntryPO::compareTo, e -> 0);
        if (query.desc())
            merged = merged.reversed();
        if (query.limit() != null)
            return merged.subList(0, Math.min(merged.size(), query.limit().intValue()));
        else
            return merged;
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context) {
        var mapper = getIndexEntryMapper(context.getAppId(), "index_entry_tmp");
        var migratedEntries =  mapper.selectByInstanceIds(context.getAppId(), Utils.map(instanceIds, Id::toBytes));
        var entries = wrapped.getIndexEntriesByInstanceIds(instanceIds, context);
        return mergeResults(migratedEntries, entries, IndexEntryPO::compareTo, e -> 0);
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
