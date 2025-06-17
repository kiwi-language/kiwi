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
import org.metavm.system.RegionConstants;
import org.metavm.util.ChangeList;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class InstanceStore extends BaseInstanceStore {

    public static final Logger logger = LoggerFactory.getLogger(InstanceStore.class);

    protected final MapperRegistry mapperRegistry;
    private final String instanceTable;
    private final String indexEntryTable;

    public InstanceStore(MapperRegistry mapperRegistry) {
        this(mapperRegistry, "instance", "index_entry");
    }

    public InstanceStore(MapperRegistry mapperRegistry, String instanceTable, String indexEntryTable) {
        this.mapperRegistry = mapperRegistry;
        this.instanceTable = instanceTable;
        this.indexEntryTable = indexEntryTable;
    }

    @Override
    public void save(long appId, ChangeList<InstancePO> diff) {
        try (var entry = ContextUtil.getProfiler().enter("InstanceStore.save")) {
            var mapper = mapperRegistry.getInstanceMapper(appId, "instance");
            entry.addMessage("numChanges", diff.inserts().size() + diff.updates().size() + diff.deletes().size());
            diff.apply(
                    mapper::batchInsert,
                    mapper::batchUpdate,
                    mapper::batchDelete1
            );
        }
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        try (var entry = context.getProfiler().enter("getRootVersions")) {
            entry.addMessage("numIds", ids.size());
            var mapper = getInstanceMapper(context.getAppId());
            var systemIds = Utils.filter(ids, RegionConstants::isSystemId);
            var nonSystemIds = Utils.exclude(ids, RegionConstants::isSystemId);
            List<TreeVersion> systemTrees = systemIds.isEmpty() ? List.of() : mapper.selectVersions(Constants.ROOT_APP_ID, systemIds);
            List<TreeVersion> nonSystemTrees = nonSystemIds.isEmpty() ? List.of() : mapper.selectVersions(context.getAppId(), nonSystemIds);
            var treeMap = new HashMap<Long, TreeVersion>();
            systemTrees.forEach(v -> treeMap.put(v.id(), v));
            nonSystemTrees.forEach(v -> treeMap.put(v.id(), v));
            return Utils.mapAndFilter(ids, treeMap::get, Objects::nonNull);
        }
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByKeys(List<IndexKeyPO> keys, IInstanceContext context) {
        return getIndexEntryMapper(context.getAppId()).selectByKeys(context.getAppId(), keys);
    }

    @Override
    public List<IndexEntryPO> getIndexEntriesByInstanceIds(Collection<Id> instanceIds, IInstanceContext context) {
        return getIndexEntryMapper(context.getAppId()).selectByInstanceIds(context.getAppId(), Utils.map(instanceIds, Id::toBytes));
    }

    @Override
    public void saveIndexEntries(long appId, ChangeList<IndexEntryPO> changes) {
        changes.apply(
                getIndexEntryMapper(appId)::batchInsert,
                i -> {},
                getIndexEntryMapper(appId)::batchDelete
        );
    }

    @Override
    public void saveInstanceLogs(List<InstanceLog> instanceLogs, IInstanceContext context) {
//        var changeLog = new ChangeLog(instanceLogs);
//        changeLog.save(context.getAppId());
    }

    @Override
    public List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return Utils.map(getIndexEntryMapper(context.getAppId()).scan(context.getAppId(), from, to),
                IndexEntryPO::getId);
    }

    @Override
    public long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return getIndexEntryMapper(context.getAppId()).countRange(context.getAppId(), from, to);
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.query")) {
            return Utils.map(queryEntries(query, context), IndexEntryPO::getId);
        }
    }

    @Override
    public List<IndexEntryPO> queryEntries(InstanceIndexQuery query, IInstanceContext context) {
        return getIndexEntryMapper(context.getAppId()).query(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.count")) {
            return getIndexEntryMapper(context.getAppId()).count(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
        }
    }

    public List<Long> scan(long appId, long startId, long limit) {
        return getInstanceMapper(appId).scanTrees(appId, startId, limit);
    }

    public void updateSyncVersion(List<VersionPO> versions) {
        if (versions.isEmpty()) return;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.updateSyncVersion")) {
            var appId = versions.getFirst().appId();
            var mapper = getInstanceMapper(appId);
            Utils.doInBatch(versions, mapper::updateSyncVersion);
        }
    }

    @Override
    public List<InstancePO> loadForest(Collection<Long> ids, IInstanceContext context) {
        if (Utils.isEmpty(ids))
            return List.of();
        try (var entry = context.getProfiler().enter("InstanceStore.loadForest")) {
            entry.addMessage("numInstances", ids.size());
            if (entry.isVerbose())
                entry.addMessage("ids", ids);
            return getInstanceMapper(context.getAppId()).selectByIds(context.getAppId(), ids);
        }
    }

    @Override
    protected List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context) {
        try (var entry = context.getProfiler().enter("InstanceStore.loadInternally")) {
            entry.addMessage("numInstances", request.ids().size());
            if (entry.isVerbose()) {
                entry.addMessage("ids", request.ids());
            }
            if (Utils.isEmpty(request.ids())) {
                return List.of();
            }
            return getInstanceMapper(context.getAppId()).selectByIds(context.getAppId(), request.ids());
        }
    }

    public InstanceMapper getInstanceMapper(long appId) {
        return getInstanceMapper(appId, instanceTable);
    }

    public IndexEntryMapper getIndexEntryMapper(long appId) {
        return getIndexEntryMapper(appId, indexEntryTable);
    }

    @Override
    public InstanceMapper getInstanceMapper(long appId, String table) {
        return mapperRegistry.getInstanceMapper(appId, table);
    }

    @Override
    public IndexEntryMapper getIndexEntryMapper(long appId, String table) {
        return mapperRegistry.getIndexEntryMapper(appId, table);
    }

}
