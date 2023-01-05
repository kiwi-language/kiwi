package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.IndexItemMapper;
import tech.metavm.object.meta.IndexConstraintRT;
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

    private final IndexItemMapper indexItemMapper;

    public IndexConstraintPlugin(IndexItemMapper indexItemMapper) {
        this.indexItemMapper = indexItemMapper;
    }

    @Override
    public void beforeSaving(EntityChange<InstancePO> diff, IInstanceContext context) {
        List<ClassInstance> currentInstances = NncUtils.mapAndFilterByType(
                        diff.insertsAndUpdates(),
                        instancePO -> context.get(instancePO.getId()),
                        ClassInstance.class
                );

        Map<Long, ClassInstance> instanceMap = NncUtils.toMap(currentInstances, Instance::getId);
        List<IndexItemPO> currentItems = NncUtils.flatMap(
                currentInstances,
                instance -> instance.getUniqueKeys(context.getTenantId())
        );
        List<InstancePO> oldInstances = NncUtils.merge(diff.updates(), diff.deletes());
        List<IndexItemPO> oldItems = NncUtils.isEmpty(oldInstances) && NncUtils.isEmpty(currentItems) ? List.of() :
                indexItemMapper.selectByInstanceIdsOrKeys(
                        context.getTenantId(),
                        NncUtils.map(oldInstances, InstancePO::getId),
                        NncUtils.map(currentItems, IndexItemPO::getKey)
                );

        Map<IndexKeyPO, Long> oldKeyMap = NncUtils.toMap(oldItems, IndexItemPO::getKey, IndexItemPO::getInstanceId);
        for (IndexItemPO currentItem : currentItems) {
            IndexConstraintRT constraint =
                    context.getEntityContext().getEntity(IndexConstraintRT.class, currentItem.getConstraintId());
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
        List<IndexItemPO> oldItems = difference.getAttribute(OLD_INDEX_ITEMS);
        List<IndexItemPO> currentItems = difference.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexItemPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if(NncUtils.isNotEmpty(changeList.inserts())) {
            indexItemMapper.batchInsert(changeList.inserts());
        }
        if(NncUtils.isNotEmpty(changeList.deletes())) {
            indexItemMapper.batchDelete(changeList.deletes());
        }
    }


}
