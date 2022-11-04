package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.LoadingOption;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.instance.persistence.mappers.IndexItemMapper;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.instance.persistence.mappers.RelationMapper;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

@Component
public class InstanceStore {

    private static final long MAX_INSTANCES_PER_TYPE = 50;

    @Autowired
    private InstanceMapper instanceMapper;

    @Autowired
    private RelationMapper relationMapper;

    @Autowired
    private IndexItemMapper indexItemMapper;

    public void save(ContextDifference diff) {
        if(NncUtils.isNotEmpty(diff.inserts())) {
            instanceMapper.batchInsert(NncUtils.map(diff.inserts(), Instance::toPO));
        }
        if(NncUtils.isNotEmpty(diff.updates())) {
            instanceMapper.batchUpdate(NncUtils.map(diff.updates(), Instance::toPO));
        }
        if(NncUtils.isNotEmpty(diff.deleteVersions())) {
            instanceMapper.batchDelete(diff.tenantId(), diff.deleteVersions());
        }
        if(NncUtils.isNotEmpty(diff.relationsToInsert())) {
            relationMapper.batchInsert(NncUtils.map(diff.relationsToInsert(), InstanceRelation::toPO));
        }
        if(NncUtils.isNotEmpty(diff.relationsToRemove())) {
            relationMapper.batchDelete(NncUtils.map(diff.relationsToRemove(), InstanceRelation::toPO));
        }
    }

    public List<Instance> selectByKey(IndexKeyPO key, InstanceContext context) {
        List<IndexItemPO> indexItems = indexItemMapper.selectByKeys(context.getTenantId(), List.of(key));
        return context.batchGet(NncUtils.map(indexItems, IndexItemPO::getInstanceId));
    }

    public boolean updateSyncVersion(List<VersionPO> versions) {
        return instanceMapper.updateSyncVersion(versions) == versions.size();
    }

    public List<Instance> loadByTypeIds(List<Long> modelIds, long start, long limit, InstanceContext context) {
        List<InstancePO> records = instanceMapper.selectByTypeIds(context.getTenantId(), modelIds, start, limit);
        return createInstances(records, context);
    }

    public List<Instance> load(Collection<Long> ids, InstanceContext context) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<InstancePO> records = instanceMapper.selectByIds(context.getTenantId(), ids);
        Set<Long> typeIds = NncUtils.mapUnique(records, InstancePO::typeId);
        context.getEntityContext().batchGet(Type.class, typeIds, LoadingOption.ENUM_CONSTANTS_LAZY_LOADING);
        return createInstances(records, context);
    }

    public List<Instance> createInstances(List<InstancePO> records, InstanceContext context) {
        List<Long> ids = NncUtils.map(records, InstancePO::id);
        Map<Long, List<RelationPO>> relationMap;
        if(NncUtils.isEmpty(ids)) {
            relationMap = Map.of();
        }
        else {
            List<RelationPO> relations = relationMapper.selectBySourceIds(context.getTenantId(), ids);
            relationMap = NncUtils.toMultiMap(relations, RelationPO::getSrcInstanceId);
        }
        List<Instance> instances = new ArrayList<>();
        for (InstancePO record : records) {
            instances.add(
                    new Instance(
                            record,
                            relationMap.get(record.id()),
                            context
                    )
            );
        }
        return instances;
    }

    public Map<Long, String> loadTitles(long tenantId, List<Long> ids) {
        if(NncUtils.isEmpty(ids)) {
            return Map.of();
        }
        List<InstanceTitlePO> titlePOs =  instanceMapper.selectTitleByIds(tenantId, ids);
        return NncUtils.toMap(titlePOs, InstanceTitlePO::id, InstanceTitlePO::title);
    }

    public List<Instance> getByTypeIds(Collection<Long> typeIds, InstanceContext context) {
        List<InstancePO> instancePOs = instanceMapper.selectByTypeIds(context.getTenantId(), typeIds, 0,
                typeIds.size() * MAX_INSTANCES_PER_TYPE);
        return context.batchGet(NncUtils.map(instancePOs, InstancePO::id));
    }
}
