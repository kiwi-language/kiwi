package org.metavm.object.instance.core;

import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.entity.*;
import org.metavm.event.EventQueue;
import org.metavm.flow.Method;
import org.metavm.object.instance.ContextPlugin;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.StoreTreeSource;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class InstanceContext extends BufferingInstanceContext {

    private boolean finished;
    private final SubContext headContext;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private final boolean childrenLazyLoading;
    @Nullable
    private final EventQueue eventQueue;
    private final IInstanceStore instanceStore;
    private final Cache cache;
    private final boolean skipPostprocessing;
    private final boolean relocationEnabled;

    public InstanceContext(long appId,
                           IInstanceStore instanceStore,
                           IdInitializer idInitializer,
                           Executor executor,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly,
                           boolean skipPostprocessing,
                           boolean relocationEnabled,
                           long timeout
    ) {
        super(appId,
                List.of(new StoreTreeSource(instanceStore)),
                new StoreIndexSource(instanceStore), idInitializer,
                parent, readonly, timeout);
        headContext = new SubContext(appId);
        this.plugins = plugins;
        this.executor = executor;
        this.childrenLazyLoading = childrenLazyLoading;
        this.eventQueue = eventQueue;
        this.instanceStore = instanceStore;
//        entityContext = new EntityContext(
//                this,
//                NncUtils.get(parent, IInstanceContext::getEntityContext),
//                defContext
//        );
        this.cache = cache;
        this.skipPostprocessing = skipPostprocessing;
        this.relocationEnabled = relocationEnabled;
    }

    @Override
    protected boolean onTreeLoaded(Tree tree) {
        return headContext.tryAdd(tree);
    }

    @Override
    public @Nullable EventQueue getEventQueue() {
        return eventQueue;
    }

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @Override
    protected void finishInternal() {
        if (finished)
            throw new IllegalStateException("Already finished");
        if (DebugEnv.debugging)
            debugLogger.info("InstanceContext.finish");
        var commit = getActiveCommit();
        headContext.freeze();
        if(commit != null)
            tryCancelCommit(commit);
        var patchContext = new PatchContext();
        var patch = buildPatch(null, patchContext);
        validateRemoval();
        /*patch = */
        beforeSaving(patch, patchContext);
        saveInstances(patch.treeChanges);
        afterSaving(patch);
        headContext.unfreeze();
        headContext.clear();
        patch.trees.forEach(headContext::add);
        try (var ignored = getProfiler().enter("postProcess")) {
            postProcess();
        }
        finished = true;
    }

    public Commit getActiveCommit() {
        return selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
    }


    private void tryCancelCommit(Commit commit) {
        if(commit != null) {
            forEach(instance -> {
                if (instance instanceof MvInstance mvInst) {
                    mvInst.forEachReference((r, isChild) -> {
                        if (!isChild && r.isResolved() && r.resolveDurable() instanceof MvInstance mv && mv.isRemoving()) {
                            if (!commit.isCancelled()) {
                                commit.cancel();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public long allocateTreeId() {
        var idProvider = ((DefaultIdInitializer) idInitializer).getIdProvider();
        return idProvider.allocateOne(appId);
    }

    private static class PatchContext {

        public static final int MAX_BUILD = 10;
        int numBuild;
        public PatchContext() {
        }

        void incBuild() {
            if (++numBuild > MAX_BUILD)
                throw new InternalException("Too many patch build");
        }
    }

    private Patch buildPatch(@Nullable Patch prevPatch, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("buildPatch")) {
            patchContext.incBuild();
            if (DebugEnv.buildPatchLog)
                debugLogger.info("building patch. numBuild: {}", patchContext.numBuild);
            unfrozen(this::onPatchBuild);
            craw();
            check();
            var bufferedTrees = buildBufferedTrees();
            var difference = buildDifference(bufferedTrees);
            var entityChange = difference.getEntityChange();
            var treeChanges = difference.getTreeChanges();
            onContextInitializeId();
            if (prevPatch != null) {
                try (var ignored2 = getProfiler().enter("InstanceContext.buildPatch.setAttributes")) {
                    prevPatch.entityChange.getAttributes().forEach((key, value) -> {
                        //noinspection rawtypes,unchecked
                        entityChange.setAttribute((DifferenceAttributeKey) key, value);
                    });
                }
            }
            return processChanges(new Patch(bufferedTrees, entityChange, treeChanges), computeNonPersistedOrphans(), patchContext);
        }
    }

    private void removeOrphans(Patch patch, List<Instance> nonPersistedOrphans) {
        try (var ignored = getProfiler().enter("InstanceContext.removeOrphans")) {
            var orphans = new ArrayList<>(nonPersistedOrphans);
            for (var version : patch.entityChange.deletes()) {
                var instance = Objects.requireNonNull(getSelfBuffered(version.id()));
                if (!instance.isRemoved() && !instance.isValue())
                    orphans.add(instance);
            }
            if (!orphans.isEmpty())
                unfrozen(() -> batchRemove(orphans));
        }
    }

    private void check() {
        try (var ignored = getProfiler().enter("check")) {
            clearMarks();
            forEachRoot(root -> {
                if (root.getParent() == null) {
                    root.forEachDescendant(instance -> {
                        if (instance.isMarked()) {
                            throw new BusinessException(ErrorCode.MULTI_PARENT, Instances.getInstanceDesc(instance.getReference()));
                        }
                        instance.setMarked();
                    });
                }
            });
        }
    }

    private void forEachRoot(Consumer<? super Instance> action) {
        for (var instance : this) {
            if (instance.isRoot())
                action.accept(instance);
        }
    }

    public void forEach(Consumer<? super Instance> action) {
        for (var instance : this) {
            action.accept(instance);
        }
    }

    private record Patch(List<Tree> trees,
                         EntityChange<VersionRT> entityChange,
                         EntityChange<InstancePO> treeChanges) {
    }

    private Patch beforeSaving(Patch patch, PatchContext patchContext) {
        for (ContextPlugin plugin : plugins) {
            try (var ignored = getProfiler().enter(plugin.getClass().getSimpleName() + ".beforeSaving")) {
                plugin.beforeSaving(patch.entityChange, this);
//                if (plugin.beforeSaving(patch.entityChange, this))
//                    patch = buildPatch(patch, patchContext);
            }
        }
        return patch;
    }

    private void afterSaving(Patch patch) {
        try (var ignored = getProfiler().enter("InstanceContext.afterSaving")) {
            plugins.forEach(plugin -> plugin.afterSaving(patch.entityChange, this));
        }
    }

    private ContextDifference buildDifference(Collection<Tree> bufferedTrees) {
        try (var ignored = getProfiler().enter("InstanceContext.buildDifference")) {
            var difference = new ContextDifference(appId);
            difference.diffTrees(headContext.trees(), bufferedTrees);
            difference.diffEntities(headContext.trees(), bufferedTrees);
            return difference;
        }
    }

    private Tree buildTree(Instance instance) {
        var bout = new ByteArrayOutputStream();
        var out = new InstanceOutput(bout);
        out.writeInt(1);
        instance.writeTo(out);
        return new InstancePO(
                appId,
                instance.getTreeId(),
                bout.toByteArray(),
                instance.getVersion(),
                instance.getSyncVersion(),
                instance.getNextNodeId()
        ).toTree();
    }

    private Patch processChanges(Patch patch, List<Instance> nonPersistedOrphans, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("InstanceContext.processChanges")) {
            if (DebugEnv.debugging)
                debugLogger.info("nonPersistedOrphans: [{}]", Utils.join(nonPersistedOrphans, i -> Instances.getInstanceDesc(i) + "/" + i.getStringId()));
            var ref = new Object() {
                boolean changed = false;
            };
            removeOrphans(patch, nonPersistedOrphans);
            // Temporarily unfreeze the head context because custom onChange and oRemove callbacks may load instances
            unfrozen(() -> {
                patch.entityChange.forEachInsertOrUpdate(v -> {
                    var instance = internalGet(v.id());
                    if (instance.setChangeNotified()) {
                        if (onChange(instance)) {
                            if (DebugEnv.buildPatchLog && !ref.changed)
                                debugLogger.info("insert/update change detected {}, numBuilds: {}", Instances.getInstancePath(instance), patchContext.numBuild);
                            ref.changed = true;
                        }
                    }
                });
                Consumer<Instance> processRemove = instance -> {
                    if (instance.setRemovalNotified()) {
                        if (onRemove(instance)) {
                            if (DebugEnv.buildPatchLog && !ref.changed)
                                debugLogger.info("removal change detected {}, numBuilds: {}", Instances.getInstancePath(instance), patchContext.numBuild);
                            ref.changed = true;
                        }
                    }
                };
                patch.entityChange.deletes().forEach(v -> processRemove.accept(internalGet(v.id())));
                nonPersistedOrphans.forEach(processRemove);
            });
            return ref.changed ? buildPatch(patch, patchContext) : patch;
        }
    }

    private String getInstanceDesc(Instance instance) {
        if (instance instanceof ArrayInstance)
            return instance.getInstanceType().getName();
        else if (instance instanceof Entity entity)
            return EntityUtils.getEntityDesc(entity);
        else
            return instance.toString();
    }

    private String getInstancePath(Instance instance) {
        var path = new LinkedList<Instance>();
        var i = instance;
        while (i != null) {
            path.addFirst(i);
            i = i.getParent();
        }
        return Utils.join(path, this::getInstanceDesc, "/");
    }

    private void validateRemoval() {
        var visited = new IdentitySet<Instance>();
        forEachRoot(root -> {
            if (!root.isRemoved() && !root.isEphemeral()) {
                root.visitGraph(i -> {
                    if (i.isRemoved()) {
                        if(DebugEnv.recordPath)
                            logger.info("Reference path: {}", Utils.join(DebugEnv.path));
                        throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, Instances.getInstanceDesc(i.getReference()));
                    }
                    return true;
                }, r -> !(r instanceof EntityReference er) || containsIdSelf(er.getId()), visited);
            }
        });
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private List<Tree> buildBufferedTrees() {
        try (var ignored = getProfiler().enter("buildBufferedTrees")) {
            var trees = new ArrayList<Tree>();
            var fpMap = new HashMap<>(getForwardingPointers());
            for (var instance : this) {
                if (instance.isRoot()
                        && !instance.isRemoved() && !instance.isEphemeral()
                        && !instance.isLoadedFromCache()) {
                    var bout = new ByteArrayOutputStream();
                    var out = new InstanceOutput(bout);
                    var fps = fpMap.remove(instance.getTreeId());
                    out.writeInt(fps != null ? 1 + fps.size() : 1);
                    if(fps != null)
                        fps.forEach(fp -> fp.writeTo(out));
                    instance.writeTo(out);
                    trees.add(new InstancePO(
                            appId,
                            instance.getTreeId(),
                            bout.toByteArray(),
                            instance.getVersion(),
                            instance.getSyncVersion(),
                            instance.getNextNodeId()
                    ).toTree());
                }
            }
            fpMap.forEach((treeId, fps) -> {
                if(fps.isEmpty())
                    return;
                var bout = new ByteArrayOutputStream();
                var out = new InstanceOutput(bout);
                out.writeInt(fps.size());
                fps.forEach(fp -> fp.writeTo(out));
                trees.add(new InstancePO(
                        appId,
                        treeId,
                        bout.toByteArray(),
                        0,
                        0,
                        0
                ).toTree());
            });
            return trees;
        }
    }

    private void registerTransactionSynchronization() {
        if (skipPostprocessing)
            return;
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        postProcess();
                    }
                }
        );
    }

    private void postProcess() {
        if (skipPostprocessing)
            return;
        postProcess0();
    }

    private void postProcess0() {
        for (ContextPlugin plugin : plugins) {
            plugin.postProcess(this);
        }
    }

    private void saveInstances(EntityChange<InstancePO> change) {
        try (var ignored = getProfiler().enter("processEntityChangeHelper")) {
            instanceStore.save(appId, change.toChangeList());
        }
    }

    public IInstanceStore getInstanceStore() {
        return instanceStore;
    }

    public <T extends ContextPlugin> T getPlugin(Class<T> pluginClass) {
        for (ContextPlugin plugin : plugins) {
            if (pluginClass.isInstance(plugin))
                return pluginClass.cast(plugin);
        }
        throw new InternalException("Can not find plugin: " + pluginClass.getName());
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        Utils.require(TransactionSynchronizationManager.isActualTransactionActive(),
                "Can not register commit callback outside of transactions");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    @Override
    public IInstanceContext createSame(long appId) {
        return createSame(appId, getTypeDefProvider());
    }

    @Override
    public IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider) {
        return new InstanceContext(
                appId,
                instanceStore,
                idInitializer,
                executor,
                plugins,
                getParent(),
                childrenLazyLoading,
                cache,
                eventQueue,
                isReadonly(),
                skipPostprocessing,
                relocationEnabled,
                getTimeout()
        );
    }

    @Override
    public ScanResult scan(long start, long limit) {
        var treeIds = instanceStore.scan(getAppId(), start, limit);
        treeIds.forEach(loadingBuffer::buffer);
        loadingBuffer.flush();
        var batch = new ArrayList<Instance>();
        for (Long treeId : treeIds) {
            var ids = loadingBuffer.getIdsInTree(treeId);
            if(ids != null && !ids.isEmpty()) {
                var root = get(ids.getFirst());
                root.accept(new StructuralInstanceVisitor() {
                    @Override
                    public Void visitInstance(Instance instance) {
                        batch.add(instance);
                        super.visitInstance(instance);
                        return null;
                    }
                });
            }
        }
        var cursor = treeIds.isEmpty() ? 0 : treeIds.getLast();
        return new ScanResult(batch, treeIds.size() < limit, cursor);
    }

    // For debugging, don't remove
    @SuppressWarnings("unused")
    private void logTreeChanges(EntityChange<InstancePO> treeChanges) {
        logger.info("inserts");
        for (InstancePO insert : treeChanges.inserts()) {
            logger.info("Tree ID: {}", insert.getId());
        }
        logger.info("updates");
        for (InstancePO update : treeChanges.updates()) {
            logger.info("Tree ID: {}, size: {}", update.getId(), update.getData().length);
        }
        logger.info("deletes");
        for (InstancePO delete : treeChanges.deletes()) {
            logger.info("Tree ID: {}", delete.getId());
        }
    }

    private void unfrozen(Runnable action) {
        headContext.unfreeze();
        action.run();
        headContext.freeze();
    }

}
