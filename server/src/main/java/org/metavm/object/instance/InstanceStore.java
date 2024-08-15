package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.StoreLoadRequest;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.object.instance.persistence.mappers.ReferenceMapper;
import org.metavm.system.RegionConstants;
import org.metavm.util.ChangeList;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InstanceStore extends BaseInstanceStore {

    public static final Logger logger = LoggerFactory.getLogger(InstanceStore.class);

    protected final InstanceMapper instanceMapper;
    private final IndexEntryMapper indexEntryMapper;
    protected final ReferenceMapper referenceMapper;
    private final Cache cache;

    public InstanceStore(InstanceMapper instanceMapper,
                         IndexEntryMapper indexEntryMapper,
                         ReferenceMapper referenceMapper,
                         Cache cache) {
        this.instanceMapper = instanceMapper;
        this.indexEntryMapper = indexEntryMapper;
        this.referenceMapper = referenceMapper;
        this.cache = cache;
        WAL.setCommitHook(wal -> {
            save(wal.getInstanceChanges());
            saveReferences(wal.getReferenceChanges());
            saveIndexEntries(wal.getIndexEntryChanges());
        });
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        try (var entry = ContextUtil.getProfiler().enter("InstanceStore.save")) {
            cache.save(diff);
            entry.addMessage("numChanges", diff.inserts().size() + diff.updates().size() + diff.deletes().size());
            diff.apply(
                    instanceMapper::batchInsert,
                    instanceMapper::batchUpdate,
                    instanceMapper::batchDelete1
            );
        }
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        try (var entry = context.getProfiler().enter("getRootVersions")) {
            entry.addMessage("numIds", ids.size());
            var systemIds = NncUtils.filter(ids, RegionConstants::isSystemId);
            var nonSystemIds = NncUtils.exclude(ids, RegionConstants::isSystemId);
            List<TreeVersion> systemTrees = systemIds.isEmpty() ? List.of() : instanceMapper.selectVersions(Constants.ROOT_APP_ID, systemIds);
            List<TreeVersion> nonSystemTrees = nonSystemIds.isEmpty() ? List.of() : instanceMapper.selectVersions(context.getAppId(), nonSystemIds);
            var treeMap = new HashMap<Long, TreeVersion>();
            systemTrees.forEach(v -> treeMap.put(v.id(), v));
            nonSystemTrees.forEach(v -> treeMap.put(v.id(), v));
            return NncUtils.mapAndFilter(ids, treeMap::get, Objects::nonNull);
        }
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByKeys(List<IndexKeyPO> keys, IInstanceContext context) {
        return indexEntryMapper.selectByKeys(context.getAppId(), keys);
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context) {
        return indexEntryMapper.selectByInstanceIds(context.getAppId(), NncUtils.map(instanceIds, Id::toBytes));
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        try (var entry = ContextUtil.getProfiler().enter("InstanceStore.saveReferences")) {
            entry.addMessage("numChanges", refChanges.inserts().size() + refChanges.updates().size() + refChanges.deletes().size());
            refChanges.apply(
                    referenceMapper::batchInsert,
                    referenceMapper::batchUpdate,
                    referenceMapper::batchDelete
            );
        }
    }

    @Override
    public void saveIndexEntries(ChangeList<IndexEntryPO> changes) {
        changes.apply(
                indexEntryMapper::batchInsert,
                i -> {},
                indexEntryMapper::batchDelete
        );
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs) {

    }

    @Override
    public ReferencePO getFirstReference(long appId, Set<Id> targetIds, Set<Long> excludedSourceIds) {
        if (targetIds.isEmpty())
            return null;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getFirstStrongReferences")) {
            return referenceMapper.selectFirstStrongReference(appId, NncUtils.map(targetIds, Id::toBytes), excludedSourceIds);
        }
    }

    @Override
    public List<ReferencePO> getAllStrongReferences(long appId, Set<Id> targetIds, Set<Long> excludedSourceIds) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAllStrongReferences")) {
            return referenceMapper.selectAllStrongReferences(appId, NncUtils.map(targetIds, Id::toBytes), excludedSourceIds);
        }
    }

    @Override
    public List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return NncUtils.map(indexEntryMapper.scan(context.getAppId(), from, to),
                IndexEntryPO::getId);
    }

    @Override
    public long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return indexEntryMapper.countRange(context.getAppId(), from, to);
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.query")) {
            return NncUtils.map(queryEntries(query, context), IndexEntryPO::getId);
        }
    }

    @Override
    public List<IndexEntryPO> queryEntries(InstanceIndexQuery query, IInstanceContext context) {
        return indexEntryMapper.query(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.count")) {
            return indexEntryMapper.count(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
        }
    }

    @Override
    public List<Long> getByReferenceTargetId(Id targetId, long startIdExclusive, long limit, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.getByReferenceTargetId")) {
            return NncUtils.map(
                    referenceMapper.selectByTargetId(context.getAppId(), targetId.toBytes(), startIdExclusive, limit),
                    ReferencePO::getSourceTreeId
            );
        }
    }

