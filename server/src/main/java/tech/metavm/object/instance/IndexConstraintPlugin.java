package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexProvider;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static tech.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static tech.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

public class IndexConstraintPlugin implements ContextPlugin {

    public static final int BATCH_SIZE = 2000;

    private final IndexEntryMapper indexEntryMapper;

    private final IndexProvider indexProvider;

    public IndexConstraintPlugin(IndexEntryMapper indexEntryMapper, IndexProvider indexProvider) {
        this.indexEntryMapper = indexEntryMapper;
        this.indexProvider = indexProvider;
    }

    @Override
    public boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        Map<Long, ClassInstance> instanceMap = new HashMap<>();
        List<IndexEntryPO> currentEntries = new ArrayList<>();
        change.forEachInsertOrUpdate(instancePO -> {
            var instance = context.get(instancePO.getInstanceId());
            if (instance instanceof ClassInstance classInstance) {
                instanceMap.put(classInstance.getPhysicalId(), classInstance);
                currentEntries.addAll(PersistenceUtils.getIndexEntries(classInstance, context.getParameterizedFlowProvider(), context.getAppId().getPhysicalId()));
            }
        });
        List<InstancePO> oldInstances = NncUtils.union(change.updates(), change.deletes());
        Set<Long> oldInstanceIds = NncUtils.mapUnique(oldInstances, InstancePO::getId);
        Set<IndexEntryPO> relatedEntries = new HashSet<>();
        NncUtils.doInBatch(NncUtils.map(oldInstances, InstancePO::getId),
                ids -> relatedEntries.addAll(indexEntryMapper.selectByInstanceIds(context.getAppId().getPhysicalId(), ids)));
        NncUtils.doInBatch(NncUtils.map(currentEntries, IndexEntryPO::getKey),
                keys -> relatedEntries.addAll(indexEntryMapper.selectByKeys(context.getAppId().getPhysicalId(), keys)));

        List<IndexEntryPO> oldEntries = NncUtils.filter(
                relatedEntries,
                e -> oldInstanceIds.contains(e.getInstanceId())
        );

        List<IndexEntryPO> conflictingEntries = NncUtils.exclude(
                relatedEntries,
                e -> oldInstanceIds.contains(e.getInstanceId())
        );

        Map<IndexKeyPO, List<Long>> newKeyMap = NncUtils.toMultiMap(
                currentEntries, IndexEntryPO::getKey, IndexEntryPO::getInstanceId
        );
        Map<IndexKeyPO, Long> conflictingKeyMap = NncUtils.toMap(conflictingEntries, IndexEntryPO::getKey, IndexEntryPO::getInstanceId);
        for (IndexEntryPO currentItem : currentEntries) {
            Index constraint = indexProvider.getIndex(PhysicalId.of(currentItem.getIndexId(), ModelDefRegistry.getType(Index.class)));
            if (!constraint.isUnique() || PersistenceUtils.containsNull(constraint, currentItem.getKey())) {
                continue;
            }
            if (newKeyMap.get(currentItem.getKey()).size() > 1) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getInstanceId()), constraint
                );
            }
            Long conflictingInstanceId = conflictingKeyMap.get(currentItem.getKey());
            if (conflictingInstanceId != null && !conflictingInstanceId.equals(currentItem.getInstanceId())) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getInstanceId()), constraint
                );
            }
        }
        change.setAttribute(OLD_INDEX_ITEMS, oldEntries);
        change.setAttribute(NEW_INDEX_ITEMS, currentEntries);
        return false;
    }

    private List<IndexEntryPO> selectByKeys(long appId, List<IndexKeyPO> keys) {
        List<IndexEntryPO> result = new ArrayList<>();
        for (int i = 0; i < 20 && (i + 1) * BATCH_SIZE < keys.size(); i++) {
            var batch = keys.subList(i * BATCH_SIZE, (i + 1) * BATCH_SIZE);
            result.addAll(indexEntryMapper.selectByKeys(appId, batch));
        }
        return result;
    }

    private List<IndexEntryPO> selectByInstanceIds(long appId, List<Long> instanceIds) {
        List<IndexEntryPO> result = new ArrayList<>();
        for (int i = 0; i < 20 && (i + 1) * BATCH_SIZE < instanceIds.size(); i++) {
            var batch = instanceIds.subList(i * BATCH_SIZE, (i + 1) * BATCH_SIZE);
            result.addAll(indexEntryMapper.selectByInstanceIds(appId, batch));
        }
        return result;
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> change, IInstanceContext context) {
        List<IndexEntryPO> oldItems = change.getAttribute(OLD_INDEX_ITEMS);
        List<IndexEntryPO> currentItems = change.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if (NncUtils.isNotEmpty(changeList.inserts())) {
            NncUtils.doInBatch(changeList.inserts(), indexEntryMapper::batchInsert);
        }
        if (NncUtils.isNotEmpty(changeList.deletes())) {
            NncUtils.doInBatch(changeList.deletes(), indexEntryMapper::batchDelete);
        }
    }


}
