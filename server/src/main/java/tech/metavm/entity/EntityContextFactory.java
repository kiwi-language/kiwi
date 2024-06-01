package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.CheckConstraintPlugin;
import tech.metavm.object.instance.IndexConstraintPlugin;
import tech.metavm.object.instance.MetaVersionPlugin;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

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
        return newContext(ContextUtil.getAppId(), defContext, null, asyncLogProcess);
    }


    public IEntityContext newContext(long appId, IdInitializer idProvider) {
        return newContext(appId, defContext, idProvider);
    }

    public IEntityContext newContext(long appId) {
        return newContext(appId, defContext);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent) {
        return newContext(appId, parent, null);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable IdInitializer idProvider) {
        return newContext(appId, parent, idProvider, defaultAsyncLogProcess);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable IdInitializer idProvider,
                                     boolean asyncLogProcessing) {
        var bridge = new EntityInstanceContextBridge();
        var instanceContext = newBridgedInstanceContext(appId, isReadonlyTransaction(), asyncLogProcessing,
                NncUtils.get(parent, IEntityContext::getInstanceContext), idProvider, bridge);
        var context = new EntityContext(instanceContext, parent, defContext);
        bridge.setEntityContext(context);
        return context;
    }

    public IInstanceContext newBridgedInstanceContext(long appId,
                                                      boolean readonly,
                                                      @Nullable Boolean asyncLogProcessing,
                                                      @Nullable IInstanceContext parent,
                                                      @Nullable IdInitializer idProvider,
                                                      EntityInstanceContextBridge bridge) {
        var builder = instanceContextFactory.newBuilder(appId, bridge, bridge)
                .readonly(readonly)
                .asyncPostProcess(NncUtils.orElse(asyncLogProcessing, defaultAsyncLogProcess))
                .parent(parent)
                .plugins(
                        new MetaVersionPlugin(bridge, bridge),
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(indexEntryMapper, bridge),
                        new ChangeLogPlugin(instanceLogService)
                );
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
