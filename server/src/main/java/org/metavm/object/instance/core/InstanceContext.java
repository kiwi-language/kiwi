package org.metavm.object.instance.core;

import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.ContextPlugin;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.StoreTreeSource;
import org.metavm.object.instance.StoreVersionSource;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.metavm.util.NncUtils.mergeSets;

public class InstanceContext extends BufferingInstanceContext {

    private boolean finished;
    private final SubContext headContext;
    private final boolean asyncPostProcessing;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private final boolean childrenLazyLoading;
    @Nullable
    private final EventQueue eventQueue;
    private final IInstanceStore instanceStore;
    private final Cache cache;
    private final boolean skipPostprocessing;
    private final boolean migrationDisabled;

    public InstanceContext(long appId,
                           IInstanceStore instanceStore,
                           IdInitializer idInitializer,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeDefProvider typeDefProvider,
                           MappingProvider mappingProvider,
                           boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly,
                           boolean skipPostprocessing,
                           boolean migrationDisabled
    ) {
        super(appId,
                List.of(/*new CacheTreeSource(cache),*/new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                new StoreIndexSource(instanceStore), idInitializer,
                parent, typeDefProvider, mappingProvider, readonly);
        headContext = new SubContext(appId);
        this.asyncPostProcessing = asyncPostProcessing;
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
        this.migrationDisabled = migrationDisabled;
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
        headContext.freeze();
        var patchContext = new PatchContext();
        var patch = buildPatch(null, patchContext);
        if(!migrationDisabled)
            patch = migrate(patch);
        validateRemoval();
        processRemoval(patch);
        /*patch = */
        beforeSaving(patch, patchContext);
        saveInstances(patch.treeChanges);
        saveReferences(patch);
        afterSaving(patch);
        headContext.unfreeze();
        headContext.clear();
        patch.trees.forEach(headContext::add);
        if (TransactionSynchronizationManager.isActualTransactionActive())
            registerTransactionSynchronization();
        else {
            try (var ignored = getProfiler().enter("postProcess")) {
                postProcess();
            }
        }
        finished = true;
    }

    private static class PatchContext {

        public static final int MAX_BUILD = 10;
        int numBuild;

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
            saveViews();
            craw();
            check();
            initIds();
            var bufferedTrees = getBufferedTrees();
            var difference = buildDifference(bufferedTrees);
            var entityChange = difference.getEntityChange();
            var refChange = difference.getReferenceChange();
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
            return processChanges(new Patch(bufferedTrees, entityChange, treeChanges, refChange), computeNonPersistedOrphans(), patchContext);
        }
    }

    private void removeOrphans(Patch patch, List<DurableInstance> nonPersistedOrphans) {
        try (var ignored = getProfiler().enter("InstanceContext.removeOrphans")) {
            var orphans = new ArrayList<>(nonPersistedOrphans);
            for (var version : patch.entityChange.deletes()) {
                var instance = Objects.requireNonNull(getSelfBuffered(version.id()));
                if (!instance.isRemoved())
                    orphans.add(instance);
            }
//            logger.debug("Removing orphans, numOrphans: {}, numNonPersistedOrphans: {}", orphans.size(), nonPersistedOrphans.size());
            if (!orphans.isEmpty()) {
                batchRemove(orphans);
            }
        }
    }

    private void check() {
        try (var ignored = getProfiler().enter("check")) {
            clearMarks();
            forEachInitializedRoot(root -> {
                if (root.getParent() == null) {
                    root.forEachDescendant(instance -> {
                        if (instance.isMarked())
                            throw new BusinessException(ErrorCode.MULTI_PARENT, Instances.getInstanceDesc(instance.getReference()));
                        instance.setMarked(true);
                    });
                }
            });
        }
    }

