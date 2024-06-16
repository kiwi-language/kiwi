package org.metavm.object.instance;

import org.metavm.object.instance.log.InstanceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.metavm.entity.EntityChange;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.metavm.entity.ContextAttributeKey.CHANGE_LOGS;

@Component
@Order(100)
public class ChangeLogPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLogPlugin.class);

    private final InstanceLogService instanceLogService;

    public ChangeLogPlugin(InstanceLogService instanceLogService) {
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
        // TODO save change logs
    }


    @Override
    public void postProcess(IInstanceContext context) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if (NncUtils.isNotEmpty(logs)) {
            instanceLogService.process(logs, context.getClientId());
        }
    }

}
