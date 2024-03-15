package tech.metavm.entity;

import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class InstanceContextBuilder {

    public static InstanceContextBuilder newBuilder(long appId,
                                                    IInstanceStore instanceStore,
                                                    IdInitializer idProvider,
                                                    TypeProvider typeProvider,
                                                    MappingProvider mappingProvider,
                                                    ParameterizedFlowProvider parameterizedFlowProvider) {
        return new InstanceContextBuilder(appId, instanceStore, idProvider,
                typeProvider, mappingProvider, parameterizedFlowProvider);
    }

    private final long appId;
    private IInstanceStore instanceStore;
    private IdInitializer idInitializer;
    private Executor executor;
    private @Nullable IInstanceContext parent;
    private boolean asyncPostProcess;
    private List<ContextPlugin> plugins = List.of();
    private TypeProvider typeProvider;
    private MappingProvider mappingProvider;
    private ParameterizedFlowProvider parameterizedFlowProvider;
    private boolean childLazyLoading;
    private Cache cache;
    private EventQueue eventQueue;
    private boolean readonly;
    private Function<Id, TypeId> getTypeIdInterceptor;

    public InstanceContextBuilder(long appId,
                                  IInstanceStore instanceStore,
                                  IdInitializer idInitializer,
                                  TypeProvider typeProvider,
                                  MappingProvider mappingProvider,
                                  ParameterizedFlowProvider parameterizedFlowProvider) {
        this.appId = appId;
        this.instanceStore = instanceStore;
        this.idInitializer = idInitializer;
        this.typeProvider = typeProvider;
        this.mappingProvider = mappingProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
    }

    public InstanceContextBuilder dependency(EntityInstanceContextBridge dependency) {
        this.typeProvider = dependency;
        this.mappingProvider = dependency;
        this.parameterizedFlowProvider = dependency;
        return this;
    }

    public InstanceContextBuilder plugins(ContextPlugin...plugins) {
        return plugins(List.of(plugins));
    }

    public InstanceContextBuilder plugins(List<ContextPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public InstanceContextBuilder executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public InstanceContextBuilder instanceStore(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
        return this;
    }

    public InstanceContextBuilder idInitializer(IdInitializer idProvider) {
        this.idInitializer = idProvider;
        return this;
    }

    public InstanceContextBuilder parent(IInstanceContext parent) {
        this.parent = parent;
        return this;
    }

    public InstanceContextBuilder asyncPostProcess(boolean asyncPostProcess) {
        this.asyncPostProcess = asyncPostProcess;
        return this;
    }

    public InstanceContextBuilder readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public InstanceContextBuilder childLazyLoading(boolean childLazyLoading) {
        this.childLazyLoading = childLazyLoading;
        return this;
    }

    public InstanceContextBuilder eventQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        return this;
    }

    public InstanceContextBuilder cache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public InstanceContextBuilder getTypeIdInterceptor(Function<Id, TypeId> getTypeIdInterceptor) {
        this.getTypeIdInterceptor = getTypeIdInterceptor;
        return this;
    }

    public IInstanceContext build() {
        if (executor == null)
            executor = Executors.newSingleThreadExecutor();
        var idInitializer = this.idInitializer;
        if(getTypeIdInterceptor != null)
            idInitializer = new WrappedIdInitializer(getTypeIdInterceptor, idInitializer);
        return new InstanceContext(
                appId, instanceStore, idInitializer, executor, asyncPostProcess,
                plugins, parent, typeProvider, mappingProvider,
                parameterizedFlowProvider, childLazyLoading, cache, eventQueue,
                readonly);
    }

}
