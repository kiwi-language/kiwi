package org.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.EntityChange;
import org.metavm.entity.Tree;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.PersistenceUtils;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.Index;
import org.metavm.util.BusinessException;
import org.metavm.util.ChangeList;
import org.metavm.util.DebugEnv;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static org.metavm.entity.DifferenceAttributeKey.NEW_INDEX_ITEMS;
import static org.metavm.entity.DifferenceAttributeKey.OLD_INDEX_ITEMS;

public class IndexConstraintPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(IndexConstraintPlugin.class);

    public static final int BATCH_SIZE = 2000;

    private final IInstanceStore instanceStore;


    public IndexConstraintPlugin(@NotNull IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public boolean beforeSaving(Patch patch, IInstanceContext context) {
        if (DebugEnv.traceIndexBuild) {
            logger.trace("Building index. entity change: {}", patch);
        }
        var instanceMap = new HashMap<Id, Instance>();
        var currentEntries = new ArrayList<IndexEntryPO>();
        var currentUniqueKeys = new HashSet<IndexKeyPO>();
        var objectsToIndex = new HashSet<Instance>(Utils.exclude(context.getReindexSet(), Instance::isRemoved));
        var involvedIds = new HashSet<Id>();
        if (context.isMigrating()) {
            for (Tree tree : patch.trees()) {
                var instance = context.internalGet(PhysicalId.of(tree.id(), 0));
                instance.accept(new InstanceVisitor<Void>() {

                    @Override
                    public Void visitClassInstance(ClassInstance instance) {
                        objectsToIndex.add(instance);
                        involvedIds.add(instance.getId());
                        instance.forEachChild(c -> c.accept(this));
                        return null;
                    }
                });
            }
        }
        else {
            patch.entityChange().forEachInsertOrUpdate(ver -> {
                var instance = context.get(ver.id());
                if (instance instanceof ClassInstance) {
                    objectsToIndex.add(instance);
                }
            });
        }
        objectsToIndex.forEach(instance -> {
            instanceMap.put(instance.getId(), instance);
            PersistenceUtils.forEachIndexEntries(instance, context.getAppId(),
                    currentEntries::add,
                    entry -> {
                        if (!currentUniqueKeys.add(entry.getKey()))
                            throw new BusinessException(
                                    ErrorCode.DUPLICATE_KEY2,
                                    context.getEntity(Index.class, Id.fromBytes(entry.getIndexId())).getName(),
                                    Utils.join(entry.getKey().getColumnValues(context::internalGet), Value::getText)
                            );
                    });
        });
        var oldIds = new ArrayList<Id>();
        patch.entityChange().forEachUpdateOrDelete(v -> {
            involvedIds.add(v.id());
            oldIds.add(v.id());
        });
        for (ClassInstance inst : context.getReindexSet()) {
            involvedIds.add(inst.getId());
            oldIds.add(inst.getId());
        }
        var oldEntries = new ArrayList<IndexEntryPO>();
        Utils.doInBatch(oldIds,
                ids -> oldEntries.addAll(instanceStore.getIndexEntriesByInstanceIds(ids, context)));
        Utils.doInBatch(new ArrayList<>(currentUniqueKeys),
                keys -> instanceStore.getIndexEntriesByKeys(keys, context).forEach(entry -> {
                    var id = entry.getId();
                    if (!involvedIds.contains(id)) {
                        var currentEntry = Utils.findRequired(currentEntries, e -> e.getKey().equals(entry.getKey()));
                        var index = context.getEntity(Index.class, Id.fromBytes(entry.getIndexId()));
                        throw BusinessException.constraintCheckFailed(instanceMap.get(currentEntry.getId()), index);
                    }
                }));
        patch.entityChange().setAttribute(NEW_INDEX_ITEMS, currentEntries);
        patch.entityChange().setAttribute(OLD_INDEX_ITEMS, oldEntries);
        return false;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        var oldItems = change.getAttribute(OLD_INDEX_ITEMS);
        var currentItems = change.getAttribute(NEW_INDEX_ITEMS);
        var changeList = context.isMigrating() ?
                ChangeList.inserts(currentItems) :
                ChangeList.build(oldItems, currentItems, Function.identity());
        instanceStore.saveIndexEntries(context.getAppId(), changeList);
//        if (NncUtils.isNotEmpty(changeList.inserts())) {
//            NncUtils.doInBatch(changeList.inserts(), instanceStore::batchInsert);
//        }
//        if (NncUtils.isNotEmpty(changeList.deletes())) {
//            NncUtils.doInBatch(changeList.deletes(), instanceStore::batchDelete);
//        }
    }


}
