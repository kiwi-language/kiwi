package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.util.ContextUtil;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class InstanceContextFactory {

    private final IdService idService;

    private final InstanceStore instanceStore;

    private final List<ContextPlugin> plugins;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000)
    );

    public InstanceContextFactory(IdService idService, InstanceStore instanceStore, List<ContextPlugin> plugins) {
        this.idService = idService;
        this.instanceStore = instanceStore;
        this.plugins = plugins;
    }

    public InstanceContext newContext(long tenantId) {
        return newContext(tenantId, true);
    }

    public InstanceContext newContext(long tenantId, boolean asyncProcessLogs) {
        return new InstanceContext(
                tenantId,
                instanceStore,
                idService,
                executor,
                asyncProcessLogs,
                plugins
        );
    }

    public InstanceContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

}
