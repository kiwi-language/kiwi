package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.StoreLoadRequest;
import org.metavm.entity.StoreLoadRequestItem;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.util.ChangeList;

import java.util.*;

public class CachingInstanceStore implements IInstanceStore {

    private final IInstanceStore wrapped;
    private final WAL wal;

    public CachingInstanceStore(IInstanceStore wrapped, WAL wal) {
        this.wrapped = wrapped;
        this.wal = wal;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        wrapped.save(diff);
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        return wrapped.getVersions(ids, context);
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByKeys(List<IndexKeyPO> keys, IInstanceContext context) {
        return wal.getIndexEntriesByKeys(keys).mergeWith(wrapped.getIndexEntriesByKeys(keys, context));
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        wrapped.saveReferences(refChanges);
    }

    @Override
    public void saveIndexEntries(ChangeList<IndexEntryPO> changes) {
        wrapped.saveIndexEntries(changes);
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context) {
        wrapped.saveInstanceLogs(instanceLogs, context);
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
    public List<IndexEntryPO> queryEntries(InstanceIndexQuery query, IInstanceContext context) {
        var queryPO = PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code());
        var result = new ArrayList<IndexEntryPO>();
        var walResult = wal.query(queryPO);
        var set = new HashSet<IndexEntryPO>();
        for (IndexEntryPO hit : walResult.hits()) {
            result.add(hit);
            set.add(hit);
        }
        set.addAll(walResult.removed());
        var newQuery = new InstanceIndexQuery(
                query.index(),
                query.from(),
                query.to(),
                query.desc(),
                query.limit() != null ? query.limit() + walResult.removed().size() : null
        );
        var wrappedRs = wrapped.queryEntries(newQuery, context);
        for (var e : wrappedRs) {
            if (!set.contains(e))
                result.add(e);
        }
        if (query.desc())
            result.sort((e1, e2) -> -e1.compareTo(e2));
        else
            result.sort(IndexEntryPO::compareTo);
        if(query.limit() != null && query.limit() < result.size())
            return result.subList(0, query.limit().intValue());
        else
            return result;
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context) {
        return wal.getIndexEntriesByInstanceIds(instanceIds).mergeWith(wrapped.getIndexEntriesByInstanceIds(instanceIds, context));
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        var walResult = wal.query(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
        return wrapped.count(query, context) + walResult.hits().size() - walResult.removed().size();
    }

    @Override
    public List<InstancePO> load(StoreLoadRequest request, IInstanceContext context) {
        var items = new ArrayList<StoreLoadRequestItem>();
        var result = new ArrayList<InstancePO>();
        for (Long id : request.ids()) {
            if (wal.isDeleted(id))
                continue;
            var hit = wal.get(id);
            if (hit != null)
                result.add(hit);
            else
                items.add(new StoreLoadRequestItem(id, Set.of()));
        }
        if (!items.isEmpty()) {
            result.addAll(wrapped.load(new StoreLoadRequest(items), context));
        }
        return result;
    }

    @Override
    public List<Long> getByReferenceTargetId(Id targetId, long startIdExclusive, long limit, IInstanceContext context) {
        return wrapped.getByReferenceTargetId(targetId, startIdExclusive, limit, context);
    }

    @Override
    public List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context) {
        var newIds = new ArrayList<Long>();
        var result = new ArrayList<InstancePO>();
        for (Long id : ids) {
            if (wal.isDeleted(id))
                continue;
            var hit = wal.get(id);
            if (hit != null)
                result.add(hit);
            else
                newIds.add(id);
        }
        if (!newIds.isEmpty())
            result.addAll(wrapped.loadForest(newIds, context));
        return result;
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

    public WAL getWal() {
        return wal;
    }
}
