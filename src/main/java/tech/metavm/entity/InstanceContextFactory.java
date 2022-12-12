package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.util.ContextUtil;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class InstanceContextFactory implements IInstanceContextFactory {

    private static volatile IInstanceContext STD_CONTEXT;

    private EntityIdProvider idService;

    private final IInstanceStore instanceStore;

    private List<ContextPlugin> plugins;

    private ApplicationContext applicationContext;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000)
    );

    public InstanceContextFactory(IInstanceStore instanceStore/*, List<ContextPlugin> plugins*/) {
//        this.idService = idService;
        this.instanceStore = instanceStore;
//        this.plugins = plugins;
    }

    @Override
    public InstanceContext newContext(long tenantId) {
        return newContext(tenantId, true, idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    @Override
    public InstanceContext newContext(long tenantId, boolean asyncProcessLogs) {
        return newContext(tenantId, asyncProcessLogs, idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    public InstanceContext newContext(long tenantId,
                                      boolean asyncProcessLogs,
                                      EntityIdProvider idProvider,
                                      IInstanceContext parent,
                                      DefContext defContext) {
        return new InstanceContext(
                tenantId,
                instanceStore,
                idProvider,
                executor,
                asyncProcessLogs,
                getPlugins(),
                parent
        );
    }

    private List<ContextPlugin> getPlugins() {
        return plugins != null ? plugins : List.of();
//        return new ArrayList<>(
//                applicationContext.getBeansOfType(ContextPlugin.class).values()
//        );
    }

    @Autowired
    public InstanceContextFactory setIdService(EntityIdProvider idService) {
        this.idService = idService;
        return this;
    }

    @Autowired
    public InstanceContextFactory setPlugins(List<ContextPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public InstanceContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

    public static void setStdContext(IInstanceContext context) {
        STD_CONTEXT = context;
    }

    public static IInstanceContext getStdContext() {
        return STD_CONTEXT;
    }

}
