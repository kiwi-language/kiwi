package org.metavm.object.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.PersistenceUtils;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.type.IndexProvider;
import org.metavm.util.BusinessException;
import org.metavm.util.ChangeList;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static org.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static org.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

public class IndexConstraintPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(IndexConstraintPlugin.class);

    public static final int BATCH_SIZE = 2000;

    private final IndexEntryMapper indexEntryMapper;

    private final IndexProvider indexProvider;

    public IndexConstraintPlugin(IndexEntryMapper indexEntryMapper, IndexProvider indexProvider) {
        this.indexEntryMapper = indexEntryMapper;
        this.indexProvider = indexProvider;
    }

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        var instanceMap = new HashMap<Id, ClassInstance>();
        var currentEntries = new ArrayList<IndexEntryPO>();
        var currentUniqueKeys = new HashSet<IndexKeyPO>();
        change.forEachInsertOrUpdate(ver -> {
            var instance = context.get(ver.id());
            if (instance instanceof ClassInstance classInstance) {
                instanceMap.put(classInstance.getId(), classInstance);
                PersistenceUtils.forEachIndexEntries(classInstance, context.getAppId(),
                        currentEntries::add,
                        entry -> {
                            if (!currentUniqueKeys.add(entry.getKey()))
                                throw BusinessException.constraintCheckFailed(
                                        instanceMap.get(entry.getId()), indexProvider.getIndex(Id.fromBytes(entry.getIndexId()))
                                );
                        });
            }
        });
        var oldIds = new HashSet<Id>();
        var oldIdBytes = new ArrayList<byte[]>();
        change.forEachUpdateOrDelete(v -> {
            oldIds.add(v.id());
            oldIdBytes.add(v.id().toBytes());
        });
        var oldEntries = new ArrayList<IndexEntryPO>();
        NncUtils.doInBatch(oldIdBytes,
                ids -> oldEntries.addAll(indexEntryMapper.selectByInstanceIds(context.getAppId(), ids)));
        NncUtils.doInBatch(new ArrayList<>(currentUniqueKeys),
                keys -> indexEntryMapper.selectByKeys(context.getAppId(), keys).forEach(entry -> {
                    var id = entry.getId();
                    if (!oldIds.contains(id)) {
                        var currentEntry = NncUtils.findRequired(currentEntries, e -> e.getKey().equals(entry.getKey()));
                        var index = indexProvider.getIndex(Id.fromBytes(entry.getIndexId()));
                        throw BusinessException.constraintCheckFailed(instanceMap.get(currentEntry.getId()), index);
                    }
                }));
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
        Collection<IndexEntryPO> oldItems = change.getAttribute(OLD_INDEX_ITEMS);
        Collection<IndexEntryPO> currentItems = change.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if (NncUtils.isNotEmpty(changeList.inserts())) {
            NncUtils.doInBatch(changeList.inserts(), indexEntryMapper::batchInsert);
        }
        if (NncUtils.isNotEmpty(changeList.deletes())) {
            NncUtils.doInBatch(changeList.deletes(), indexEntryMapper::batchDelete);
        }
    }


}
