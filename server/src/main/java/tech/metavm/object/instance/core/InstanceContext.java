package tech.metavm.object.instance.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.StoreTreeSource;
import tech.metavm.object.instance.StoreVersionSource;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static tech.metavm.util.NncUtils.mergeSets;

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
                           boolean readonly
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
    }

    @Override
    protected void onTreeLoaded(Tree tree) {
        headContext.add(tree);
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
        var patchContext = new PatchContext();
        var patch = buildPatch(null, patchContext);
        checkRemoval();
        processRemoval(patch);
        patch = beforeSaving(patch, patchContext);
        saveInstances(patch.treeChanges);
        afterSaving(patch);
        saveReferences(patch.referenceChange);
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
            onPatchBuild();
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
            if (DebugEnv.buildPatchLog)
                debugLogger.info("removeOrphans, numOrphans: {}, numNonPersistedOrphans: {}", orphans.size(), nonPersistedOrphans.size());
            if (!orphans.isEmpty()) {
                batchRemove(orphans);
            }
        }
    }

    private void check() {
        try (var ignored = getProfiler().enter("check")) {
            var checker = new StructuralVisitor() {
                @Override
                public Void visitDurableInstance(DurableInstance instance) {
                    if (instance.isMarked())
                        throw new BusinessException(ErrorCode.MULTI_PARENT, Instances.getInstanceDesc(instance));
                    instance.setMarked(true);
                    return super.visitDurableInstance(instance);
                }
            };
            clearMarks();
            forEachInitializedRoot(i -> i.accept(checker));
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
                    NncUtils.filter(this, i -> !i.isIdInitialized() && !i.isEphemeral());
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
                if (plugin.beforeSaving(patch.entityChange, this))
                    patch = buildPatch(patch, patchContext);
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
            i = i.getParent();
        }
        return NncUtils.join(path, this::getInstanceDesc, "/");
    }

    private void checkRemoval() {
        var visitor = new GraphVisitor() {

            private final LinkedList<String> path = new LinkedList<>();

            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                if (DebugEnv.removeCheckVerbose)
                    path.addLast(getInstancePath(instance));
                if (instance.isRemoved()) {
                    if (DebugEnv.removeCheckVerbose)
                        logger.info("reference path: {}", String.join("->", path));
                    throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, Instances.getInstanceDesc(instance));
                }
                if (DebugEnv.removeCheckVerbose) {
                    try {
                        numCalls++;
                        if (!visited.add(instance))
                            return null;
                        if (instance instanceof ArrayInstance arrayInstance) {
                            for (int i = 0; i < arrayInstance.getElements().size(); i++) {
                                path.addLast(i + "");
                                visit(arrayInstance.getElement(i));
                                path.removeLast();
                            }
                        } else if (instance instanceof ClassInstance classInstance) {
                            classInstance.forEachField((field, fieldValue) -> {
                                path.addLast(field.getName());
                                visit(fieldValue);
                                path.removeLast();
                            });
                            classInstance.forEachUnknownField((fieldValue) -> {
                                path.addLast("<unknown>");
                                visit(fieldValue);
                                path.removeLast();
                            });
                        }
                        return null;
                    } finally {
                        path.removeLast();
                    }
                } else
                    return super.visitDurableInstance(instance);
            }
        };
        for (var instance : this) {
            if (instance.isRoot() && !instance.isRemoved() && !instance.isEphemeral())
                visitor.visit(instance);
        }
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
                throw BusinessException.strongReferencesPreventRemoval(getRoot(ref.getSourceTreeId()),
                        internalGet(ref.getTargetInstanceId()));
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
                if (instance.isInitialized() && instance.isRoot()
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
            new ReferenceExtractor(tree.openInput(), appId, references::add).visitMessage();
        }
        return references;
    }

    private void registerTransactionSynchronization() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        postProcess();
                    }
                }
        );
    }

    public static final Pattern PTN = Pattern.compile("对象'.+\\-(.+)'不存在");

    private void postProcess() {
        if (asyncPostProcessing) {
            executor.execute(this::postProcess0);
        } else {
            try {
                postProcess0();
            } catch (BusinessException e) {
                if (DebugEnv.debugging) {
                    var m = PTN.matcher(e.getMessage());
                    if (m.matches()) {
                        var id = Id.parse(m.group(1));
                        var instance = get(id);
                        var field = (Field) Objects.requireNonNull(instance.getMappedEntity());
                        var type = field.getDeclaringType();
                        var logs = getAttribute(ContextAttributeKey.CHANGE_LOGS);
                        var log = NncUtils.find(logs, l -> l.getId().equals(type.getId()));
                        Entity root = field;
                        while (root.getParentEntity() != null)
                            root = root.getParentEntity();
                        debugLogger.info("field: {}, root: {}, log: {}", EntityUtils.getEntityDesc(field), EntityUtils.getEntityDesc(root), log);
                    }
                }
                throw e;
            }
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

    private void saveReferences(EntityChange<ReferencePO> referenceChange) {
        try (var ignored = getProfiler().enter("saveReferences")) {
            instanceStore.saveReferences(referenceChange.toChangeList());
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
                eventQueue, isReadonly());
    }

    @Override
    public List<DurableInstance> scan(long start, long limit) {
        var treeIds = instanceStore.scan(getAppId(), start, limit);
        treeIds.forEach(loadingBuffer::buffer);
        loadingBuffer.flush();
        var ids = NncUtils.flatMap(treeIds, loadingBuffer::getIdsInTree);
        return NncUtils.map(ids, this::get);
    }

}
