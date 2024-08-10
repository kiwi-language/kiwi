package org.metavm.entity;

import org.metavm.event.EventQueue;
import org.metavm.object.instance.BufferedInstanceStore;
import org.metavm.object.instance.CachingInstanceStore;
import org.metavm.object.instance.ContextPlugin;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ActiveCommitProvider;
import org.metavm.object.type.RedirectStatusProvider;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;

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
                                                    RedirectStatusProvider redirectStatusProvider,
                                                    ActiveCommitProvider activeCommitProvider) {
        return new InstanceContextBuilder(appId, instanceStore, idProvider,
                typeDefProvider, mappingProvider, redirectStatusProvider, activeCommitProvider);
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
    private final RedirectStatusProvider redirectStatusProvider;
    private final ActiveCommitProvider activeCommitProvider;
    private boolean childLazyLoading;
    private Cache cache;
    private EventQueue eventQueue;
    private boolean readonly;
    private @Nullable WAL readWAL;
    private @Nullable WAL writeWAL;
    private boolean skipPostprocessing;
    private boolean relocationEnabled;
    private long timeout;

    public InstanceContextBuilder(long appId,
                                  IInstanceStore instanceStore,
                                  IdInitializer idInitializer,
                                  TypeDefProvider typeDefProvider,
                                  MappingProvider mappingProvider,
                                  RedirectStatusProvider redirectStatusProvider,
                                  ActiveCommitProvider activeCommitProvider) {
        this.appId = appId;
        this.instanceStore = instanceStore;
        this.idInitializer = idInitializer;
        this.typeDefProvider = typeDefProvider;
        this.mappingProvider = mappingProvider;
        this.redirectStatusProvider = redirectStatusProvider;
        this.activeCommitProvider = activeCommitProvider;
    }

    public InstanceContextBuilder dependency(EntityInstanceContextBridge dependency) {
        this.typeDefProvider = dependency;
        this.mappingProvider = dependency;
        return this;
    }

    public InstanceContextBuilder plugins(ContextPlugin...plugins) {
        return plugins(List.of(plugins));
    }

    public InstanceContextBuilder plugins(Function<IInstanceStore, List<ContextPlugin>> pluginsSupplier) {
       return plugins(pluginsSupplier.apply(instanceStore));
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

    public InstanceContextBuilder readWAL(WAL wal) {
        readWAL = wal;
        if(wal != null)
            instanceStore = new CachingInstanceStore(instanceStore, wal);
        return this;
    }

    public InstanceContextBuilder writeWAL(WAL wal) {
        writeWAL = wal;
        if(wal != null) {
            instanceStore = new BufferedInstanceStore(instanceStore, wal);
            skipPostprocessing = true;
        }
        return this;
    }

    public InstanceContextBuilder relocationEnabled(boolean relocationEabled) {
        this.relocationEnabled = relocationEabled;
        return this;
    }

    public InstanceContextBuilder timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public InstanceContextBuilder typeDefProvider(TypeDefProvider typeDefProvider) {
        this.typeDefProvider = typeDefProvider;
        return this;
    }

    public IInstanceContext build() {
        if (executor == null)
            executor = Executors.newSingleThreadExecutor();
        var idInitializer = this.idInitializer;
        return new InstanceContext(
                appId, instanceStore, idInitializer, executor, asyncPostProcess,
                plugins, parent, typeDefProvider, mappingProvider, redirectStatusProvider,
                activeCommitProvider,
                childLazyLoading, cache,
                eventQueue, readonly, skipPostprocessing, relocationEnabled, timeout);
    }

}
