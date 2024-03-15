package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.Constants;

public class EntityContextFactoryBean {

    protected final EntityContextFactory entityContextFactory;

    public EntityContextFactoryBean(EntityContextFactory entityContextFactory) {
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
