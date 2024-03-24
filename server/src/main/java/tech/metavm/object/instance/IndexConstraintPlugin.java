package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.object.instance.persistence.VersionRT;
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
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        Map<Id, ClassInstance> instanceMap = new HashMap<>();
        List<IndexEntryPO> currentEntries = new ArrayList<>();
        change.forEachInsertOrUpdate(ver -> {
            var instance = context.get(ver.id());
            if (instance instanceof ClassInstance classInstance) {
                instanceMap.put(classInstance.getId(), classInstance);
                currentEntries.addAll(PersistenceUtils.getIndexEntries(classInstance, context.getParameterizedFlowProvider(), context.getAppId()));
            }
        });
        var oldVersions = NncUtils.union(change.updates(), change.deletes());
        var oldIdSet = NncUtils.mapUnique(oldVersions, VersionRT::id);
        Set<IndexEntryPO> relatedEntries = new HashSet<>();
        NncUtils.doInBatch(NncUtils.map(oldVersions, v -> v.id().toBytes()),
                ids -> relatedEntries.addAll(indexEntryMapper.selectByInstanceIds(context.getAppId(), ids)));
        NncUtils.doInBatch(NncUtils.map(currentEntries, IndexEntryPO::getKey),
                keys -> relatedEntries.addAll(indexEntryMapper.selectByKeys(context.getAppId(), keys)));

        List<IndexEntryPO> oldEntries = NncUtils.filter(
                relatedEntries,
                e -> oldIdSet.contains(e.getId())
        );

        List<IndexEntryPO> conflictingEntries = NncUtils.exclude(
                relatedEntries,
                e -> oldIdSet.contains(e.getId())
        );

        Map<IndexKeyPO, List<Id>> newKeyMap = NncUtils.toMultiMap(
                currentEntries, IndexEntryPO::getKey, IndexEntryPO::getId
        );
        Map<IndexKeyPO, Id> conflictingKeyMap = NncUtils.toMap(conflictingEntries, IndexEntryPO::getKey, IndexEntryPO::getId);
        for (IndexEntryPO currentItem : currentEntries) {
            Index constraint = indexProvider.getIndex(Id.fromBytes(currentItem.getIndexId()));
            if (!constraint.isUnique() || PersistenceUtils.containsNull(constraint, currentItem.getKey())) {
                continue;
            }
            if (newKeyMap.get(currentItem.getKey()).size() > 1) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getId()), constraint
                );
            }
            var conflictingInstanceId = conflictingKeyMap.get(currentItem.getKey());
            if (conflictingInstanceId != null && !conflictingInstanceId.equals(currentItem.getId())) {
                throw BusinessException.constraintCheckFailed(
                        instanceMap.get(currentItem.getId()), constraint
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

    private List<IndexEntryPO> selectByInstanceIds(long appId, List<byte[]> instanceIds) {
        List<IndexEntryPO> result = new ArrayList<>();
        for (int i = 0; i < 20 && (i + 1) * BATCH_SIZE < instanceIds.size(); i++) {
            var batch = instanceIds.subList(i * BATCH_SIZE, (i + 1) * BATCH_SIZE);
            result.addAll(indexEntryMapper.selectByInstanceIds(appId, batch));
        }
        return result;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
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
