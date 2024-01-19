package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.application.Application;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.CheckConstraintPlugin;
import tech.metavm.object.instance.IndexConstraintPlugin;
import tech.metavm.object.instance.MetaVersionPlugin;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.type.IdConstants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@Component
public class EntityContextFactory {

    private final InstanceContextFactory instanceContextFactory;
    private final IndexEntryMapper indexEntryMapper;
    private InstanceLogService instanceLogService;
    private boolean defaultAsyncLogProcess = true;

    public EntityContextFactory(InstanceContextFactory instanceContextFactory,
                                IndexEntryMapper indexEntryMapper) {
        this.instanceContextFactory = instanceContextFactory;
        this.indexEntryMapper = indexEntryMapper;
    }

    public IEntityContext newContext() {
        return newContext(ContextUtil.getAppId());
    }


    public IEntityContext newContext(boolean asyncLogProcess) {
        return newContext(ContextUtil.getAppId(), ModelDefRegistry.getDefContext(), null, asyncLogProcess);
    }


    public IEntityContext newContext(long appId, EntityIdProvider idProvider) {
        return newContext(appId, ModelDefRegistry.getDefContext(), idProvider);
    }

    public IEntityContext newContext(long appId) {
        return newContext(appId, ModelDefRegistry.getDefContext());
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent) {
        return newContext(appId, parent, null);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable EntityIdProvider idProvider) {
        return newContext(appId, parent, idProvider, defaultAsyncLogProcess);
    }

    public IEntityContext newContext(long appId, @Nullable IEntityContext parent, @Nullable EntityIdProvider idProvider,
                                     boolean asyncLogProcessing) {
        var bridge = new EntityInstanceContextBridge();
        var builder = instanceContextFactory.newBuilder(appId, bridge, bridge, bridge)
                .readonly(isReadonlyTransaction())
                .asyncPostProcess(asyncLogProcessing)
                .parent(NncUtils.get(parent, IEntityContext::getInstanceContext))
                .getTypeIdInterceptor(id -> IdConstants.isBuiltinAppId(id) ? bridge.getType(Application.class).tryGetId() : null)
                .plugins(
                        new MetaVersionPlugin(bridge, bridge),
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(indexEntryMapper, bridge),
                        new ChangeLogPlugin(instanceLogService)
                );
        if (idProvider != null)
            builder.idProvider(idProvider);
        var instanceContext = builder.build();
        var context = new EntityContext(instanceContext, parent);
        bridge.setEntityContext(context);
        return context;
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
}
