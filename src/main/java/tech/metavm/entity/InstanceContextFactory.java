package tech.metavm.entity;

import org.springframework.stereotype.Component;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class InstanceContextFactory {

    private static IInstanceContext STD_CONTEXT;

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
        return newContext(tenantId, true, idService, EntityTypeRegistry.getDefContext());
    }

    public InstanceContext newContext(long tenantId, boolean asyncProcessLogs) {
        return newContext(tenantId, asyncProcessLogs, idService, EntityTypeRegistry.getDefContext());
    }

    public InstanceContext newContext(long tenantId,
                                      boolean asyncProcessLogs,
                                      EntityIdProvider idProvider,
                                      DefContext defContext) {
        return new InstanceContext(
                tenantId,
                instanceStore,
                idProvider,
                executor,
                asyncProcessLogs,
                plugins,
                STD_CONTEXT,
                defContext
        );
    }

    public InstanceContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

    public static void initStdContext(IInstanceContext context) {
        NncUtils.requireNull(STD_CONTEXT, "standard context already initialized");
        STD_CONTEXT = context;
    }

}
