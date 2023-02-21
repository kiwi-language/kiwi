package tech.metavm.entity;

import tech.metavm.util.ContextUtil;

public abstract class InstanceContextFactoryAware {

    private final InstanceContextFactory instanceContextFactory;

    protected InstanceContextFactoryAware(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    protected IEntityContext newContext() {
        return newContext(ContextUtil.getTenantId());
    }

    protected IEntityContext newContext(long tenantId) {
        return newContext(tenantId, false);
    }

    protected IEntityContext newContext(long tenantId, boolean asyncProcessing) {
        return instanceContextFactory.newContext(tenantId, asyncProcessing).getEntityContext();
    }


}
