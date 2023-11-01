package tech.metavm.entity;

import tech.metavm.util.Constants;

import java.util.function.Function;

public class MockInstanceContextFactory implements IInstanceContextFactory{

    private final Function<Long, IInstanceContext> contextSupplier;

    public MockInstanceContextFactory(Function<Long, IInstanceContext> contextSupplier) {
        this.contextSupplier = contextSupplier;
    }

    @Override
    public IInstanceContext newContext(long tenantId) {
        return newContext(tenantId, true);
    }

    @Override
    public IInstanceContext newContext(long tenantId, boolean asyncProcessLogs) {
        return contextSupplier.apply(tenantId);
    }

    @Override
    public IInstanceContext newRootContext() {
        return contextSupplier.apply(Constants.ROOT_TENANT_ID);
    }

    @Override
    public IEntityContext newEntityContext(long tenantId, boolean asyncProcessing) {
        //noinspection resource
        return newContext(tenantId, asyncProcessing).getEntityContext();
    }
}
