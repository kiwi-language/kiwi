package org.metavm.entity;

import org.metavm.object.instance.*;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.instance.persistence.MapperRegistry;
import org.metavm.object.type.ActiveCommitProvider;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class InstanceContextBuilder {


    public static InstanceContextBuilder newBuilder(long appId,
                                                    MapperRegistry mapperRegistry,
                                                    IdInitializer idProvider) {
        return new InstanceContextBuilder(appId, mapperRegistry, idProvider);
    }

    private final long appId;
    private final MapperRegistry mapperRegistry;
    private IInstanceStore instanceStore;
    private IdInitializer idInitializer;
    private Executor executor;
    private @Nullable IInstanceContext parent;
    private List<ContextPlugin> plugins = List.of();
    private TypeDefProvider typeDefProvider;
    private ActiveCommitProvider activeCommitProvider;
    private boolean childLazyLoading;
    private boolean readonly;
    private boolean skipPostprocessing;
    private boolean relocationEnabled;
    private long timeout;
    private boolean changeLogDisabled;
    private boolean migrating;

    public InstanceContextBuilder(long appId,
                                  MapperRegistry mapperRegistry,
                                  IdInitializer idInitializer) {
        this.appId = appId;
        this.mapperRegistry = mapperRegistry;
        this.idInitializer = idInitializer;
    }

    private IInstanceStore getInstanceStore() {
        if (instanceStore == null)
            instanceStore = new InstanceStore(mapperRegistry);
        return instanceStore;
    }

    public InstanceContextBuilder plugins(ContextPlugin...plugins) {
        return plugins(List.of(plugins));
    }

    public InstanceContextBuilder plugins(Function<IInstanceStore, List<ContextPlugin>> pluginsSupplier) {
       return plugins(pluginsSupplier.apply(getInstanceStore()));
    }

    public InstanceContextBuilder plugins(List<ContextPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public InstanceContextBuilder executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public InstanceContextBuilder instanceStore(Function<MapperRegistry, IInstanceStore> instanceStoreSupplier) {
        Utils.require(instanceStore == null, () -> "InstanceStore is already set");
        this.instanceStore = instanceStoreSupplier.apply(mapperRegistry);
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

    public InstanceContextBuilder readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public InstanceContextBuilder childLazyLoading(boolean childLazyLoading) {
        this.childLazyLoading = childLazyLoading;
        return this;
    }

    public InstanceContextBuilder skipPostProcessing(boolean skipPostprocessing) {
        this.skipPostprocessing = skipPostprocessing;
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

    public InstanceContextBuilder changeLogDisabled(boolean changeLogDisabled) {
        this.changeLogDisabled = changeLogDisabled;
        return this;
    }

    public InstanceContextBuilder activeCommitProvider(ActiveCommitProvider activeCommitProvider) {
        this.activeCommitProvider = activeCommitProvider;
        return this;
    }

    public InstanceContextBuilder migrating(boolean migrating) {
        if (migrating)
            instanceStore = new MigrationInstanceStore(getInstanceStore());
        this.migrating = migrating;
        return this;
    }

    public IInstanceContext build() {
        if (executor == null)
            executor = Executors.newSingleThreadExecutor();
        if(changeLogDisabled)
            plugins = Utils.exclude(plugins, p -> p instanceof ChangeLogPlugin);
        var idInitializer = this.idInitializer;
        return new InstanceContext(
                appId, getInstanceStore(), idInitializer, executor,
                plugins, parent,
                childLazyLoading,
                readonly, skipPostprocessing, relocationEnabled, migrating, timeout);
    }

}
