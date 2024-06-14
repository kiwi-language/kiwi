package org.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;

import java.util.concurrent.*;

@Component
public class InstanceContextFactory {

    private EntityIdProvider idService;

    private final IInstanceStore instanceStore;

    private final EventQueue eventQueue;

    private Cache cache;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
    );

    public InstanceContextFactory(IInstanceStore instanceStore, EventQueue eventQueue) {
        this.instanceStore = instanceStore;
        this.eventQueue = eventQueue;
    }

    private boolean isReadonlyTransaction() {
        return !TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    public InstanceContextBuilder newBuilder(long appId,
                                             TypeDefProvider typeDefProvider,
                                             MappingProvider mappingProvider) {
        return InstanceContextBuilder.newBuilder(appId, instanceStore,
                        new DefaultIdInitializer(idService),
                        typeDefProvider, mappingProvider)
                .executor(executor)
                .eventQueue(eventQueue)
                .cache(cache)
                .asyncPostProcess(true)
                .readonly(isReadonlyTransaction());
    }

    @Autowired
    public InstanceContextFactory setIdService(EntityIdProvider idService) {
        this.idService = idService;
        return this;
    }

    @Autowired
    public void setCache(Cache cache) {
        this.cache = cache;
    }

}
