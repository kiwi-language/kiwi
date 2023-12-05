package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.instance.persistence.mappers.ReferenceMapper;
import tech.metavm.util.ChangeList;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class InstanceStore extends BaseInstanceStore {

    protected final InstanceMapper instanceMapper;
    private final IndexEntryMapper indexEntryMapper;
    private final ReferenceMapper referenceMapper;

    public InstanceStore(InstanceMapper instanceMapper,
                         IndexEntryMapper indexEntryMapper,
                         ReferenceMapper referenceMapper) {
        this.instanceMapper = instanceMapper;
        this.indexEntryMapper = indexEntryMapper;
        this.referenceMapper = referenceMapper;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.save")) {
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
            return instanceMapper.selectRootVersions(context.getAppId(), ids);
        }
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.saveReferences")) {
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
    public List<ReferencePO> getAllStrongReferences(long appId, Set<Long> targetIds, Set<Long> excludedSourceIds) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getAllStrongReferences")) {
            return referenceMapper.selectAllStrongReferences(appId, targetIds, excludedSourceIds);
        }
    }

    @Override
    public List<Long> query(InstanceIndexQuery query, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.query")) {
            return NncUtils.map(
                    indexEntryMapper.query(PersistenceUtils.toIndexQueryPO(query,context.getAppId(), context.getLockMode().code())),
                    IndexEntryPO::getInstanceId
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
    public List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.getByReferenceTargetId")) {
            return NncUtils.map(
                    referenceMapper.selectByTargetId(context.getAppId(), targetId, startIdExclusive, limit),
                    ReferencePO::getSourceId
            );
        }
    }

    @Override
    public List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.queryByTypeIds")) {
            return instanceMapper.selectByTypeIds(context.getAppId(), queries);
        }
    }

    @Override
    public List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context) {
        try (var ignored = context.getProfiler().enter("InstanceStore.scan")) {
            return instanceMapper.scan(context.getAppId(), queries);
        }
    }

    public boolean updateSyncVersion(List<VersionPO> versions) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.updateSyncVersion")) {
            return instanceMapper.updateSyncVersion(versions) == versions.size();
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
            var records = instanceMapper.selectForest(context.getAppId(), ids,
                    context.getLockMode().code());
            var typeIds = NncUtils.mapUnique(records, InstancePO::getTypeId);
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
            List<InstancePO> records = instanceMapper.selectByIds(context.getAppId(), request.ids(),
                    context.getLockMode().code());
            Set<Long> typeIds = NncUtils.mapUnique(records, InstancePO::getTypeId);
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

    public String getTitle(Long id, InstanceContext context) {
        try (var ignored = ContextUtil.getProfiler().enter("InstanceStore.getTitle")) {
            Map<Long, String> titleMap = context.getAttribute(ContextAttributeKey.INSTANCE_TITLES);
            String title = titleMap.get(id);
            if (title != null) {
                return title;
            }
            return context.get(id).getTitle();
        }
    }

}
