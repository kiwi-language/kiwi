package tech.metavm.object.instance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.type.Index;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static tech.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static tech.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

@Component
@Order(11)
public class IndexConstraintPlugin implements ContextPlugin {

    private final IndexEntryMapper indexEntryMapper;

    public IndexConstraintPlugin(IndexEntryMapper indexEntryMapper) {
        this.indexEntryMapper = indexEntryMapper;
    }

    @Override
    public boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        Map<Long, ClassInstance> instanceMap = new HashMap<>();
        List<IndexEntryPO> currentEntries = new ArrayList<>();
        change.forEachInsertOrUpdate(instancePO -> {
            var instance = context.get(instancePO.getId());
            if(instance instanceof ClassInstance classInstance) {
                instanceMap.put(classInstance.getIdRequired(), classInstance);
                currentEntries.addAll(classInstance.getIndexEntries(context.getEntityContext()));
            }
        });
        List<InstancePO> oldInstances = NncUtils.union(change.updates(), change.deletes());
        Set<Long> oldInstanceIds = NncUtils.mapUnique(oldInstances, InstancePO::getId);
        List<IndexEntryPO> oldAndConflictingItems =
                NncUtils.isEmpty(oldInstances) && NncUtils.isEmpty(currentEntries) ? List.of() :
                        indexEntryMapper.selectByInstanceIdsOrKeys(
                            context.getTenantId(),
                            NncUtils.map(oldInstances, InstancePO::getId),
                            NncUtils.map(currentEntries, IndexEntryPO::getKey)
                    );

        List<IndexEntryPO> oldEntries = NncUtils.filter(
                oldAndConflictingItems,
                e -> oldInstanceIds.contains(e.getInstanceId())
        );

        List<IndexEntryPO> conflictingEntries = NncUtils.exclude(
                oldAndConflictingItems,
                e -> oldInstanceIds.contains(e.getInstanceId())
        );

        Map<IndexKeyPO, List<Long>> newKeyMap = NncUtils.toMultiMap(
                currentEntries, IndexEntryPO::getKey, IndexEntryPO::getInstanceId
        );
        Map<IndexKeyPO, Long> conflictingKeyMap = NncUtils.toMap(conflictingEntries, IndexEntryPO::getKey, IndexEntryPO::getInstanceId);
        for (IndexEntryPO currentItem : currentEntries) {
            Index constraint =
                    context.getEntityContext().getEntity(Index.class, currentItem.getConstraintId());
            if(!constraint.isUnique() || constraint.containsNull(currentItem.getKey())) {
                continue;
            }
            if(newKeyMap.get(currentItem.getKey()).size() > 1) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getInstanceId()), constraint
                );
            }
            Long conflictingInstanceId = conflictingKeyMap.get(currentItem.getKey());
            if(conflictingInstanceId != null && !conflictingInstanceId.equals(currentItem.getInstanceId())) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getInstanceId()), constraint
                );
            }
        }
        change.setAttribute(OLD_INDEX_ITEMS, oldEntries);
        change.setAttribute(NEW_INDEX_ITEMS, currentEntries);
        return false;
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        List<IndexEntryPO> oldItems = change.getAttribute(OLD_INDEX_ITEMS);
        List<IndexEntryPO> currentItems = change.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if(NncUtils.isNotEmpty(changeList.inserts())) {
            indexEntryMapper.batchInsert(changeList.inserts());
        }
        if(NncUtils.isNotEmpty(changeList.deletes())) {
            indexEntryMapper.batchDelete(changeList.deletes());
        }
    }


}
