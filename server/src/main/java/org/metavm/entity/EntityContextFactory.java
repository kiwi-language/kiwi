package org.metavm.entity;

import org.metavm.object.instance.ChangeLogPlugin;
import org.metavm.object.instance.CheckConstraintPlugin;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.IndexConstraintPlugin;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Component
public class EntityContextFactory {

    private final InstanceContextFactory instanceContextFactory;
    private InstanceLogService instanceLogService;
    private DefContext defContext;

    public EntityContextFactory(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public IInstanceContext newContext() {
        return newContext(ContextUtil.getAppId());
    }


    public IInstanceContext newContext(long appId, IdInitializer idProvider) {
        return newContext(appId, defContext, idProvider);
    }

    public IInstanceContext newLoadedContext(WAL cachingWAL) {
        return newLoadedContext(ContextUtil.getAppId(), cachingWAL);
    }

    public IInstanceContext newLoadedContext(long appId, WAL cachingWAL) {
        return newLoadedContext(appId, cachingWAL, false);
    }

    public IInstanceContext newLoadedContext(long appId, WAL cachingWAL, boolean migrationDisabled) {
        return newContext(appId, defContext, null, cachingWAL, null, migrationDisabled, null, builder -> {
        });
    }

    public IInstanceContext newBufferingContext(WAL bufferingWAL) {
        return newBufferingContext(ContextUtil.getAppId(), bufferingWAL);
    }

    public IInstanceContext newBufferingContext(long appId, WAL bufferingWAL) {
        return newContext(appId, defContext, null, null, bufferingWAL, false, null, builder -> {
        });
    }

    public IInstanceContext newContext(long appId) {
        return newContext(appId, defContext);
    }

    public IInstanceContext newContext(long appId, Consumer<InstanceContextBuilder> customizer) {
        return newContext(appId, defContext, null, null, null,
                false, null, customizer);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent) {
        return newContext(appId, parent, (IdInitializer) null);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent, Consumer<InstanceContextBuilder> customizer) {
        return newContext(appId, parent, null, null, null,
                false, null, customizer);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent, @Nullable IdInitializer idProvider) {
        return newContext(appId, parent, idProvider, null, null, false, null, builder -> {
        });
    }

    public IInstanceContext newContext(long appId,
                                     @Nullable IInstanceContext parent,
                                     @Nullable IdInitializer idProvider,
                                     @Nullable WAL cachingWAL,
                                     @Nullable WAL bufferingWAL,
                                     boolean migrationDisabled, @Nullable IInstanceStore store, Consumer<InstanceContextBuilder> customizer) {
        return newBridgedInstanceContext(appId, isReadonlyTransaction(),
                parent, idProvider, cachingWAL, bufferingWAL, store, migrationDisabled, customizer);
    }

    public IInstanceContext newContextWithStore(long appId, IInstanceStore instanceStore) {
        return newContext(appId, defContext, null, null, null, false, instanceStore, builder -> {
        });
    }

    public IInstanceContext newBridgedInstanceContext(long appId,
                                                      boolean readonly,
                                                      @Nullable IInstanceContext parent,
                                                      @Nullable IdInitializer idProvider,
                                                      @Nullable WAL cachingWAL,
                                                      @Nullable WAL bufferingWAL,
                                                      @Nullable IInstanceStore store, boolean migrationDisabled, Consumer<InstanceContextBuilder> customizer) {
        var builder = instanceContextFactory.newBuilder(appId)
                .readonly(readonly)
                .parent(parent)
                .relocationEnabled(migrationDisabled)
                .readWAL(cachingWAL)
                .writeWAL(bufferingWAL)
                .timeout(Constants.SESSION_TIMEOUT);
        if (store != null)
            builder.instanceStore(store);
        if (idProvider != null)
            builder.idInitializer(idProvider);
        customizer.accept(builder);
        builder.plugins(
                currentStore -> List.of(
//                        new MetaVersionPlugin(bridge, bridge),
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(currentStore),
                        new ChangeLogPlugin(currentStore, instanceLogService)
                ));
        return builder.build();
    }

    private boolean isReadonlyTransaction() {
        return !TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    @Autowired
    public void setInstanceLogService(InstanceLogService instanceLogService) {
        this.instanceLogService = instanceLogService;
    }

    public InstanceContextFactory getInstanceContextFactory() {
        return instanceContextFactory;
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }

}
