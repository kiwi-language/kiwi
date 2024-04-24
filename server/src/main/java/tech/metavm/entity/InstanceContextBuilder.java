package tech.metavm.entity;

import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.TypeDefProvider;
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
                                                    TypeDefProvider typeDefProvider,
                                                    MappingProvider mappingProvider,
                                                    ParameterizedFlowProvider parameterizedFlowProvider,
                                                    CompositeTypeFacade compositeTypeFacade) {
        return new InstanceContextBuilder(appId, instanceStore, idProvider,
                typeDefProvider, mappingProvider, parameterizedFlowProvider, compositeTypeFacade);
    }

    private final long appId;
    private IInstanceStore instanceStore;
    private IdInitializer idInitializer;
    private Executor executor;
    private @Nullable IInstanceContext parent;
    private boolean asyncPostProcess;
    private List<ContextPlugin> plugins = List.of();
    private TypeDefProvider typeDefProvider;
    private MappingProvider mappingProvider;
    private ParameterizedFlowProvider parameterizedFlowProvider;
    private CompositeTypeFacade compositeTypeFacade;
    private boolean childLazyLoading;
    private Cache cache;
    private EventQueue eventQueue;
    private boolean readonly;
    private Function<Id, TypeId> getTypeIdInterceptor;

    public InstanceContextBuilder(long appId,
                                  IInstanceStore instanceStore,
                                  IdInitializer idInitializer,
                                  TypeDefProvider typeDefProvider,
                                  MappingProvider mappingProvider,
                                  ParameterizedFlowProvider parameterizedFlowProvider,
                                  CompositeTypeFacade compositeTypeFacade) {
        this.appId = appId;
        this.instanceStore = instanceStore;
        this.idInitializer = idInitializer;
        this.typeDefProvider = typeDefProvider;
        this.mappingProvider = mappingProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.compositeTypeFacade = compositeTypeFacade;
    }

    public InstanceContextBuilder dependency(EntityInstanceContextBridge dependency) {
        this.typeDefProvider = dependency;
        this.mappingProvider = dependency;
        this.parameterizedFlowProvider = dependency;
        this.compositeTypeFacade = dependency;
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
                plugins, parent, typeDefProvider, mappingProvider,
                parameterizedFlowProvider, compositeTypeFacade, childLazyLoading, cache,
                eventQueue, readonly);
    }

}
