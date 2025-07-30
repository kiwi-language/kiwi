package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.EntityChange;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.instance.search.SearchSync;
import org.metavm.task.ShadowTask;
import org.metavm.task.Task;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.metavm.entity.ContextAttributeKey.CHANGE_LOGS;

@Slf4j
public class ChangeLogPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLogPlugin.class);

    private final IInstanceStore instanceStore;
    private final InstanceLogService instanceLogService;

    public ChangeLogPlugin(IInstanceStore instanceStore, InstanceLogService instanceLogService) {
        this.instanceStore = instanceStore;
        this.instanceLogService = instanceLogService;
    }

    @Override
    public boolean beforeSaving(Patch patch, IInstanceContext context) {
        var entityChange = patch.entityChange();
        List<InstanceLog> logs = new ArrayList<>();
        for (var instance : entityChange.inserts()) {
            logs.add(InstanceLog.insert(instance));
        }
        for (var instance : entityChange.updates()) {
            logs.add(InstanceLog.update(instance));
        }
        for (var delete : entityChange.deletes()) {
            logs.add(InstanceLog.delete(delete));
        }
        context.getAttribute(CHANGE_LOGS).addAll(logs);
        return false;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        instanceStore.saveInstanceLogs(context.getAttribute(CHANGE_LOGS), context);
    }

    @Override
    public void postProcess(IInstanceContext context, Patch patch) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if (Utils.isNotEmpty(logs)) {
            instanceLogService.process(context.getAppId(), logs,
                    instanceStore, context.getClientId(), ModelDefRegistry.getDefContext());
        }
        createTasks(logs, context);
        createSearchSyncTask(patch, logs, context);
    }

    private void createSearchSyncTask(Patch patch, List<InstanceLog> logs, IInstanceContext context) {
        if (context.isMigrating()) {
            List<Id> ids = Utils.map(patch.trees(), t -> PhysicalId.of(t.id(), 0));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    SearchSync.sync(ids, List.of(), false, context);
                }
            });

        } else {
            var idsToIndex = new HashSet<>(Utils.filterAndMap(context.getSearchReindexSet(), i -> !i.isRemoved(), Instance::getId));
            var idsToRemove = new ArrayList<Id>();
            for (var log : logs) {
                var inst = context.internalGet(log.getId());
                if (inst instanceof ClassInstance clsInst && clsInst.isRoot() && clsInst.isSearchable()) {
                    if (log.isInsertOrUpdate())
                        idsToIndex.add(inst.getId());
                    else
                        idsToRemove.add(inst.getId());
                }
            }
            if(!idsToIndex.isEmpty() || !idsToRemove.isEmpty())
                instanceLogService.createSearchSyncTask(context.getAppId(), idsToIndex, idsToRemove, ModelDefRegistry.getDefContext(), context.isMigrating());
        }
    }

    private void createTasks(List<InstanceLog> logs, IInstanceContext context) {
        var tasks = new ArrayList<Task>();
        for (var log : logs) {
            var inst = context.internalGet(log.getId());
            if (log.isInsert()) {
                if (inst instanceof Task task) {
                    tasks.add(task);
                }
            }
        }
        if(!tasks.isEmpty())
            ShadowTask.saveShadowTasksHook.accept(context.getAppId(), tasks);
    }

}
