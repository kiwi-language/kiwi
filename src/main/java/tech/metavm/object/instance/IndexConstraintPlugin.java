package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.meta.Index;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static tech.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static tech.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

@Component
public class IndexConstraintPlugin implements ContextPlugin {

    private final IndexEntryMapper indexEntryMapper;

    public IndexConstraintPlugin(IndexEntryMapper indexEntryMapper) {
        this.indexEntryMapper = indexEntryMapper;
    }

    @Override
    public void beforeSaving(EntityChange<InstancePO> diff, IInstanceContext context) {
        List<ClassInstance> currentInstances = NncUtils.mapAndFilterByType(
                        diff.insertsAndUpdates(),
                        instancePO -> context.get(instancePO.getId()),
                        ClassInstance.class
                );

        Map<Long, ClassInstance> instanceMap = NncUtils.toMap(currentInstances, Instance::getId);
        List<IndexEntryPO> currentItems = NncUtils.flatMap(
                currentInstances,
                instance -> instance.getUniqueKeys(context.getTenantId())
        );
        List<InstancePO> oldInstances = NncUtils.merge(diff.updates(), diff.deletes());
        List<IndexEntryPO> oldItems = NncUtils.isEmpty(oldInstances) && NncUtils.isEmpty(currentItems) ? List.of() :
                indexEntryMapper.selectByInstanceIdsOrKeys(
                        context.getTenantId(),
                        NncUtils.map(oldInstances, InstancePO::getId),
                        NncUtils.map(currentItems, IndexEntryPO::getKey)
                );

        Map<IndexKeyPO, Long> oldKeyMap = NncUtils.toMap(oldItems, IndexEntryPO::getKey, IndexEntryPO::getInstanceId);
        for (IndexEntryPO currentItem : currentItems) {
            Index constraint =
                    context.getEntityContext().getEntity(Index.class, currentItem.getConstraintId());
            if(!constraint.isUnique() || constraint.containsNull(currentItem.getKey())) {
                continue;
            }
            Long existingInstanceId = oldKeyMap.get(currentItem.getKey());
            if(existingInstanceId != null && !existingInstanceId.equals(currentItem.getInstanceId())) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getInstanceId()), constraint
                );
            }
        }
        diff.setAttribute(OLD_INDEX_ITEMS, oldItems);
        diff.setAttribute(NEW_INDEX_ITEMS, currentItems);
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> difference, IInstanceContext context) {
        List<IndexEntryPO> oldItems = difference.getAttribute(OLD_INDEX_ITEMS);
        List<IndexEntryPO> currentItems = difference.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if(NncUtils.isNotEmpty(changeList.inserts())) {
            indexEntryMapper.batchInsert(changeList.inserts());
        }
        if(NncUtils.isNotEmpty(changeList.deletes())) {
            indexEntryMapper.batchDelete(changeList.deletes());
        }
    }


}
