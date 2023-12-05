package tech.metavm.entity;

import tech.metavm.event.EventQueue;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
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
    private EventQueue eventQueue;
    private long appId;
    private boolean asyncLogProcessing;
    private DefContext defContext = ModelDefRegistry.getDefContext();
    private boolean childrenLazyLoading;
    private TypeResolver typeResolver = new DefaultTypeResolver();
    private Cache cache;
    private boolean readonly;

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

    public InstanceContextBuilder eventQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        return this;
    }

    public InstanceContextBuilder childrenLazyLoading(boolean childrenLazyLoading) {
        this.childrenLazyLoading = childrenLazyLoading;
        return this;
    }

    public InstanceContextBuilder readonly(boolean readonly) {
        this.readonly = readonly;
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

    public InstanceContextBuilder appId(long appId) {
        this.appId = appId;
        return this;
    }

    public InstanceContextBuilder cache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public IInstanceContext buildInstanceContext() {
        if(appId == 0)
            appId = ContextUtil.getAppId();
        return new InstanceContext(
                appId, instanceStore, idProvider, executor,
                asyncLogProcessing, plugins, defContext, parent, typeResolver,
                 childrenLazyLoading, cache, eventQueue, readonly
        );
    }

    public IEntityContext build() {
        //noinspection resource
        return buildInstanceContext().getEntityContext();
    }

}
