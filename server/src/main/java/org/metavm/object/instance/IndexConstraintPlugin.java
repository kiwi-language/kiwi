package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.PersistenceUtils;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.IndexProvider;
import org.metavm.util.BusinessException;
import org.metavm.util.ChangeList;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static org.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static org.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

public class IndexConstraintPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(IndexConstraintPlugin.class);

    public static final int BATCH_SIZE = 2000;

    private final IInstanceStore instanceStore;

    private final IndexProvider indexProvider;

    public IndexConstraintPlugin(IInstanceStore instanceStore, IndexProvider indexProvider) {
        this.instanceStore = instanceStore;
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
        var oldIdSet = new HashSet<Id>();
        var oldIds = new ArrayList<Id>();
        change.forEachUpdateOrDelete(v -> {
            oldIdSet.add(v.id());
            oldIds.add(v.id());
        });
        var oldEntries = new ArrayList<IndexEntryPO>();
        NncUtils.doInBatch(oldIds,
                ids -> oldEntries.addAll(instanceStore.getIndexEntriesByInstanceIds(ids, context)));
        NncUtils.doInBatch(new ArrayList<>(currentUniqueKeys),
                keys -> instanceStore.getIndexEntriesByKeys( keys, context).forEach(entry -> {
                    var id = entry.getId();
                    if (!oldIdSet.contains(id)) {
                        var currentEntry = NncUtils.findRequired(currentEntries, e -> e.getKey().equals(entry.getKey()));
                        var index = indexProvider.getIndex(Id.fromBytes(entry.getIndexId()));
                        throw BusinessException.constraintCheckFailed(instanceMap.get(currentEntry.getId()), index);
                    }
                }));
        change.setAttribute(OLD_INDEX_ITEMS, oldEntries);
        change.setAttribute(NEW_INDEX_ITEMS, currentEntries);
        return false;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        Collection<IndexEntryPO> oldItems = change.getAttribute(OLD_INDEX_ITEMS);
        Collection<IndexEntryPO> currentItems = change.getAttribute(NEW_INDEX_ITEMS);
        ChangeList<IndexEntryPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        instanceStore.saveIndexEntries(changeList);
//        if (NncUtils.isNotEmpty(changeList.inserts())) {
//            NncUtils.doInBatch(changeList.inserts(), instanceStore::batchInsert);
//        }
//        if (NncUtils.isNotEmpty(changeList.deletes())) {
//            NncUtils.doInBatch(changeList.deletes(), instanceStore::batchDelete);
//        }
    }


}
