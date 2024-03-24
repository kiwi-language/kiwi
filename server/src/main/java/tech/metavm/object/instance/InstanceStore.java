package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TreeVersion;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.instance.persistence.mappers.ReferenceMapper;
import tech.metavm.util.ChangeList;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class InstanceStore extends BaseInstanceStore {

    protected final InstanceMapper instanceMapper;
    private final IndexEntryMapper indexEntryMapper;
    protected final ReferenceMapper referenceMapper;

    public InstanceStore(InstanceMapper instanceMapper,
                         IndexEntryMapper indexEntryMapper,
                         ReferenceMapper referenceMapper) {
        this.instanceMapper = instanceMapper;
        this.indexEntryMapper = indexEntryMapper;
        this.referenceMapper = referenceMapper;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        try (var entry = ContextUtil.getProfiler().enter("InstanceStore.save")) {
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
            return instanceMapper.selectVersions(context.getAppId(), ids);
        }
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
    public ReferencePO getFirstReference(long appId, Set<Id> targetIds, Set<Id> excludedSourceIds) {
        if (targetIds.isEmpty())
            return null;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getFirstStrongReferences")) {
            return referenceMapper.selectFirstStrongReference(appId, targetIds, excludedSourceIds);
        }
    }

    @Override
    public List<ReferencePO> getAllStrongReferences(long appId, Set<Id> targetIds, Set<Id> excludedSourceIds) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAllStrongReferences")) {
            return referenceMapper.selectAllStrongReferences(appId, targetIds, excludedSourceIds);
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
            return NncUtils.map(
                    indexEntryMapper.query(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code())),
                    IndexEntryPO::getId
            );
        }
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.count")) {
            return indexEntryMapper.count(PersistenceUtils.toIndexQueryPO(query, context.getAppId(), context.getLockMode().code()));
        }
    }

    @Override
    public List<Id> getByReferenceTargetId(Id targetId, Id startIdExclusive, long limit, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.getByReferenceTargetId")) {
            return NncUtils.map(
                    referenceMapper.selectByTargetId(context.getAppId(), targetId.toBytes(),
                            NncUtils.get(startIdExclusive, Id::toBytes), limit),
                    ReferencePO::getSourceInstanceId
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
            var records = instanceMapper.selectByIds(context.getAppId(), ids,
                    context.getLockMode().code());
//            var typeIds = NncUtils.mapUnique(records, InstancePO::getInstanceId);
//            context.buffer(typeIds);
            return records;
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
