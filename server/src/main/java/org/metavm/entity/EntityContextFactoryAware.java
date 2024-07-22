package org.metavm.entity;

import org.metavm.object.instance.IInstanceStore;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;

import java.util.function.Consumer;

public class EntityContextFactoryAware {

    protected final EntityContextFactory entityContextFactory;

    public EntityContextFactoryAware(EntityContextFactory entityContextFactory) {
        this.entityContextFactory = entityContextFactory;
    }

    public IEntityContext newContext() {
        return entityContextFactory.newContext();
    }

    public IEntityContext newContext(long appId, Consumer<InstanceContextBuilder> customizer) {
        return entityContextFactory.newContext(appId, customizer);
    }

    public IEntityContext newContext(Consumer<InstanceContextBuilder> customizer) {
        return entityContextFactory.newContext(ContextUtil.getAppId(), customizer);
    }

    public IEntityContext newContext(long appId) {
        return entityContextFactory.newContext(appId);
    }

    public IEntityContext newContextWithStore(long appId, IInstanceStore instanceStore) {
        return entityContextFactory.newContextWithStore(appId, instanceStore);
    }

    public IEntityContext newContextWithStore(IInstanceStore instanceStore) {
        return entityContextFactory.newContextWithStore(ContextUtil.getAppId(), instanceStore);
    }

    public IEntityContext newPlatformContext() {
        return newContext(Constants.PLATFORM_APP_ID);
    }

    public IEntityContext newContext(long appId, IdInitializer idProvider) {
        return entityContextFactory.newContext(appId, idProvider);
    }

}
