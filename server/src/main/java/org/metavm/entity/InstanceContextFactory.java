package org.metavm.entity;

import org.metavm.event.EventQueue;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.type.ActiveCommitProvider;
import org.metavm.object.type.RedirectStatusProvider;
import org.metavm.object.type.TypeDefProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    public InstanceContextBuilder newBuilder(long appId) {
        return InstanceContextBuilder.newBuilder(appId, instanceStore, new DefaultIdInitializer(idService))
                .executor(executor)
                .eventQueue(eventQueue)
                .cache(cache)
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
