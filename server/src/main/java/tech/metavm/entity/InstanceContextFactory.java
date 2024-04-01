package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;

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
                                             TypeProvider typeProvider,
                                             MappingProvider mappingProvider,
                                             ParameterizedFlowProvider parameterizedFlowProvider,
                                             CompositeTypeFacade compositeTypeFacade) {
        return InstanceContextBuilder.newBuilder(appId, instanceStore,
                        new DefaultIdInitializer(idService),
                        typeProvider, mappingProvider, parameterizedFlowProvider, compositeTypeFacade)
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
