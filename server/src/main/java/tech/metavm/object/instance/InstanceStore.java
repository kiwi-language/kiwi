package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.instance.persistence.mappers.ReferenceMapper;
import tech.metavm.util.ChangeList;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.HashSet;
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
    public List<Long> getVersions(List<Long> ids) {
        try (var ignored = ContextUtil.getProfiler().enter("getVersions")) {
            return instanceMapper.selectVersions(ids);
        }
    }

    @Override
    public List<Version> getRootVersions(List<Long> ids, IInstanceContext context) {
        try(var entry = context.getProfiler().enter("getRootVersions")) {
            entry.addMessage("numIds", ids.size());
            return instanceMapper.selectRootVersions(context.getAppId().getPhysicalId(), ids);
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
    public ReferencePO getFirstReference(long appId, Set<Long> targetIds, Set<Long> excludedSourceIds) {
        if(targetIds.isEmpty())
            return null;
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getFirstStrongReferences")) {
            return referenceMapper.selectFirstStrongReference(appId, targetIds, excludedSourceIds);
        }
    }

    @Override
    public List<ReferencePO> getAllStrongReferences(long appId, Set<Id> targetIds, Set<Id> excludedSourceIds) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAllStrongReferences")) {
            return referenceMapper.selectAllStrongReferences(appId,
                    NncUtils.map(targetIds, Id::getPhysicalId), NncUtils.map(excludedSourceIds, Id::getPhysicalId));
        }
    }

    @Override
    public List<Id> indexScan(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return NncUtils.map(indexEntryMapper.scan(context.getAppId().getPhysicalId(), from, to),
                IndexEntryPO::getId);
    }

    @Override
    public long indexCount(IndexKeyPO from, IndexKeyPO to, IInstanceContext context) {
        return indexEntryMapper.countRange(context.getAppId().getPhysicalId(), from, to);
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.query")) {
            return NncUtils.map(
                    indexEntryMapper.query(PersistenceUtils.toIndexQueryPO(query,context.getAppId().getPhysicalId(), context.getLockMode().code())),
                    IndexEntryPO::getId
            );
        }
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.count")) {
            return indexEntryMapper.count(PersistenceUtils.toIndexQueryPO(query, context.getAppId().getPhysicalId(), context.getLockMode().code()));
        }
    }

    @Override
    public List<Id> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.getByReferenceTargetId")) {
            return NncUtils.map(
                    referenceMapper.selectByTargetId(context.getAppId().getPhysicalId(), targetId, startIdExclusive, limit),
                    r -> PhysicalId.of(r.getSourceId(), TypeTag.fromCode(r.getTargetTypeTag()), r.getSourceId())
            );
        }
    }

    @Override
    public List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.queryByTypeIds")) {
            return instanceMapper.selectByTypeIds(context.getAppId().getPhysicalId(), queries);
        }
    }

    @Override
    public List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.scan")) {
            return instanceMapper.scan(context.getAppId().getPhysicalId(), queries);
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
            var records = instanceMapper.selectForest(context.getAppId().getPhysicalId(), ids,
                    context.getLockMode().code());
            var typeIds = NncUtils.mapUnique(records, r -> PhysicalId.of(r.getId(), TypeTag.fromCode(r.getTypeTag()), r.getTypeId()));
            context.buffer(typeIds);
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
            List<InstancePO> records = instanceMapper.selectByIds(context.getAppId().getPhysicalId(), request.ids(),
                    context.getLockMode().code());
            Set<Id> typeIds = NncUtils.mapUnique(records,
                    r -> PhysicalId.of(r.getTypeId(), TypeTag.fromCode(r.getTypeTag()), r.getTypeId()));
            context.buffer(typeIds);
            return records;
        }
    }

    @Override
    public Set<Long> getAliveInstanceIds(long appId, Set<Long> instanceIds) {
        if(NncUtils.isEmpty(instanceIds))
            return Set.of();
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAliveInstanceIds")) {
            return new HashSet<>(instanceMapper.getAliveIds(appId, instanceIds));
        }
    }

}
