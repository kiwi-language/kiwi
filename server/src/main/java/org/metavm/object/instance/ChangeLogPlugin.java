package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.task.ShadowTask;
import org.metavm.task.Task;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.metavm.entity.ContextAttributeKey.CHANGE_LOGS;

public class ChangeLogPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLogPlugin.class);

    private final IInstanceStore instanceStore;
    private final InstanceLogService instanceLogService;

    public ChangeLogPlugin(IInstanceStore instanceStore, InstanceLogService instanceLogService) {
        this.instanceStore = instanceStore;
        this.instanceLogService = instanceLogService;
    }

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        List<InstanceLog> logs = new ArrayList<>();
        for (var instance : change.inserts()) {
            logs.add(InstanceLog.insert(instance));
        }
        for (var instance : change.updates()) {
            logs.add(InstanceLog.update(instance));
        }
        for (var delete : change.deletes()) {
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
    public void postProcess(IInstanceContext context) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if (Utils.isNotEmpty(logs) || !context.getSearchReindexSet().isEmpty()) {
            instanceLogService.process(context.getAppId(), logs,
                    instanceStore, context.getClientId(), ModelDefRegistry.getDefContext());
            var tasks = new ArrayList<Task>();
            var idsToIndex = new HashSet<>(Utils.filterAndMap(context.getSearchReindexSet(), i -> !i.isRemoved(), Instance::getId));
            var idsToRemove = new ArrayList<Id>();
            for (var log : logs) {
                var inst = context.internalGet(log.getId());
                if(log.isInsert()) {
                    if (inst instanceof Task task) {
                        tasks.add(task);
                    }
                }
                if(inst instanceof ClassInstance clsInst && clsInst.isRoot() && clsInst.isSearchable()) {
                    if(log.isInsertOrUpdate())
                        idsToIndex.add(inst.getId());
                    else
                        idsToRemove.add(inst.getId());
                }
            }
            if(!tasks.isEmpty())
                ShadowTask.saveShadowTasksHook.accept(context.getAppId(), tasks);
            if(!idsToIndex.isEmpty() || !idsToRemove.isEmpty())
                instanceLogService.createSearchSyncTask(context.getAppId(), idsToIndex, idsToRemove, ModelDefRegistry.getDefContext());
        }
    }

}
