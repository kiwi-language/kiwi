package org.metavm.object.instance.core;

import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
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
import org.metavm.object.type.ActiveCommitProvider;
import org.metavm.object.type.RedirectStatusProvider;
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
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.metavm.util.NncUtils.mergeSets;

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
    private final List<Instance> relocated = new ArrayList<>();
    private final ActiveCommitProvider activeCommitProvider;

    public InstanceContext(long appId,
                           IInstanceStore instanceStore,
                           IdInitializer idInitializer,
                           Executor executor,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeDefProvider typeDefProvider,
                           MappingProvider mappingProvider,
                           RedirectStatusProvider redirectStatusProvider,
                           ActiveCommitProvider activeCommitProvider,
                           boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly,
                           boolean skipPostprocessing,
                           boolean relocationEnabled,
                           long timeout
    ) {
        super(appId,
                List.of(/*new CacheTreeSource(cache),*/new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                new StoreIndexSource(instanceStore), idInitializer,
                parent, typeDefProvider, mappingProvider, redirectStatusProvider, readonly, timeout);
        headContext = new SubContext(appId);
        this.plugins = plugins;
        this.executor = executor;
        this.childrenLazyLoading = childrenLazyLoading;
        this.eventQueue = eventQueue;
        this.instanceStore = instanceStore;
        this.activeCommitProvider = activeCommitProvider;
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
        var commit = activeCommitProvider.getActiveCommit();
        if(commit != null)
            EntityUtils.ensureTreeInitialized(commit);
        headContext.freeze();
        if(commit != null)
            tryCancelCommit(commit);
        finalizeRedirections();
        var migrationResult = relocationEnabled ? relocate() : Relocations.EMPTY;
        var patchContext = new PatchContext(migrationResult);
        var patch = buildPatch(null, patchContext);
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
        try (var ignored = getProfiler().enter("postProcess")) {
            postProcess();
        }
        finished = true;
    }

    private void tryCancelCommit(Commit commit) {
        if(commit != null) {
            forEachInitialized(instance -> {
                instance.forEachReference((r, isChild) -> {
                    if(!isChild && r.isResolved() && r.resolve().isRemoving()) {
                        if(!commit.isCancelled()) {
                            commit.cancel();
                        }
                    }
                });
            });
        }
    }

    private static class PatchContext {

        public static final int MAX_BUILD = 10;
        int numBuild;
        final Relocations relocations;

        public PatchContext(Relocations relocations) {
            this.relocations = relocations;
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
            saveViews();
            craw();
            check();
            initIds();
            var bufferedTrees = buildBufferedTrees();
            var difference = buildDifference(bufferedTrees, patchContext.relocations);
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

    private void removeOrphans(Patch patch, List<Instance> nonPersistedOrphans) {
        try (var ignored = getProfiler().enter("InstanceContext.removeOrphans")) {
            var orphans = new ArrayList<>(nonPersistedOrphans);
            for (var version : patch.entityChange.deletes()) {
                var instance = Objects.requireNonNull(getSelfBuffered(version.id()));
                if (!instance.isRemoved())
                    orphans.add(instance);
            }
            if (!orphans.isEmpty())
                unfrozen(() -> batchRemove(orphans));
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
    public List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
        return batchGetRoots(instanceStore.getByReferenceTargetId(targetId, startExclusive, limit, this));
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

    private List<Instance> getByTypeFromBuffer(Type type, @Nullable Instance startExclusive, int limit, Set<Long> persistedIds) {
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

    private void forEachInitializedRoot(Consumer<Instance> action) {
        for (var instance : this) {
            if (instance.isInitialized() && instance.isRoot())
                action.accept(instance);
        }
    }

    private void forEachInitialized(Consumer<Instance> action) {
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

    private ContextDifference buildDifference(Collection<Tree> bufferedTrees, Relocations relocations) {
        try (var ignored = getProfiler().enter("InstanceContext.buildDifference")) {
            var difference = new ContextDifference(appId);
            difference.diffTrees(headContext.trees(), bufferedTrees);
            difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedTrees));
            if(relocations.isEmpty())
                difference.diffEntities(headContext.trees(), bufferedTrees);
            else {
                List<Instance> toRebuild = new ArrayList<>();
                for (Instance instance : relocations.merged) {
                    toRebuild.add(instance.getRoot());
                    toRebuild.add(instance);
                }
                var discardedTreeIds = new HashSet<Long>();
                relocations.forEach(i -> {
                    discardedTreeIds.add(i.getCurrentId().getTreeId());
                    discardedTreeIds.add(i.getOldId().getTreeId());
                });
                relocations.rollback();
                for (Instance extracted : relocations.extracted) {
                    toRebuild.add(extracted.getRoot());
                }
                var trees = NncUtils.exclude(bufferedTrees, t -> discardedTreeIds.contains(t.id()));
                toRebuild.forEach(i -> trees.add(buildTree(i)));
                difference.diffEntities(headContext.trees(), trees);
                relocations.recover();
            }
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
            return instance.getType().getName();
        else if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityDesc(instance.getMappedEntity());
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
        return NncUtils.join(path, this::getInstanceDesc, "/");
    }

    private void validateRemoval() {
        var visited = new IdentitySet<Instance>();
        forEachInitializedRoot(root -> {
            if (!root.isRemoved() && !root.isEphemeral()) {
                root.visitGraph(i -> {
                    if (i.isRemoved()) {
                        if(DebugEnv.recordPath)
                            logger.debug("Reference path: {}", NncUtils.join(DebugEnv.path));
                        throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, Instances.getInstanceDesc(i.getReference()));
                    }
                    return true;
                }, r -> r.tryGetId() == null || containsIdSelf(r.getId()), visited);
            }
        });
    }

    private void processRemoval(Patch patch) {
        try (var ignored = getProfiler().enter("processRemoval")) {
            var entityChange = patch.entityChange;
            if (NncUtils.isEmpty(entityChange.deletes()))
                return;
            var idsToRemove = new HashSet<Id>();
            for (VersionRT delete : entityChange.deletes()) {
                var i = internalGet(delete.id());
                if(i.tryGetCurrentId() != null)
                    idsToRemove.add(i.getCurrentId());
                if(i.tryGetOldId() != null)
                    idsToRemove.add(i.getOldId());
            }
            var idsToUpdate = NncUtils.mapUnique(entityChange.updates(), v -> v.id().getTreeId());
            var ref = instanceStore.getFirstReference(
                    appId, idsToRemove, mergeSets(NncUtils.mapUnique(idsToRemove, Id::getTreeId), idsToUpdate)
            );
            if (ref != null) {
                unfrozen(() -> {
                    throw BusinessException.strongReferencesPreventRemoval(getRoot(ref.getSourceTreeId()).getReference(),
                            internalGet(ref.getTargetInstanceId()).getReference());
                });
            }
        }
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
                if (instance.isInitialized()
                        && instance.isRoot()
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

    private Set<ReferencePO> getBufferedReferences(Collection<Tree> trees) {
        Set<ReferencePO> references = new HashSet<>();
        for (Tree tree : trees) {
            new ReferenceExtractor(tree.openInput(), appId, references::add).visitGrove();
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
        postProcess0();
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
                typeDefProvider,
                getMappingProvider(),
                getRedirectStatusProvider(),
                activeCommitProvider,
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
            if(!ids.isEmpty()) {
                var root = get(ids.get(0));
                root.accept(new StructuralInstanceVisitor() {
                    @Override
                    public void visitInstance(Instance instance) {
                        if (instance.tryGetId() != null)
                            batch.add(instance);
                        super.visitInstance(instance);
                    }
                });
            }
        }
        var cursor = treeIds.isEmpty() ? 0 : treeIds.get(treeIds.size() - 1);
        return new ScanResult(batch, treeIds.size() < limit, cursor);
    }

    public record Relocations(
            List<Instance> merged,
            List<Instance> extracted
    ) {

        static final Relocations EMPTY = new Relocations(List.of(), List.of());

        void forEach(Consumer<? super Instance> action) {
            merged.forEach(action);
            extracted.forEach(action);
        }

        void rollback() {
            merged.forEach(Instance::rollbackMerge);
            extracted.forEach(Instance::rollbackExtraction);
        }

        void recover() {
            merged.forEach(Instance::merge);
            extracted.forEach(i -> i.extract(i.getRelocatedId().isRoot()));
        }

        public boolean isEmpty() {
            return merged.isEmpty() && extracted.isEmpty();
        }
    }

    private void finalizeRedirections() {
        forEachInitialized(instance -> instance.transformReference(r -> {
            if(r instanceof RedirectingReference redirectingRef && redirectingRef.shouldRedirect())
                return redirectingRef.getRedirectionReference();
            else
                return r;
        }));
    }

    private Relocations relocate() {
        var merged = merge();
        var extracted = extract();
        mergeDetachedValues();
        return new Relocations(merged, extracted);
    }

    private List<Instance> merge() {
        var merged = new ArrayList<Instance>();
        forEachInitialized(instance -> {
            if (!instance.isEphemeral() && !instance.isRemoved()) {
                if(instance.canMerge()) {
                    instance.forEachDescendant(i -> {
                        i.merge();
                        addForwardingPointer(new ForwardingPointer(i.getOldId(), i.getCurrentId()));
                        mapManually(i.getId(), i);
                        merged.add(i);
                    });
                    this.relocated.add(instance);
                }
            }
        });
        return merged;
    }

    private void mergeDetachedValues() {
        forEachInitialized(instance -> {
            if(!instance.isEphemeral() && !instance.isRemoved()) {
                instance.transformReference((r, isChild) -> {
                    if(r.isValueReference() && !isChild) {
                        var v = r.resolve();
                        if(v.isDetachedValue())
                            return v.copy().getReference();
                    }
                    return r;
                });
            }
        });
    }

    private List<Instance> extract() {
        var extracted = new ArrayList<Instance>();
        forEachInitialized(instance -> {
            if (!instance.isRemoved() && !instance.isEphemeral() && instance.canExtract()) {
                //                    allExtracted.add(i);
                var ref = instance.getReference();
                Objects.requireNonNull(instance.getParent()).forEachReference(r -> {
                    if(r.equals(ref))
                        r.setForwarded();
                });
                instance.forEachDescendant(i -> {
                    i.extract(i == instance);
                    extracted.add(i);
                });
                relocated.add(instance);
            }
        });
        idInitializer.initializeIds(appId, extracted);
        for (Instance instance : extracted) {
            addForwardingPointer(new ForwardingPointer(instance.getOldId(), instance.getCurrentId()));
            mapManually(instance.getId(), instance);
        }
        return extracted;
    }

    @Override
    public List<Instance> getRelocated() {
        return Collections.unmodifiableList(relocated);
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
                    if (f.isChild() && v instanceof Reference r) {
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
