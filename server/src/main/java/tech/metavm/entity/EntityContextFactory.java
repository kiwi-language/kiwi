package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.CheckConstraintPlugin;
import tech.metavm.object.instance.IndexConstraintPlugin;
import tech.metavm.object.instance.MetaVersionPlugin;
import tech.metavm.object.instance.core.InstanceContextDependency;
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

    public EntityContextFactory(InstanceContextFactory instanceContextFactory,
                                IndexEntryMapper indexEntryMapper) {
        this.instanceContextFactory = instanceContextFactory;
        this.indexEntryMapper = indexEntryMapper;
    }

    public IEntityContext newContext() {
        return newContext(ContextUtil.getAppId());
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
        var dep = new InstanceContextDependency();
        var builder = instanceContextFactory.newBuilder(appId, dep, dep, dep)
                .readonly(isReadonlyTransaction())
                .plugins(
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(indexEntryMapper, dep),
                        new MetaVersionPlugin(dep, dep),
                        new ChangeLogPlugin(instanceLogService)
                );
        if(parent == null)
            parent = ModelDefRegistry.getDefContext();
        if(idProvider != null)
            builder.idProvider(idProvider);
        if(parent == null)
            builder.parent(NncUtils.get(parent, IEntityContext::getInstanceContext));
        var instanceContext = builder.build();
        var context = new EntityContext(instanceContext, parent);
        dep.setEntityContext(context);
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
}
