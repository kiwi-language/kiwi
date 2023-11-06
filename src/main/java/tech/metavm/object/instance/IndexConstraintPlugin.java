package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
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
import java.util.Set;
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
                        instancePO -> context.get(instancePO.getIdRequired()),
                        ClassInstance.class
                );

        Map<Long, ClassInstance> instanceMap = NncUtils.toMap(currentInstances, Instance::getId);
        List<IndexEntryPO> currentEntries = NncUtils.flatMap(
                currentInstances,
                instance -> instance.getIndexEntries(context.getEntityContext())
        );
        List<InstancePO> oldInstances = NncUtils.union(diff.updates(), diff.deletes());
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

        List<IndexEntryPO> conflictingEntries = NncUtils.filterNot(
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
        diff.setAttribute(OLD_INDEX_ITEMS, oldEntries);
        diff.setAttribute(NEW_INDEX_ITEMS, currentEntries);
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> diff, IInstanceContext context) {
        List<IndexEntryPO> oldItems = diff.getAttribute(OLD_INDEX_ITEMS);
        List<IndexEntryPO> currentItems = diff.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if(NncUtils.isNotEmpty(changeList.inserts())) {
            indexEntryMapper.batchInsert(changeList.inserts());
        }
        if(NncUtils.isNotEmpty(changeList.deletes())) {
            indexEntryMapper.batchDelete(changeList.deletes());
        }
    }


}
