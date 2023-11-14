package tech.metavm.entity;

import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.util.ContextUtil;

import java.util.List;
import java.util.concurrent.Executor;

public class InstanceContextBuilder {

    private final IInstanceStore instanceStore;
    private final Executor executor;
    private IInstanceContext parent;
    private EntityIdProvider idProvider;
    private List<ContextPlugin> plugins = List.of();
    private long tenantId;
    private long profileLogThreshold = -1L;
    private boolean asyncLogProcessing;
    private DefContext defContext = ModelDefRegistry.getDefContext();
    private TypeResolver typeResolver = new DefaultTypeResolver();

    public InstanceContextBuilder(IInstanceStore instanceStore, Executor executor,
                                  IInstanceContext parent, EntityIdProvider idProvider) {
        this.instanceStore = instanceStore;
        this.executor = executor;
        this.parent = parent;
        this.idProvider = idProvider;
    }

    public InstanceContextBuilder plugins(List<ContextPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public InstanceContextBuilder idProvider(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
        return this;
    }

    public InstanceContextBuilder parent(IInstanceContext parent) {
        this.parent = parent;
        return this;
    }

    public InstanceContextBuilder profileLogThreshold(long profileLogThreshold) {
        this.profileLogThreshold = profileLogThreshold;
        return this;
    }

    public InstanceContextBuilder asyncLogProcessing(boolean asyncLogProcessing) {
        this.asyncLogProcessing = asyncLogProcessing;
        return this;
    }

    public InstanceContextBuilder defContext(DefContext defContext) {
        this.defContext = defContext;
        return this;
    }

    public InstanceContextBuilder typeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
        return this;
    }

    public InstanceContextBuilder tenantId(long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public IInstanceContext buildInstanceContext() {
        if(tenantId == 0)
            tenantId = ContextUtil.getTenantId();
        return new InstanceContext(
                tenantId, instanceStore, idProvider, executor,
                asyncLogProcessing, plugins, defContext, parent, typeResolver,
                profileLogThreshold
        );
    }

    public IEntityContext build() {
        //noinspection resource
        return buildInstanceContext().getEntityContext();
    }

}
