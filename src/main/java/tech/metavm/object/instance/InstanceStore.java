package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapperGateway;
import tech.metavm.object.instance.persistence.mappers.ReferenceMapper;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class InstanceStore extends BaseInstanceStore {

    private final InstanceMapperGateway instanceMapperGateway;
    private final IndexEntryMapper indexEntryMapper;
    private final ReferenceMapper referenceMapper;

    public InstanceStore(InstanceMapperGateway instanceMapperGateway,
                         IndexEntryMapper indexEntryMapper,
                         ReferenceMapper referenceMapper) {
        this.instanceMapperGateway = instanceMapperGateway;
        this.indexEntryMapper = indexEntryMapper;
        this.referenceMapper = referenceMapper;
    }

    @Override
    public void save(ChangeList<InstancePO> diff) {
        diff.apply(
                instanceMapperGateway::batchInsert,
                instanceMapperGateway::batchUpdate,
                instanceMapperGateway::batchDelete
        );
    }

    @Override
    public void saveReferences(ChangeList<ReferencePO> refChanges) {
        refChanges.apply(
                referenceMapper::batchInsert,
                referenceMapper::batchUpdate,
                referenceMapper::batchDelete
        );
    }

    @Override
    public List<ReferencePO> getFirstStrongReferences(long tenantId, Set<Long> targetIds, Set<Long> excludedSourceIds) {
        System.out.println("getFirstStrongReferences. tenantId: "+ tenantId +
                ", targetIds: " + targetIds + ", excludedSourceIds: " + excludedSourceIds);
        return referenceMapper.selectFirstStrongReferences(tenantId, targetIds, excludedSourceIds);
    }


    @Override
    public List<ReferencePO> getAllStrongReferences(long tenantId, Set<Long> targetIds, Set<Long> excludedSourceIds) {
        return referenceMapper.selectAllStrongReferences(tenantId, targetIds, excludedSourceIds);
    }


    @Override
    public List<Long> selectByKey(IndexKeyPO key, IInstanceContext context) {
        List<IndexEntryPO> indexItems = indexEntryMapper.selectByKeys(context.getTenantId(), List.of(key));
        return NncUtils.map(indexItems, IndexEntryPO::getInstanceId);
    }

    @Override
    public List<Long> query(InstanceIndexQuery query, IInstanceContext context) {
        return NncUtils.map(
                indexEntryMapper.query(query.toPO(context.getTenantId())),
                IndexEntryPO::getInstanceId
        );
    }

    @Override
    public List<Long> getByReferenceTargetId(long targetId, long startIdExclusive, long limit, IInstanceContext context) {
        return NncUtils.map(
                referenceMapper.selectByTargetId(context.getTenantId(), targetId, startIdExclusive, limit),
                ReferencePO::getSourceId
        );
    }

    @Override
    public List<InstancePO> queryByTypeIds(List<ByTypeQuery> queries, IInstanceContext context) {
        return instanceMapperGateway.selectByTypeIds(context.getTenantId(), queries);
    }

    @Override
    public List<InstancePO> scan(List<ScanQuery> queries, IInstanceContext context) {
        return instanceMapperGateway.scanInstances(context.getTenantId(), queries);
    }

    public boolean updateSyncVersion(List<VersionPO> versions) {
        return instanceMapperGateway.updateSyncVersion(versions) == versions.size();
    }

    @Override
    protected List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context) {
        if(NncUtils.isEmpty(request.ids())) {
            return List.of();
        }
        List<InstancePO> records = instanceMapperGateway.selectByIds(context.getTenantId(), request.ids());
        Set<Long> typeIds = NncUtils.mapUnique(records, InstancePO::getTypeId);
        context.preload(typeIds, LoadingOption.ENUM_CONSTANTS_LAZY_LOADING);
        return records;
    }

    @Override
    public Set<Long> getAliveInstanceIds(long tenantId, Set<Long> instanceIds) {
        return new HashSet<>(instanceMapperGateway.getAliveIds(tenantId, instanceIds));
    }

    public void loadTitles(List<Long> ids, IInstanceContext context) {
        if(NncUtils.isEmpty(ids)) {
            return;
        }
        List<InstanceTitlePO> titlePOs =  instanceMapperGateway.selectTitleByIds(context.getTenantId(), ids);
        Map<Long, String> currentTitleMap = context.getAttribute(ContextAttributeKey.INSTANCE_TITLES);
        currentTitleMap.putAll(
                NncUtils.toMap(titlePOs, InstanceTitlePO::id, InstanceTitlePO::title)
        );
    }
//
//    @Override
//    public List<InstancePO> getByTypeIds(Collection<Long> typeIds,
//                                         long startIdExclusive,
//                                         long limit,
//                                         IInstanceContext context)
//    {
//        List<InstancePO> instancePOs =  instanceMapperGateway.selectByTypeIds(
//                context.getTenantId(), typeIds, startIdExclusive, limit
//        );
//        clearStaleReferences(instancePOs, context);
//        return instancePOs;
//    }

    public String getTitle(Long id, InstanceContext context) {
        Map<Long, String> titleMap = context.getAttribute(ContextAttributeKey.INSTANCE_TITLES);
        String title = titleMap.get(id);
        if(title != null) {
            return title;
        }
        return context.get(id).getTitle();
    }

}
