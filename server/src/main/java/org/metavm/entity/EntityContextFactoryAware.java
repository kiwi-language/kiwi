package org.metavm.entity;

import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;

import java.util.function.Consumer;

public class EntityContextFactoryAware {

    protected final EntityContextFactory entityContextFactory;

    public EntityContextFactoryAware(EntityContextFactory entityContextFactory) {
        this.entityContextFactory = entityContextFactory;
    }

    public IInstanceContext newContext() {
        return entityContextFactory.newContext();
    }

    public IInstanceContext newContext(long appId, Consumer<InstanceContextBuilder> customizer) {
        return entityContextFactory.newContext(appId, customizer);
    }

    public IInstanceContext newContext(long appId, IInstanceContext parent, Consumer<InstanceContextBuilder> customizer) {
        return entityContextFactory.newContext(appId, parent, customizer);
    }

    public IInstanceContext newContext(Consumer<InstanceContextBuilder> customizer) {
        return entityContextFactory.newContext(ContextUtil.getAppId(), customizer);
    }

    public IInstanceContext newContext(long appId) {
        return entityContextFactory.newContext(appId);
    }

    public IInstanceContext newPlatformContext() {
        return newContext(Constants.PLATFORM_APP_ID);
    }

    public IInstanceContext newContext(long appId, IdInitializer idProvider) {
        return entityContextFactory.newContext(appId, idProvider);
    }

}
