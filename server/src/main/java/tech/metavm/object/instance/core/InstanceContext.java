package tech.metavm.object.instance.core;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.application.Application;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.mergeSets;

public class InstanceContext extends BufferingInstanceContext {

    private final Map<LoadByTypeRequest, List<InstancePO>> loadByTypeCache = new HashMap<>();
    private boolean finished;
    private final SubContext headContext;
    private final boolean asyncPostProcessing;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private IEntityContext entityContext;

    private final TypeResolver typeResolver;
    private final boolean childrenLazyLoading;
    private final @Nullable EventQueue eventQueue;
    private final IInstanceStore instanceStore;
    private final Cache cache;

    public InstanceContext(long appId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           DefContext defContext,
                           IInstanceContext parent,
                           TypeResolver typeResolver,
                           boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly
    ) {
        super(appId,
                List.of(new CacheTreeSource(cache),
                        new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                idService,
                new StoreIndexSource(instanceStore)
                , defContext, parent, readonly);
        headContext = new SubContext(appId);
        this.asyncPostProcessing = asyncPostProcessing;
        this.plugins = plugins;
        this.executor = executor;
        this.childrenLazyLoading = childrenLazyLoading;
        this.typeResolver = typeResolver;
        this.eventQueue = eventQueue;
        this.instanceStore = instanceStore;
        entityContext = new EntityContext(
                this,
                NncUtils.get(parent, IInstanceContext::getEntityContext),
                defContext
        );
        this.cache = cache;
    }

    @Override
    protected void onReplace(List<Instance> replacements) {
        for (Instance replacement : replacements) {
            buffer(replacement.getIdRequired());
        }
        for (Instance replacement : replacements) {
            var tree = loadingBuffer.tryGetTree(replacement.getIdRequired());
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
        var patch = buildPatch(null);
        Set<Instance> orphans = getOrphans(patch);
        if (!orphans.isEmpty()) {
            batchRemove(orphans);
            patch = buildPatch(patch);
        }
        processUpdate(patch);
        processRemoval(patch.entityChange);
        patch = beforeSaving(patch);
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

    private void incPOVersions(ContextPatch patch) {
        patch.entityChange.forEachUpdateOrDelete(i -> i.setVersion(i.getVersion() + 1));
    }

    private void incVersions(ContextPatch patch) {
        patch.entityChange.forEachUpdateOrDelete(i -> internalGet(i.getId()).incVersion());
    }

    private ContextPatch buildPatch(@Nullable ContextPatch prevPatch) {
        craw();
        check();
        initIds();
        var bufferedTrees = getBufferedTrees();
        var difference = buildDifference(bufferedTrees.keySet());
        var entityChange = difference.getEntityChange();
        var refChange = difference.getReferenceChange();
        if (prevPatch == null)
            entityContext.afterContextIntIds();
        else {
            prevPatch.entityChange.getAttributes().forEach((key, value) -> {
                //noinspection rawtypes,unchecked
                entityChange.setAttribute((DifferenceAttributeKey) key, value);
            });
        }
        return new ContextPatch(bufferedTrees, entityChange, refChange);
    }

    private void check() {
        forEachInitialized(i -> i.accept(new ForestChecker()));
    }

    @Override
    public List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.getByReferenceTargetId(
                        targetId,
                        NncUtils.getOrElse(startExclusive, Instance::getId, -1L),
                        limit,
                        this
                ),
                this::get
        );
    }

    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
            List<Instance> instancesToInitId = NncUtils.filter(this, Instance::isIdNull);
            if (instancesToInitId.isEmpty()) {
                return;
            }
            Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
            Map<Type, List<Long>> idMap = idGenerator.apply(countMap);
            Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
            Map<Long, Instance> allocatedMap = new HashMap<>();
            type2instances.forEach((type, instances) -> {
                List<Long> ids = idMap.get(type);
                for (Long id : ids) {
                    boolean contains1 = allocatedMap.containsKey(id);
                    if (contains1) {
                        throw new InternalException();
                    }
                    boolean contains = containsId(id);
                    if (contains) {
                        throw new InternalException();
                    }
                }
                for (Instance instance : instances) {
                    allocatedMap.put(instance.getId(), instance);
                }
                NncUtils.biForEach(instances, ids, Instance::initId);
            });
            for (Instance instance : instancesToInitId) {
                onIdInitialized(instance);
            }
        }
    }

    @Override
    protected long getTypeId(long id) {
        return id == Constants.ROOT_APP_ID || id == Constants.PLATFORM_APP_ID ?
                getDefContext().getType(Application.class).getIdRequired() : super.getTypeId(id);
    }

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idProvider.allocate(appId, typeId2count);
    }

    private List<InstancePO> loadByType(LoadByTypeRequest request) {
        var cachedResult = loadByTypeCache.get(request);
        if (cachedResult != null)
            return cachedResult;
        var result = instanceStore.queryByTypeIds(
                List.of(
                        new ByTypeQuery(
                                NncUtils.requireNonNull(request.type().getId(), "Type id is not initialized"),
                                request.startExclusive().isNull() ? -1L : request.startExclusive().getIdRequired() + 1L,
                                request.limit()
                        )
                ),
                this
        );
        loadByTypeCache.put(request, result);
        return result;
    }

    private List<Instance> getByType(Type type, Instance startExclusive, long limit, boolean persistedOnly) {
        if (startExclusive == null) {
            startExclusive = InstanceUtils.nullInstance();
        }
        List<InstancePO> instancePOs = loadByType(new LoadByTypeRequest(type, startExclusive, limit));
        List<Instance> persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getId()));
        if (persistedResult.size() >= limit || persistedOnly) {
            return persistedResult;
        }
        Set<Long> persistedIds = NncUtils.mapUnique(persistedResult, Instance::getId);
        var result = NncUtils.union(
                persistedResult,
                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()), persistedIds)
        );
        return result;
    }

    @Override
    public List<Instance> getByType(Type type, Instance startExclusive, long limit) {
        return getByType(type, startExclusive, limit, false);
    }

    private List<Instance> getByTypeFromBuffer(Type type, Instance startExclusive, int limit, Set<Long> persistedIds) {
        List<Instance> typeInstances = NncUtils.filter(
                this,
                i -> type == i.getType() && !persistedIds.contains(i.getId())
        );
        if (startExclusive.isNull()) {
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        }
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        if (NncUtils.anyMatch(this, i -> type.isInstance(i) && (!persistedOnly || i.isPersisted()))) {
            return true;
        }
        return type.getId() != null && NncUtils.isNotEmpty(getByType(type, null, 1, persistedOnly));
    }

    @Override
    public List<Instance> scan(Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.scan(List.of(
                        new ScanQuery(startExclusive.isNull() ? 0L : startExclusive.getIdRequired() + 1L, limit)
                ), this),
                instancePO -> get(instancePO.getId())
        );
    }

    private static class ForestChecker extends InstanceVisitor {

        final LinkedList<InstanceParentRef> ancestorRefs = new LinkedList<>();
        final IdentitySet<Instance> visited = new IdentitySet<>();

        @Override
        public void visitInstance(Instance instance) {
            super.visitInstance(instance);
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
        }

        @Override
        public void visitNullInstance(NullInstance instance) {
        }

        @Override
        public void visitClassInstance(ClassInstance instance) {
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
        }

        @Override
        public void visitArrayInstance(ArrayInstance instance) {
            if (instance.isInitialized()) {
                super.visitArrayInstance(instance);
                if (instance.isChildArray() && !instance.isEmpty()) {
                    ancestorRefs.push(new InstanceParentRef(instance, null));
                    for (Instance child : instance)
                        child.accept(this);
                    ancestorRefs.pop();
                }
            }
        }
    }

    private void forEachInitialized(Consumer<Instance> action) {
        for (Instance instance : this) {
            if (instance.isInitialized())
                action.accept(instance);
        }
    }

    private record ContextPatch(IdentityHashMap<Tree, Instance> instancePOs,
                                EntityChange<InstancePO> entityChange,
                                EntityChange<ReferencePO> referenceChange) {
    }

    private ContextPatch beforeSaving(ContextPatch patch) {
        try (var ignored = getProfiler().enter("beforeSaving")) {
            for (ContextPlugin plugin : plugins) {
                if (plugin.beforeSaving(patch.entityChange, this)) {
                    patch = buildPatch(patch);
                }
            }
        }
        return patch;
    }

    private void afterSaving(ContextPatch patch) {
        try (var ignored = getProfiler().enter("afterSaving")) {
            plugins.forEach(plugin -> plugin.afterSaving(patch.entityChange, this));
        }
    }

    private ContextDifference buildDifference(Collection<Tree> bufferedTrees) {
        ContextDifference difference =
                new ContextDifference(appId, id -> internalGet(id).getType().getIdRequired());
        difference.diff(headContext.trees(), bufferedTrees);
        difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedTrees));
        return difference;
    }

    private void processUpdate(ContextPatch patch) {
        try (var ignored = getProfiler().enter("processUpdate")) {
            List<InstancePO> insertOrUpdate = NncUtils.union(patch.entityChange.inserts(), patch.entityChange.updates());
            for (InstancePO instancePO : insertOrUpdate) {
                Instance instance = internalGet(instancePO.getId());
                if (instance instanceof ClassInstance classInstance) {
                    var model = entityContext.getEntity(Object.class, instance);
                    if (model instanceof UpdateAware updateAware) {
                        updateAware.onUpdate(classInstance);
                    }
                    if (model instanceof Entity entity) {
                        entity.validate();
                    }
                }
            }
        }
    }

    private void processRemoval(EntityChange<InstancePO> entityChange) {
        try (var ignored = getProfiler().enter("processRemoval")) {
            if (NncUtils.isEmpty(entityChange.deletes())) {
                return;
            }
            Set<Long> idsToRemove = NncUtils.mapUnique(entityChange.deletes(), InstancePO::getId);
            Set<Long> idsToUpdate = NncUtils.mapUnique(entityChange.updates(), InstancePO::getId);
            ReferencePO ref = instanceStore.getFirstReference(
                    appId, idsToRemove, mergeSets(idsToRemove, idsToUpdate)
            );
            if (ref != null) {
                throw BusinessException.strongReferencesPreventRemoval(get(ref.getSourceId()), internalGet(ref.getTargetId()));
            }
        }
    }

    private Set<Instance> getOrphans(ContextPatch patch) {
        Set<Long> removedIds = NncUtils.mapUnique(patch.entityChange().deletes(), InstancePO::getId);
        Set<Instance> orphans = new HashSet<>();
        for (ReferencePO removedRef : patch.referenceChange().deletes()) {
            if (isChildReference(removedRef) && !removedIds.contains(removedRef.getTargetId()))
                orphans.add(get(removedRef.getTargetId()));
        }
        return orphans;
    }

    private boolean isChildReference(ReferencePO referencePO) {
        long sourceId = referencePO.getSourceId();
        Instance source = isRemoved(sourceId) ? getRemoved(sourceId) : get(sourceId);
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
            for (Instance instance : this) {
                if (instance.isInitialized() && instance.isRoot() && !instance.isRemoved()
                        && (instance.isNew() || !instance.isLoadedFromCache()) && !processed.contains(instance)) {
                    var tree = new Tree(
                            instance.getIdRequired(),
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

    public IEntityContext getEntityContext() {
        return entityContext;
    }

    @Override
    public Type getType(long id) {
        return typeResolver.getType(this, id);
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

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
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
                getDefContext(),
                getParent(),
                typeResolver,
                childrenLazyLoading,
                cache,
                eventQueue,
                isReadonly()
        );
    }

}
