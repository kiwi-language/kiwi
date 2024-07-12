package org.metavm.object.instance;

import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        instanceStore.saveInstanceLogs(context.getAttribute(CHANGE_LOGS));
    }

    @Override
    public void postProcess(IInstanceContext context) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if (NncUtils.isNotEmpty(logs) || !context.getMigrated().isEmpty()) {
            if(NncUtils.isNotEmpty(logs))
                instanceStore.saveInstanceLogs(logs);
            instanceLogService.process(context.getAppId(), logs,
                    instanceStore, NncUtils.map(context.getMigrated(), DurableInstance::getId), context.getClientId());
        }
    }

}
