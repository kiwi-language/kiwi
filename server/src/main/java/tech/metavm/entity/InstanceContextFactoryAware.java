package tech.metavm.entity;

import tech.metavm.util.ContextUtil;

public abstract class InstanceContextFactoryAware {

    private final InstanceContextFactory instanceContextFactory;

    protected InstanceContextFactoryAware(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    protected IEntityContext newContext() {
        return newContext(ContextUtil.getAppId());
    }

    protected IEntityContext newContext(long appId) {
        return newContext(appId, false);
    }

    protected IEntityContext newContext(long appId, boolean asyncProcessing) {
        return instanceContextFactory.newContext(appId, asyncProcessing).getEntityContext();
    }


}
