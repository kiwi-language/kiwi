package tech.metavm.object.instance.core;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static tech.metavm.util.NncUtils.mergeSets;

public class InstanceContext extends BufferingInstanceContext {

    private final Map<LoadByTypeRequest, List<InstancePO>> loadByTypeCache = new HashMap<>();
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
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeProvider typeProvider,
                           MappingProvider mappingProvider,
                           ParameterizedFlowProvider parameterizedFlowProvider,
                           boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly
    ) {
        super(appId,
                List.of(new CacheTreeSource(cache),
                        new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                new StoreIndexSource(instanceStore), idService,
                parent, typeProvider, mappingProvider, parameterizedFlowProvider, readonly);
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
    protected void onReplace(List<DurableInstance> replacements) {
        for (Instance replacement : replacements) {
            buffer(Objects.requireNonNull(replacement.getId()));
        }
        for (var replacement : replacements) {
            var tree = loadingBuffer.tryGetTree(replacement.getPhysicalId());
            if (tree != null) {
                headContext.add(tree);
            }
        }
    }

    @Override
    protected void onTreeLoaded(Tree tree) {
        headContext.add(tree);
    }

    @Override
    public @Nullable EventQueue getEventQueue() {
        return eventQueue;
    }

    @Override
    protected void finishInternal() {
        if (finished)
            throw new IllegalStateException("Already finished");
        var patchContext = new PatchContext();
        var patch = buildPatch(null, patchContext);
        processRemoval(patch);
        patch = beforeSaving(patch, patchContext);
        saveInstances(patch.entityChange);
        afterSaving(patch);
        saveReferences(patch.referenceChange);
        headContext.clear();
        patch.instancePOs.keySet().forEach(headContext::add);
        if (TransactionSynchronizationManager.isActualTransactionActive())
            registerTransactionSynchronization();
        else {
            try (var ignored = getProfiler().enter("postProcess")) {
                postProcess();
            }
        }
        finished = true;
    }

    private void incPOVersions(Patch patch) {
        patch.entityChange.forEachUpdateOrDelete(i -> i.setVersion(i.getVersion() + 1));
    }

    private void incVersions(Patch patch) {
        patch.entityChange.forEachUpdateOrDelete(i -> internalGet(new PhysicalId(i.getId())).incVersion());
    }

    private static class PatchContext {

        public static final int MAX_BUILD = 10;

        int numBuild;
        final Set<Long> changeNotified = new HashSet<>();

        void incBuild() {
            if (++numBuild > MAX_BUILD)
                throw new InternalException("Too many patch build");
        }
    }

    private Patch buildPatch(@Nullable Patch prevPatch, PatchContext patchContext) {
        patchContext.incBuild();
        onPatchBuild();
        craw();
        check();
        saveViews();
        initIds();
        var bufferedTrees = getBufferedTrees();
        var difference = buildDifference(bufferedTrees.keySet());
        var entityChange = difference.getEntityChange();
        var refChange = difference.getReferenceChange();
        onContextInitializeId();
        if (prevPatch != null) {
            prevPatch.entityChange.getAttributes().forEach((key, value) -> {
                //noinspection rawtypes,unchecked
                entityChange.setAttribute((DifferenceAttributeKey) key, value);
            });
        }
        return processChanges(new Patch(bufferedTrees, entityChange, refChange), patchContext);
    }

    private void removeOrphans(Patch patch) {
        var orphans = new ArrayList<DurableInstance>();
        for (InstancePO delete : patch.entityChange.deletes()) {
            var instance = Objects.requireNonNull(getSelfBuffered(PhysicalId.of(delete.getId())));
            if (!instance.isRemoved())
                orphans.add(instance);
        }
        if (!orphans.isEmpty())
            batchRemove(orphans);
    }

    private void check() {
        forEachInitialized(i -> i.accept(new ForestChecker()));
    }

    @Override
    public List<DurableInstance> getByReferenceTargetId(long targetId, DurableInstance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.getByReferenceTargetId(
                        targetId,
                        NncUtils.getOrElse(startExclusive, DurableInstance::tryGetPhysicalId, -1L),
                        limit,
                        this
                ),
                id -> get(PhysicalId.of(id))
        );
    }


    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            var instancesToInitId =
                    NncUtils.filter(this, i -> !i.isIdInitialized() && !i.isEphemeral());
            if (instancesToInitId.isEmpty())
                return;
            var countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
            var type2ids = idProvider.allocate(appId, countMap);
            var type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
            var allocatedMap = new HashMap<Long, Instance>();
            type2instances.forEach((type, instances) -> {
                List<Long> ids = type2ids.get(type);
                for (Long id : ids) {
                    boolean contains1 = allocatedMap.containsKey(id);
                    if (contains1)
                        throw new InternalException();
                    boolean contains = containsId(id);
                    if (contains)
                        throw new InternalException();
                }
                for (var instance : instances) {
                    allocatedMap.put(instance.tryGetPhysicalId(), instance);
                }
                NncUtils.biForEach(instances, ids, (inst, id) -> inst.initId(PhysicalId.of(id)));
            });
            for (var instance : instancesToInitId) {
                onIdInitialized(instance);
            }
        }
    }

    @Override
    public List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit) {
        return getByType(type, startExclusive, limit, false);
    }

    private List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit, boolean persistedOnly) {
        List<InstancePO> instancePOs = loadByType(new LoadByTypeRequest(type, startExclusive, limit));
        var persistedResult = NncUtils.map(instancePOs, instancePO -> get(new PhysicalId(instancePO.getId())));
        if (persistedResult.size() >= limit || persistedOnly)
            return persistedResult;
        Set<Long> persistedIds = NncUtils.mapUnique(persistedResult, DurableInstance::tryGetPhysicalId);
        var result = NncUtils.union(
                persistedResult,
                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()), persistedIds)
        );
        return result;
    }

    private List<DurableInstance> getByTypeFromBuffer(Type type, @Nullable DurableInstance startExclusive, int limit, Set<Long> persistedIds) {
        var typeInstances = NncUtils.filter(
                this,
                i -> type == i.getType() && !persistedIds.contains(i.tryGetPhysicalId())
        );
        if (startExclusive == null)
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    private List<InstancePO> loadByType(LoadByTypeRequest request) {
        var cachedResult = loadByTypeCache.get(request);
        if (cachedResult != null)
            return cachedResult;
        var result = instanceStore.queryByTypeIds(
                List.of(
                        new ByTypeQuery(
                                NncUtils.requireNonNull(request.type().tryGetId(), "Type id is not initialized"),
                                request.startExclusive() == null ? -1L : request.startExclusive().getPhysicalId() + 1L,
                                request.limit()
                        )
                ),
                this
        );
        loadByTypeCache.put(request, result);
        return result;
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        if (NncUtils.anyMatch(this, i -> type.isInstance(i) && (!persistedOnly || i.isPersisted()))) {
            return true;
        }
        return type.tryGetId() != null && NncUtils.isNotEmpty(getByType(type, null, 1, persistedOnly));
    }

    @Override
    public List<DurableInstance> scan(DurableInstance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.scan(List.of(
                        new ScanQuery(startExclusive.isNull() ? 0L : startExclusive.getPhysicalId() + 1L, limit)
                ), this),
                instancePO -> get(new PhysicalId(instancePO.getId()))
        );
    }

    private static class ForestChecker extends VoidInstanceVisitor {

        final LinkedList<InstanceParentRef> ancestorRefs = new LinkedList<>();
        final IdentitySet<Instance> visited = new IdentitySet<>();

        @Override
        public Void visitDurableInstance(DurableInstance instance) {
            if (!visited.add(instance))
                throw new BusinessException(ErrorCode.MULTI_PARENT, instance);
            if (!ancestorRefs.isEmpty()) {
                var parentRef = ancestorRefs.peek();
                if (!Objects.equals(parentRef.parent(), instance.getParent()) ||
                        !Objects.equals(parentRef.field(), instance.getParentField())) {
                    throw new BusinessException(ErrorCode.INCORRECT_PARENT_REF,
                            instance, parentRef.parent(),
                            String.format("%s - %s", instance.getParent(), instance.getParentField())
                    );
                }
            }
            return null;
        }

        @Override
        public Void visitNullInstance(NullInstance instance) {
            return null;
        }

        @Override
        public Void visitClassInstance(ClassInstance instance) {
            if (instance.isInitialized()) {
                super.visitClassInstance(instance);
                instance.forEachField((field, value) -> {
                    if (field.isChild()) {
                        ancestorRefs.push(new InstanceParentRef(instance, field));
                        value.accept(this);
                        ancestorRefs.pop();
                    }
                });
            }
            return null;
        }

        @Override
        public Void visitArrayInstance(ArrayInstance instance) {
            if (instance.isInitialized()) {
                super.visitArrayInstance(instance);
                if (instance.isChildArray() && !instance.isEmpty()) {
                    ancestorRefs.push(new InstanceParentRef(instance, null));
                    for (Instance child : instance)
                        child.accept(this);
                    ancestorRefs.pop();
                }
            }
            return null;
        }
    }

    private void forEachInitialized(Consumer<DurableInstance> action) {
        for (var instance : this) {
            if (instance.isInitialized())
                action.accept(instance);
        }
    }

    private record Patch(IdentityHashMap<Tree, Instance> instancePOs,
                         EntityChange<InstancePO> entityChange,
                         EntityChange<ReferencePO> referenceChange) {
    }

    private Patch beforeSaving(Patch patch, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("beforeSaving")) {
            for (ContextPlugin plugin : plugins) {
                if (plugin.beforeSaving(patch.entityChange, this))
                    patch = buildPatch(patch, patchContext);
            }
        }
        return patch;
    }

    private void afterSaving(Patch patch) {
        try (var ignored = getProfiler().enter("afterSaving")) {
            plugins.forEach(plugin -> plugin.afterSaving(patch.entityChange, this));
        }
    }

    private ContextDifference buildDifference(Collection<Tree> bufferedTrees) {
        ContextDifference difference =
                new ContextDifference(appId, id -> internalGet(new PhysicalId(id)).getType().tryGetId());
        difference.diff(headContext.trees(), bufferedTrees);
        difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedTrees));
        computeVirtualUpdates(difference.getEntityChange());
        return difference;
    }

    private void computeVirtualUpdates(EntityChange<InstancePO> change) {
        var queue = new LinkedList<InstancePO>();
        queue.addAll(change.inserts());
        queue.addAll(change.updates());
        queue.addAll(change.deletes());
        var queuedIds = new HashSet<>(NncUtils.map(queue, InstancePO::getId));
        while (!queue.isEmpty()) {
            var instance = queue.poll();
            if (instance.getParentId() != -1L && queuedIds.add(instance.getParentId())) {
                var parent = internalGet(PhysicalId.of(instance.getParentId()));
                var parentPO = new InstancePO(
                        appId, parent.getPhysicalId(), null, parent.getType().getId(), null,
                        NncUtils.getOrElse(parent.getParent(), DurableInstance::getPhysicalId, -1L),
                        NncUtils.getOrElse(parent.getParentField(), Entity::getId, -1L),
                        parent.getRoot().getPhysicalId(), parent.getVersion() + 1,0L
                );
                queue.offer(parentPO);
                change.addVirtualUpdate(parentPO);
            }
        }
    }

    private Patch processChanges(Patch patch, PatchContext patchContext) {
        var ref = new Object() {
            boolean changed = false;
        };
        try (var ignored = getProfiler().enter("processChanges")) {
            patch.entityChange.forEachInsertOrUpdate(instancePO -> {
                if (patchContext.changeNotified.add(instancePO.getId())) {
                    if (onChange(internalGet(new PhysicalId(instancePO.getId()))))
                        ref.changed = true;
                }
            }, true);
        }
        return ref.changed ? buildPatch(patch, patchContext) : patch;
    }

    private void processRemoval(Patch patch) {
        try (var ignored = getProfiler().enter("processRemoval")) {
            removeOrphans(patch);
            var entityChange = patch.entityChange;
            if (NncUtils.isEmpty(entityChange.deletes()))
                return;
            var visitor = new GraphVisitor() {
                @Override
                public Void visitDurableInstance(DurableInstance instance) {
                    if (instance.isRemoved())
                        throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, instance);
                    return super.visitDurableInstance(instance);
                }
            };
            for (var instance : this) {
                if (!instance.isRemoved() && !instance.isEphemeral())
                    visitor.visit(instance);
            }
            Set<Long> idsToRemove = NncUtils.mapUnique(entityChange.deletes(), InstancePO::getId);
            Set<Long> idsToUpdate = NncUtils.mapUnique(entityChange.updates(), InstancePO::getId);
            ReferencePO ref = instanceStore.getFirstReference(
                    appId, idsToRemove, mergeSets(idsToRemove, idsToUpdate)
            );
            if (ref != null)
                throw BusinessException.strongReferencesPreventRemoval(get(new PhysicalId(ref.getSourceId())),
                        internalGet(new PhysicalId(ref.getTargetId())));
        }
    }

    private boolean isChildReference(ReferencePO referencePO) {
        long sourceId = referencePO.getSourceId();
        Instance source = isRemoved(sourceId) ? getRemoved(sourceId) : get(new PhysicalId(sourceId));
        if (source instanceof ArrayInstance array) {
            return array.isChildArray();
        } else {
            ClassType type = (ClassType) source.getType();
            var field = type.getField(referencePO.getFieldId());
            return field.isChild();
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public static final int MAX_ITERATION = 5;

    private IdentityHashMap<Tree, Instance> getBufferedTrees() {
        IdentityHashMap<Tree, Instance> trees = new IdentityHashMap<>();
        Set<Instance> processed = new IdentitySet<>();
        int it = 0;
        for (; ; ) {
            if (it++ >= MAX_ITERATION) {
                throw new InternalException(
                        String.format("getBufferedEntityPOs reached max number of iteration (%d)", MAX_ITERATION));
            }
            boolean added = false;
            for (var instance : this) {
                if (instance.isInitialized() && instance.isRoot()
                        && !instance.isRemoved() && !instance.isEphemeral()
                        && (instance.isNew() || !instance.isLoadedFromCache()) && !processed.contains(instance)) {
                    var tree = new Tree(
                            instance.getPhysicalId(),
                            instance.getVersion(),
                            InstanceOutput.toMessage(instance)
                    );
                    trees.put(tree, instance);
                    processed.add(instance);
                    added = true;
                }
            }
            if (!added)
                break;
        }
        return trees;
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

    private void postProcess() {
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

    private void saveReferences(EntityChange<ReferencePO> referenceChange) {
        try (var ignored = getProfiler().enter("saveReferences")) {
            instanceStore.saveReferences(referenceChange.toChangeList());
        }
    }

    public IInstanceStore getInstanceStore() {
        return instanceStore;
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
                idProvider,
                executor,
                asyncPostProcessing,
                plugins,
                getParent(),
                getTypeProvider(),
                getMappingProvider(),
                getParameterizedFlowProvider(),
                childrenLazyLoading,
                cache,
                eventQueue,
                isReadonly());
    }

}
