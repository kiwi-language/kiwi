package tech.metavm.object.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityChange;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

import static tech.metavm.entity.ContextAttributeKey.CHANGE_LOGS;

@Component
@Order(100)
public class ChangeLogPlugin implements ContextPlugin {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final InstanceLogService instanceLogService;

    public ChangeLogPlugin(InstanceLogService instanceLogService) {
        this.instanceLogService = instanceLogService;
    }

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
        if(DebugEnv.debugging)
            debugLogger.info("generating change logs");
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

    public static final Logger LOGGER = LoggerFactory.getLogger(ChangeLogPlugin.class);

    @Override
    public void postProcess(IInstanceContext context) {
        List<InstanceLog> logs = context.getAttribute(CHANGE_LOGS);
        if(NncUtils.isNotEmpty(logs)) {
        instanceLogService.process(logs, context.getClientId());
        }
    }

}
