package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.util.ContextUtil;

import java.util.List;

@Component
public class EntityContextFactory {

    @Autowired
    private StoreRegistry storeRegistry;

    @Autowired
    private IdService idService;

    @Autowired
    private InstanceStore instanceStore;

    @Autowired
    private InstanceLogService instanceLogService;

    @Autowired
    private List<ContextPlugin> plugins;


    public EntityContext newContext(long tenantId) {
        return newContext(tenantId, true);
    }

    public EntityContext newContext(long tenantId, boolean asyncProcessLogs) {
        return new EntityContext(tenantId,
                storeRegistry,
                idService,
                instanceStore,
                instanceLogService,
                asyncProcessLogs,
                plugins);
    }

    public EntityContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

}
