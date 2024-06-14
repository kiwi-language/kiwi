package org.metavm.entity;

import org.metavm.util.Constants;

public class EntityContextFactoryAware {

    protected final EntityContextFactory entityContextFactory;

    public EntityContextFactoryAware(EntityContextFactory entityContextFactory) {
        this.entityContextFactory = entityContextFactory;
    }

    public IEntityContext newContext() {
        return entityContextFactory.newContext();
    }

    public IEntityContext newContext(long appId) {
        return entityContextFactory.newContext(appId);
    }

    public IEntityContext newPlatformContext() {
        return newContext(Constants.PLATFORM_APP_ID);
    }

    public IEntityContext newContext(long appId, IdInitializer idProvider) {
        return entityContextFactory.newContext(appId, idProvider);
    }

}
