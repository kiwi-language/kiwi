package org.metavm.entity;

import org.metavm.object.instance.ChangeLogPlugin;
import org.metavm.object.instance.CheckConstraintPlugin;
import org.metavm.object.instance.IndexConstraintPlugin;
import org.metavm.object.instance.MetaVersionPlugin;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class EntityContextFactory {

    private final InstanceContextFactory instanceContextFactory;
    private final IndexEntryMapper indexEntryMapper;
    private InstanceLogService instanceLogService;
    private boolean defaultAsyncLogProcess = true;
    private DefContext defContext;

    public EntityContextFactory(InstanceContextFactory instanceContextFactory,
                                IndexEntryMapper indexEntryMapper) {
        this.instanceContextFactory = instanceContextFactory;
        this.indexEntryMapper = indexEntryMapper;
    }

    public IEntityContext newContext() {
        return newContext(ContextUtil.getAppId());
    }


    public IEntityContext newContext(boolean asyncLogProcess) {
        return newContext(ContextUtil.getAppId(), defContext, null, asyncLogProcess, null, null);
    }

    public IEntityContext newContext(long appId, IdInitializer idProvider) {
        return newContext(appId, defContext, idProvider);
    }

    public IEntityContext newLoadedContext(WAL cachingWAL) {
        return newLoadedContext(ContextUtil.getAppId(), cachingWAL);
    }

    public IEntityContext newLoadedContext(long appId, WAL cachingWAL) {
        return newContext(appId, defContext, null, defaultAsyncLogProcess, cachingWAL, null);
    }

    public IEntityContext newBufferingContext(WAL bufferingWAL) {
        return newBufferingContext(ContextUtil.getAppId(), bufferingWAL);
    }

    public IEntityContext newBufferingContext(long appId, WAL bufferingWAL) {
        return newContext(appId, defContext, null, defaultAsyncLogProcess, null, bufferingWAL);
    }

    public IEntityContext newContext(long appId) {
        return newContext(appId, defContext);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent) {
        return newContext(appId, parent, null);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable IdInitializer idProvider) {
        return newContext(appId, parent, idProvider, defaultAsyncLogProcess, null, null);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable IdInitializer idProvider,
                                     boolean asyncLogProcessing, @Nullable WAL cachingWAL, @Nullable WAL bufferingWAL) {
        var bridge = new EntityInstanceContextBridge();
        var instanceContext = newBridgedInstanceContext(appId, isReadonlyTransaction(), asyncLogProcessing,
                NncUtils.get(parent, IEntityContext::getInstanceContext), idProvider, bridge, cachingWAL, bufferingWAL);
        var context = new EntityContext(instanceContext, parent, defContext);
        bridge.setEntityContext(context);
        return context;
    }

    public IInstanceContext newBridgedInstanceContext(long appId,
                                                      boolean readonly,
                                                      @Nullable Boolean asyncLogProcessing,
                                                      @Nullable IInstanceContext parent,
                                                      @Nullable IdInitializer idProvider,
                                                      EntityInstanceContextBridge bridge, @Nullable WAL cachingWAL, @Nullable WAL bufferingWAL) {
        var builder = instanceContextFactory.newBuilder(appId, bridge, bridge)
                .readonly(readonly)
                .asyncPostProcess(NncUtils.orElse(asyncLogProcessing, defaultAsyncLogProcess))
                .parent(parent)
                .readWAL(cachingWAL)
                .writeWAL(bufferingWAL)
                .plugins(
                        store -> List.of(
                                new MetaVersionPlugin(bridge, bridge),
                                new CheckConstraintPlugin(),
                                new IndexConstraintPlugin(store, bridge),
                                new ChangeLogPlugin(store, instanceLogService)
                        ));
        if (idProvider != null)
            builder.idInitializer(idProvider);
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

    public void setDefaultAsyncLogProcess(boolean defaultAsyncLogProcess) {
        this.defaultAsyncLogProcess = defaultAsyncLogProcess;
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }
}
