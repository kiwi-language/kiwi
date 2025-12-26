package org.metavm.entity;

import org.metavm.context.Autowired;
import org.metavm.context.Component;
import org.metavm.jdbc.TransactionStatus;
import org.metavm.object.instance.persistence.MapperRegistry;

import java.util.concurrent.*;

@Component
public class InstanceContextFactory {

    private EntityIdProvider idService;

    private final MapperRegistry mapperRegistry;

    private final Executor executor = new ThreadPoolExecutor(
            16, 16, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
    );

    public InstanceContextFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    private boolean isReadonlyTransaction() {
        return !TransactionStatus.isTransactionActive()
                || TransactionStatus.isTransactionReadonly();
    }

    public InstanceContextBuilder newBuilder(long appId) {
        return InstanceContextBuilder.newBuilder(appId, mapperRegistry, new DefaultIdInitializer(idService))
                .executor(executor)
                .readonly(isReadonlyTransaction());
    }

    @Autowired
    public InstanceContextFactory setIdService(EntityIdProvider idService) {
        this.idService = idService;
        return this;
    }

}
