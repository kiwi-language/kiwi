package org.metavm.entity;

import lombok.Getter;
import lombok.Setter;
import org.metavm.jdbc.TransactionStatus;
import org.metavm.object.instance.ChangeLogPlugin;
import org.metavm.object.instance.CheckConstraintPlugin;
import org.metavm.object.instance.IndexConstraintPlugin;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.context.Autowired;
import org.metavm.context.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Component
public class EntityContextFactory {

    @Getter
    private final InstanceContextFactory instanceContextFactory;
    private InstanceLogService instanceLogService;
    @Setter
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

    public IInstanceContext newContext(long appId) {
        return newContext(appId, defContext);
    }

    public IInstanceContext newContext(long appId, Consumer<InstanceContextBuilder> customizer) {
        return newContext(appId, defContext, null, false, customizer);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent) {
        return newContext(appId, parent, (IdInitializer) null);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent, Consumer<InstanceContextBuilder> customizer) {
        return newContext(appId, parent, null, false, customizer);
    }

    public IInstanceContext newContext(long appId, @Nullable IInstanceContext parent, @Nullable IdInitializer idProvider) {
        return newContext(appId, parent, idProvider, false, builder -> {
        });
    }

    public IInstanceContext newContext(long appId,
                                       @Nullable IInstanceContext parent,
                                       @Nullable IdInitializer idProvider,
                                       boolean migrationDisabled, Consumer<InstanceContextBuilder> customizer) {
        return newBridgedInstanceContext(appId, isReadonlyTransaction(),
                parent, idProvider, migrationDisabled, customizer);
    }

    public IInstanceContext newBridgedInstanceContext(long appId,
                                                      boolean readonly,
                                                      @Nullable IInstanceContext parent,
                                                      @Nullable IdInitializer idProvider,
                                                      boolean migrationDisabled, Consumer<InstanceContextBuilder> customizer) {
        var builder = instanceContextFactory.newBuilder(appId)
                .readonly(readonly)
                .parent(parent)
                .relocationEnabled(migrationDisabled)
                .timeout(Constants.SESSION_TIMEOUT);
        if (idProvider != null)
            builder.idInitializer(idProvider);
        customizer.accept(builder);
        builder.plugins(
                currentStore -> List.of(
                        new CheckConstraintPlugin(),
                        new IndexConstraintPlugin(currentStore),
                        new ChangeLogPlugin(currentStore, instanceLogService)
                ));
        return builder.build();
    }

    private boolean isReadonlyTransaction() {
        return TransactionStatus.isTransactionActive()
                && TransactionStatus.isTransactionReadonly();
    }

    @Autowired
    public void setInstanceLogService(InstanceLogService instanceLogService) {
        this.instanceLogService = instanceLogService;
    }

}
