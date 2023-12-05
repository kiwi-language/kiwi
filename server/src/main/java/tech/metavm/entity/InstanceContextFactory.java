package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.event.EventQueue;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.IInstanceContext;
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

    private final EventQueue eventQueue;

    private List<ContextPlugin> plugins = List.of();

    private ApplicationContext applicationContext;

    private boolean defaultAsyncProcessing = true;

    private Cache cache;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000)
    );

    public InstanceContextFactory(IInstanceStore instanceStore, EventQueue eventQueue) {
        this.instanceStore = instanceStore;
        this.eventQueue = eventQueue;
    }

    @Override
    public IInstanceContext newContext(long appId) {
        return newContext(appId, defaultAsyncProcessing, isReadonlyTransaction(), idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    @Override
    public IInstanceContext newContext(long appId, boolean asyncProcessLogs) {
        return newContext(appId, asyncProcessLogs, isReadonlyTransaction(), idService, STD_CONTEXT, ModelDefRegistry.getDefContext());
    }

    private boolean isReadonlyTransaction() {
        return  !TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    public IEntityContext newEntityContext() {
        return newEntityContext(ContextUtil.getAppId(), defaultAsyncProcessing);
    }

    public IEntityContext newEntityContext(long appId) {
        return newEntityContext(appId, defaultAsyncProcessing);
    }

    public IEntityContext newEntityContext(long appId, boolean asyncProcessing) {
        //noinspection resource
        return newContext(appId, asyncProcessing).getEntityContext();
    }

    public InstanceContextBuilder newBuilder() {
        return new InstanceContextBuilder(instanceStore, executor, STD_CONTEXT, idService)
                .eventQueue(eventQueue)
                .plugins(plugins)
                .cache(cache);
    }

    public IInstanceContext newContext(long appId,
                                      boolean asyncProcessLogs,
                                      boolean readonly,
                                      EntityIdProvider idProvider,
                                      IInstanceContext parent,
                                      DefContext defContext) {
        return newBuilder()
                .appId(appId)
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
        return newContext(ContextUtil.getAppId());
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
