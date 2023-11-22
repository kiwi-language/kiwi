package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.Constants;
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

    private List<ContextPlugin> plugins = List.of();

    private ApplicationContext applicationContext;

    private boolean defaultAsyncProcessing = false;

    private Cache cache;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000)
    );

    public InstanceContextFactory(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public IInstanceContext newContext(long tenantId) {

        return newContext(tenantId, defaultAsyncProcessing, isReadonlyTransaction(), idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    @Override
    public IInstanceContext newContext(long tenantId, boolean asyncProcessLogs) {
        return newContext(tenantId, asyncProcessLogs, isReadonlyTransaction(), idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    private boolean isReadonlyTransaction() {
        return  !TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    public IEntityContext newEntityContext(long tenantId) {
        return newEntityContext(tenantId, defaultAsyncProcessing);
    }

    public IEntityContext newEntityContext(long tenantId, boolean asyncProcessing) {
        //noinspection resource
        return newContext(tenantId, asyncProcessing).getEntityContext();
    }

    public InstanceContextBuilder newBuilder() {
        return new InstanceContextBuilder(instanceStore, executor, STD_CONTEXT, idService)
                .plugins(plugins)
                .cache(cache);
    }

    public IInstanceContext newContext(long tenantId,
                                      boolean asyncProcessLogs,
                                      boolean readonly,
                                      EntityIdProvider idProvider,
                                      IInstanceContext parent,
                                      DefContext defContext) {
        return newBuilder()
                .tenantId(tenantId)
                .asyncLogProcessing(asyncProcessLogs)
                .idProvider(idProvider)
                .parent(parent)
                .readonly(readonly)
                .defContext(defContext)
                .buildInstanceContext();
    }

    private List<ContextPlugin> getPlugins() {
        return plugins != null ? plugins : List.of();
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

    @Autowired
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public IInstanceContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

    public IInstanceContext newRootContext() {
        return newContext(Constants.ROOT_TENANT_ID);
    }

    public static void setStdContext(IInstanceContext context) {
        STD_CONTEXT = context;
    }

    public static IInstanceContext getStdContext() {
        return STD_CONTEXT;
    }

    public InstanceContextFactory setDefaultAsyncProcessing(boolean defaultAsyncProcessing) {
        this.defaultAsyncProcessing = defaultAsyncProcessing;
        return this;
    }
}