//    @Override
//    public List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context) {
//        try (var ignored = context.getProfiler().enter("InstanceStore.queryByTypeIds")) {
//            return instanceMapper.selectByTypeIds(context.getAppId(), queries);
//        }
//    }

    public List<Long> scan(long appId, long startId, long limit) {
        return instanceMapper.scanTrees(appId, startId, limit);
    }

    @Override
    public List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.scan")) {
            return instanceMapper.scan(context.getAppId(), queries);
        }
    }

    public void updateSyncVersion(List<VersionPO> versions) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.updateSyncVersion")) {
            NncUtils.doInBatch(versions, instanceMapper::updateSyncVersion);
        }
    }

    @Override
    public List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context) {
        if (NncUtils.isEmpty(ids))
            return List.of();
        try (var entry = context.getProfiler().enter("InstanceStore.loadForest")) {
            entry.addMessage("numInstances", ids.size());
            if (entry.isVerbose())
                entry.addMessage("ids", ids);
            var hits = cache.batchGet(ids);
            var result = new ArrayList<InstancePO>();
            var missedIds = new ArrayList<Long>();
            NncUtils.biForEach(ids, hits, (id, hit) -> {
                if(hit != null)
                    result.add(PersistenceUtils.buildInstancePO(context.getAppId(), id, hit));
                else
                    missedIds.add(id);
            });
            if(!missedIds.isEmpty()) {
                var records = instanceMapper.selectByIds(context.getAppId(), missedIds,
                        context.getLockMode().code());
                result.addAll(records);
            }
//            var typeIds = NncUtils.mapUnique(records, InstancePO::getInstanceId);
//            context.buffer(typeIds);
            return result;
        }
    }

    @Override
    protected List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context) {
        try (var entry = context.getProfiler().enter("InstanceStore.loadInternally")) {
            entry.addMessage("numInstances", request.ids().size());
            if (entry.isVerbose()) {
                entry.addMessage("ids", request.ids());
            }
            if (NncUtils.isEmpty(request.ids())) {
                return List.of();
            }
            List<InstancePO> records = instanceMapper.selectByIds(context.getAppId(), request.ids(), context.getLockMode().code());
//            Set<Id> typeIds = NncUtils.mapUnique(records,
//                    r -> ((PhysicalId) r.getInstanceId()).getTypeId());
//            context.buffer(typeIds);
            return records;
        }
    }

    //    @Override
//    public Set<Id> getAliveInstanceIds(long appId, Set<Id> instanceIds) {
//        if (NncUtils.isEmpty(instanceIds))
//            return Set.of();
//        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAliveInstanceIds")) {
//            return NncUtils.mapUnique(
//                    instanceMapper.getAliveIds(appId, NncUtils.map(instanceIds, Id::toBytes)),
//                    Id::fromBytes
//            );
//        }
//    }

}
