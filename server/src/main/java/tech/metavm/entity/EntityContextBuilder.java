package tech.metavm.entity;

import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.concurrent.Executor;

public class EntityContextBuilder {

    public static EntityContextBuilder newBuilder(IInstanceStore instanceStore, Executor executor, EntityIdProvider idProvider) {
        return new EntityContextBuilder(instanceStore, executor, idProvider);
    }

    private final IInstanceStore instanceStore;
    private final Executor executor;
    private IEntityContext parent;
    private EntityIdProvider idProvider;
    private List<ContextPlugin> plugins = List.of();
    private EventQueue eventQueue;
    private long appId;
    private boolean asyncLogProcessing;
    private DefContext defContext;
    private boolean childrenLazyLoading;
    private TypeProvider typeProvider;
    private MappingProvider mappingProvider;
    private ParameterizedFlowProvider parameterizedFlowProvider;
    private Cache cache;
    private boolean readonly;

    private EntityContextBuilder(IInstanceStore instanceStore, Executor executor,
                                 EntityIdProvider idProvider) {
        this.instanceStore = instanceStore;
        this.executor = executor;
        this.idProvider = idProvider;
    }

    public EntityContextBuilder plugins(List<ContextPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public EntityContextBuilder idProvider(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
        return this;
    }

    public EntityContextBuilder parent(IEntityContext parent) {
        this.parent = parent;
        return this;
    }

    public EntityContextBuilder eventQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        return this;
    }

    public EntityContextBuilder childrenLazyLoading(boolean childrenLazyLoading) {
        this.childrenLazyLoading = childrenLazyLoading;
        return this;
    }

    public EntityContextBuilder readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public EntityContextBuilder asyncLogProcessing(boolean asyncLogProcessing) {
        this.asyncLogProcessing = asyncLogProcessing;
        return this;
    }

    public EntityContextBuilder defContext(DefContext defContext) {
        this.defContext = defContext;
        return this;
    }

    public EntityContextBuilder typeProvider(TypeProvider typeProvider) {
        this.typeProvider = typeProvider;
        return this;
    }

    public EntityContextBuilder appId(long appId) {
        this.appId = appId;
        return this;
    }

    public EntityContextBuilder cache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public IInstanceContext buildInstanceContext() {
        //noinspection resource
        return build().getInstanceContext();
    }

    public IEntityContext build() {
        if (appId == 0)
            appId = ContextUtil.getAppId();
        if (defContext == null)
            defContext = ModelDefRegistry.getDefContext();
        var dep = new EntityInstanceContextBridge();
        if(typeProvider == null)
            typeProvider = dep;
        if(mappingProvider == null)
            mappingProvider = dep;
        if(parameterizedFlowProvider == null)
            parameterizedFlowProvider = dep;
        var instanceContext = new InstanceContext(
                appId, instanceStore, idProvider, executor,
                asyncLogProcessing, plugins, NncUtils.get(parent, IEntityContext::getInstanceContext),
                typeProvider , mappingProvider,
                parameterizedFlowProvider, childrenLazyLoading, cache, eventQueue,
                readonly);
        return new EntityContext(instanceContext, parent, defContext);
    }

}