    @Override
    public List<DurableInstance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.getByReferenceTargetId(
                        targetId,
                        startExclusive,
                        limit,
                        this
                ),
                this::getRoot
        );
    }


    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            var instancesToInitId =
                    NncUtils.filter(this, i -> !i.isIdInitialized() && !i.isEphemeral() && !i.isValue());
            if (instancesToInitId.isEmpty())
                return;
            idInitializer.initializeIds(appId, instancesToInitId);
            for (var instance : instancesToInitId) {
                onIdInitialized(instance);
            }
        }
    }

    private List<DurableInstance> getByTypeFromBuffer(Type type, @Nullable DurableInstance startExclusive, int limit, Set<Long> persistedIds) {
        var typeInstances = NncUtils.filter(
                this,
                i -> type == i.getType() && !persistedIds.contains(i.tryGetTreeId())
        );
        if (startExclusive == null)
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    private void forEachInitializedRoot(Consumer<DurableInstance> action) {
        for (var instance : this) {
            if (instance.isInitialized() && instance.isRoot())
                action.accept(instance);
        }
    }

    private void forEachInitialized(Consumer<DurableInstance> action) {
        for (var instance : this) {
            if (instance.isInitialized())
                action.accept(instance);
        }
    }

    private record Patch(List<Tree> trees,
                         EntityChange<VersionRT> entityChange,
                         EntityChange<InstancePO> treeChanges,
                         EntityChange<ReferencePO> referenceChange) {
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
            difference.diff(headContext.trees(), bufferedTrees);
            difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedTrees));
            return difference;
        }
    }

    private Patch processChanges(Patch patch, List<DurableInstance> nonPersistedOrphans, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("InstanceContext.processChanges")) {
            if (DebugEnv.debugging)
                debugLogger.info("nonPersistedOrphans: [{}]", NncUtils.join(nonPersistedOrphans, i -> Instances.getInstanceDesc(i) + "/" + i.getStringId()));
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
                Consumer<DurableInstance> processRemove = instance -> {
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

    private String getInstanceDesc(DurableInstance instance) {
        if (instance instanceof ArrayInstance)
            return instance.getType().getName();
        else if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityDesc(instance.getMappedEntity());
        else
            return instance.toString();
    }

    private String getInstancePath(DurableInstance instance) {
        var path = new LinkedList<DurableInstance>();
        var i = instance;
        while (i != null) {
            path.addFirst(i);
            i = NncUtils.get(i.getParent(), InstanceReference::resolve);
        }
        return NncUtils.join(path, this::getInstanceDesc, "/");
    }

    private void validateRemoval() {
        var visited = new IdentitySet<DurableInstance>();
        forEachInitializedRoot(root -> {
            if (!root.isRemoved() && !root.isEphemeral()) {
                root.visitGraph(i -> {
                    if (i.isRemoved()) {
                        throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, Instances.getInstanceDesc(i.getReference()));
                    }
                    return true;
                }, r -> r.tryGetId() == null || containsIdSelf(r.getId()), visited);
            }
        });
    }

    private void processRemoval(Patch patch) {
        try (var ignored = getProfiler().enter("processRemoval")) {
//            removeOrphans(patch, List.of());
            var entityChange = patch.entityChange;
            if (NncUtils.isEmpty(entityChange.deletes()))
                return;
            var idsToRemove = NncUtils.mapUnique(entityChange.deletes(), VersionRT::id);
            var idsToUpdate = NncUtils.mapUnique(entityChange.updates(), v -> v.id().getTreeId());
            var ref = instanceStore.getFirstReference(
                    appId, idsToRemove, mergeSets(NncUtils.mapUnique(idsToRemove, Id::getTreeId), idsToUpdate)
            );
            if (ref != null)
                throw BusinessException.strongReferencesPreventRemoval(getRoot(ref.getSourceTreeId()).getReference(),
                        internalGet(ref.getTargetInstanceId()).getReference());
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private List<Tree> getBufferedTrees() {
        try (var ignored = getProfiler().enter("getBufferedTrees")) {
            var trees = new ArrayList<Tree>();
            for (var instance : this) {
                if (instance.isInitialized()
                        && instance.isRoot()
                        && !instance.isRemoved() && !instance.isEphemeral()
                        && !instance.isLoadedFromCache()) {
                    var tree = new Tree(
                            instance.getTreeId(),
                            instance.getVersion(),
                            instance.getNextNodeId(),
                            InstanceOutput.toBytes(instance)
                    );
                    trees.add(tree);
                }
            }
            return trees;
        }
    }

    private Set<ReferencePO> getBufferedReferences(Collection<Tree> trees) {
        Set<ReferencePO> references = new HashSet<>();
        for (Tree tree : trees) {
            if (!tree.migrated())
                new ReferenceExtractor(tree.openInput(), appId, references::add).visitMessage();
        }
        return references;
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
        if (asyncPostProcessing) {
            executor.execute(this::postProcess0);
        } else {
            postProcess0();
        }
    }

    private void postProcess0() {
        for (ContextPlugin plugin : plugins) {
            plugin.postProcess(this);
        }
    }

    private void saveInstances(EntityChange<InstancePO> change) {
        try (var ignored = getProfiler().enter("processEntityChangeHelper")) {
            instanceStore.save(change.toChangeList());
        }
    }

    private void saveReferences(Patch patch) {
        var diff = new ContextDifference(appId);
        diff.diffReferences(headContext.getReferences(), getBufferedReferences(patch.trees));
        try (var ignored = getProfiler().enter("saveReferences")) {
            instanceStore.saveReferences(diff.getReferenceChange().toChangeList());
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
        NncUtils.requireTrue(TransactionSynchronizationManager.isActualTransactionActive(),
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
        return new InstanceContext(
                appId,
                instanceStore,
                idInitializer,
                executor,
                asyncPostProcessing,
                plugins,
                getParent(),
                getTypeDefProvider(),
                getMappingProvider(),
                childrenLazyLoading,
                cache,
                eventQueue,
                isReadonly(),
                skipPostprocessing,
                migrationDisabled
        );
    }

    @Override
    public List<InstanceReference> scan(long start, long limit) {
        var treeIds = instanceStore.scan(getAppId(), start, limit);
        treeIds.forEach(loadingBuffer::buffer);
        loadingBuffer.flush();
        var ids = NncUtils.flatMap(treeIds, loadingBuffer::getIdsInTree);
        return NncUtils.map(ids, this::createReference);
    }

    private Patch migrate(Patch patch) {
        var migrated = new ArrayList<DurableInstance>();
        var roots = new HashSet<DurableInstance>();
        forEachInitialized(i -> {
            if (!i.isEphemeral() && !i.isRemoved() && i.isMigratable()) {
//                assert !i.getAggregateRoot().isRemoved();
                roots.add(i.getAggregateRoot());
                i.forEachDescendant(instance -> {
                    instance.migrate();
                    logger.info("Instance {} migrated from {} to {}", i, i.getId().getTreeId(), i.getRoot().getTreeId());
                    mapManually(instance.getId(), instance);
                });
                migrated.add(i);
            }
        });
        if (migrated.isEmpty())
            return patch;
        var trees = new ArrayList<>(patch.trees);
        var inserts = new ArrayList<>(patch.treeChanges.inserts());
        var updates = new ArrayList<>(patch.treeChanges.updates());
        for (DurableInstance root : roots) {
            var i = new InstancePO(
                    appId,
                    root.getTreeId(),
                    InstanceOutput.toBytes(root),
                    root.getVersion() + 1,
                    root.getSyncVersion(),
                    root.getNextNodeId()
            );
            boolean added = NncUtils.replace(inserts, i, (t1, t2) -> t1.getId() == t2.getId());
            if (!added)
                NncUtils.replaceOrAppend(updates, i, (t1, t2) -> t1.getId() == t2.getId());
            NncUtils.replace(trees, i.toTree(), (t1, t2) -> t1.id() == t2.id());
        }
        for (DurableInstance instance : migrated) {
            var originalTreeId = instance.getId().getTreeId();
            var i = new InstancePO(
                    appId,
                    originalTreeId,
                    InstanceOutput.toMigrationsBytes(instance),
                    0L,
                    0L,
                    0L
            );
            NncUtils.replaceOrAppend(updates, i, (t1, t2) -> t1.getId() == t2.getId());
            trees.removeIf(t -> t.id() == originalTreeId);
        }
        var treeChanges = EntityChange.create(InstancePO.class, inserts, updates, patch.treeChanges.deletes());
        return new Patch(
                trees,
                patch.entityChange,
                treeChanges,
                patch.referenceChange
        );
    }

    // For debugging, don't remove
    @SuppressWarnings("unused")
    private void logTreeChanges(EntityChange<InstancePO> treeChanges) {
        logger.debug("inserts");
        for (InstancePO insert : treeChanges.inserts()) {
            logger.debug("Tree ID: {}", insert.getId());
        }
        logger.debug("updates");
        for (InstancePO update : treeChanges.updates()) {
            logger.debug("Tree ID: {}, size: {}", update.getId(), update.getData().length);
        }
        logger.debug("deletes");
        for (InstancePO delete : treeChanges.deletes()) {
            logger.debug("Tree ID: {}", delete.getId());
        }
    }

    private void loadSeparateChildren() {
        // Assuming multiple levels of separate children are rare, hence only one level of buffering is applied
        forEachInitialized(i -> {
            if(i instanceof ClassInstance classInstance) {
                classInstance.forEachField((f, v) -> {
                    if (f.isChild() && v instanceof InstanceReference r) {
                        if (!r.isInitialized()) {
                            buffer(r.getId());
                        }
                    }
                });
            }
        });
        forEachInitializedRoot(r -> {
            if (r.getParent() == null)
                r.forEachDescendant(i -> {});
        });
    }

    private void unfrozen(Runnable action) {
        headContext.unfreeze();
        action.run();
        headContext.freeze();
    }

}
