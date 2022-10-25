package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.constant.ColumnNames;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.instance.persistence.mappers.RelationMapper;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class InstanceStore {

    @Autowired
    private InstanceMapper instanceMapper;

    @Autowired
    private RelationMapper relationMapper;

    public void save(ContextDifference diff) {
//        Map<InstancePO, Instance> toInsertMap = NncUtils.toIdentityMap(diff.instancesToInsert(), Instance::toPO);
        if(NncUtils.isNotEmpty(diff.instancesToInsert())) {
//            instanceMapper.batchInsert(toInsertMap.keySet());
//            toInsertMap.forEach((po, instance) -> instance.initId(po.id()));
            instanceMapper.batchInsert(NncUtils.map(diff.instancesToInsert(), Instance::toPO));
        }
        if(NncUtils.isNotEmpty(diff.instanceToUpdate())) {
            instanceMapper.batchUpdate(NncUtils.map(diff.instanceToUpdate(), Instance::toPO));
        }
        if(NncUtils.isNotEmpty(diff.instanceIdsToRemove())) {
            instanceMapper.batchDelete(diff.tenantId(), diff.instanceIdsToRemove());
        }
        if(NncUtils.isNotEmpty(diff.relationsToInsert())) {
            relationMapper.batchInsert(NncUtils.map(diff.relationsToInsert(), InstanceRelation::toPO));
        }
        if(NncUtils.isNotEmpty(diff.relationsToRemove())) {
            relationMapper.batchDelete(NncUtils.map(diff.relationsToRemove(), InstanceRelation::toPO));
        }
    }

    public boolean updateSyncVersion(List<VersionPO> versions) {
        return instanceMapper.updateSyncVersion(versions) == versions.size();
    }

    public List<Instance> loadByModelIds(List<Long> modelIds, long start, long limit, InstanceContext context) {
        List<InstancePO> records = instanceMapper.selectByModelIds(context.getTenantId(), modelIds, start, limit);
        return createInstances(records, context);
    }

    public List<Instance> load(List<Long> ids, InstanceContext context) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<InstancePO> records = instanceMapper.selectByIds(context.getTenantId(), ids);
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

}
