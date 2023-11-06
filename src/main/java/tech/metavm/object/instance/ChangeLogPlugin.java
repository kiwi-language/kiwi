package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static tech.metavm.entity.ContextAttributeKey.CHANGE_LOGS;

@Component
public class ChangeLogPlugin implements ContextPlugin {

    private final InstanceLogService instanceLogService;

    public ChangeLogPlugin(InstanceLogService instanceLogService) {
        this.instanceLogService = instanceLogService;
    }

    @Override
    public void beforeSaving(EntityChange<InstancePO> changes, IInstanceContext context) {
        List<InstanceLog> logs = new ArrayList<>();
        for (InstancePO instance : changes.inserts()) {
            logs.add(InstanceLog.insert(instance));
        }
        for (InstancePO instance : changes.updates()) {
            logs.add(InstanceLog.update(instance));
        }
        for (InstancePO delete : changes.deletes()) {
            logs.add(InstanceLog.delete(delete.nextVersion()));
        }
        context.getAttribute(CHANGE_LOGS).addAll(logs);
    }

    @Override
    public void afterSaving(EntityChange<InstancePO> changes, IInstanceContext context) {
        // TODO save change logs
    }

    @Override
    public void postProcess(IInstanceContext context) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if(NncUtils.isNotEmpty(logs)) {
            instanceLogService.process(logs);
        }
    }

}
