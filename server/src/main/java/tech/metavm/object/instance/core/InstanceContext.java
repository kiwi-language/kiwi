package tech.metavm.object.instance.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.StoreTreeSource;
import tech.metavm.object.instance.StoreVersionSource;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.VersionRT;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
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
                           IdInitializer idInitializer,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeProvider typeProvider,
                           MappingProvider mappingProvider,
                           ParameterizedFlowProvider parameterizedFlowProvider,
                           CompositeTypeFacade compositeTypeFacade, boolean childrenLazyLoading,
                           Cache cache,
                           @Nullable EventQueue eventQueue,
                           boolean readonly
    ) {
        super(appId,
                List.of(/*new CacheTreeSource(cache),*/new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                new StoreIndexSource(instanceStore), idInitializer,
                parent, typeProvider, mappingProvider, parameterizedFlowProvider, compositeTypeFacade, readonly);
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
            buffer(Objects.requireNonNull(replacement.tryGetId()));
        }
        for (var replacement : replacements) {
            var tree = loadingBuffer.tryGetTree(replacement.tryGetId());
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

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    @Override
    protected void finishInternal() {
        if (finished)
            throw new IllegalStateException("Already finished");
        if (DebugEnv.DEBUG_ON)
            DEBUG_LOGGER.info("InstanceContext.finish");
        checkBug("start");
        var patchContext = new PatchContext();
        var patch = buildPatch(null, patchContext);
        checkBug("after patch build");
        checkRemoval();
        processRemoval(patch);
        checkBug("after processRemoval");
        patch = beforeSaving(patch, patchContext);
        checkBug("after beforeSaving");
        saveInstances(patch.treeChanges);
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

    private void checkBug(String position) {
        var i1 = NncUtils.find(this, i -> i.isInitialized() && i.toString().equals("Class类型-Node<MyList_T>"));
        if (i1 != null) {
            var t1 = (ClassType) Objects.requireNonNull(i1.getMappedEntity());
            DebugEnv.checkBug("InstanceContext." + position, t1);
        }
    }

    private static class PatchContext {

        public static final int MAX_BUILD = 10;

        int numBuild;
        final Set<Id> changeNotified = new HashSet<>();

        void incBuild() {
            if (++numBuild > MAX_BUILD)
                throw new InternalException("Too many patch build");
        }
    }

    private Patch buildPatch(@Nullable Patch prevPatch, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("buildPatch")) {
            if (DebugEnv.DEBUG_ON)
                DEBUG_LOGGER.info("building patch. numBuild: {}", patchContext.numBuild);
            patchContext.incBuild();
            onPatchBuild();
            saveViews();
            craw();
            check();
            initIds();
            var bufferedTrees = getBufferedTrees();
            var difference = buildDifference(bufferedTrees.keySet());
            var entityChange = difference.getEntityChange();
            var refChange = difference.getReferenceChange();
            var treeChanges = difference.getTreeChanges();
            onContextInitializeId();
            if (prevPatch != null) {
                prevPatch.entityChange.getAttributes().forEach((key, value) -> {
                    //noinspection rawtypes,unchecked
                    entityChange.setAttribute((DifferenceAttributeKey) key, value);
                });
            }
            return processChanges(new Patch(bufferedTrees, entityChange, treeChanges, refChange), computeNonPersistedOrphans(), patchContext);
        }
    }

    private boolean removeOrphans(Patch patch, List<DurableInstance> nonPersistedOrphans) {
        var orphans = new ArrayList<DurableInstance>();
        for (var version : patch.entityChange.deletes()) {
            var instance = Objects.requireNonNull(getSelfBuffered(version.id()));
            if (!instance.isRemoved())
                orphans.add(instance);
        }
        for (DurableInstance instance : nonPersistedOrphans) {
            if (!instance.isRemoved())
                orphans.add(instance);
        }
        if (!orphans.isEmpty()) {
            batchRemove(orphans);
            return true;
        } else
            return false;
    }

    private void check() {
        try (var ignored = getProfiler().enter("check")) {
            forEachInitialized(i -> i.accept(new ForestChecker()));
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

//    @Override
//    public List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit) {
//        return getByType(type, startExclusive, limit, false);
//    }

//    private List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit, boolean persistedOnly) {
//        List<InstancePO> instancePOs = loadByType(new LoadByTypeRequest(type, startExclusive, limit));
//        var persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getInstanceId()));
//        if (persistedResult.size() >= limit || persistedOnly)
//            return persistedResult;
//        Set<Long> persistedIds = NncUtils.mapUnique(persistedResult, DurableInstance::tryGetPhysicalId);
//        var result = NncUtils.union(
//                persistedResult,
//                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()), persistedIds)
//        );
//        return result;
//    }

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

//    private List<InstancePO> loadByType(LoadByTypeRequest request) {
//        var cachedResult = loadByTypeCache.get(request);
//        if (cachedResult != null)
//            return cachedResult;
//        var result = instanceStore.queryByTypeIds(
//                List.of(
//                        new ByTypeQuery(
//                                request.type().getId(),
//                                request.startExclusive() == null ? null : request.startExclusive().getId(),
//                                request.limit()
//                        )
//                ),
//                this
//        );
//        loadByTypeCache.put(request, result);
//        return result;
//    }

//    @Override
//    public boolean existsInstances(Type type, boolean persistedOnly) {
//        if (NncUtils.anyMatch(this, i -> type.isInstance(i) && (!persistedOnly || i.isPersisted()))) {
//            return true;
//        }
//        return type.tryGetPhysicalId() != null && NncUtils.isNotEmpty(getByType(type, null, 1, persistedOnly));
//    }

//    @Override
//    public List<DurableInstance> scan(DurableInstance startExclusive, long limit) {
//        return NncUtils.map(
//                instanceStore.scan(List.of(
//                        new ScanQuery(startExclusive == null ? NullId.BYTES : startExclusive.getId().toBytes(), limit)
//                ), this),
//                instancePO -> get(instancePO.getInstanceId())
//        );
//    }

    private static class ForestChecker extends VoidInstanceVisitor {

        final LinkedList<InstanceParentRef> ancestorRefs = new LinkedList<>();
        final IdentitySet<Instance> visited = new IdentitySet<>();
        final Map<Instance, String> pathMap = new IdentityHashMap<>();

        @Override
        public Void visitDurableInstance(DurableInstance instance) {
            if (DebugEnv.DEBUG_ON) {
                var refs = new LinkedList<InstanceParentRef>();
                ancestorRefs.forEach(refs::addFirst);
                String path = NncUtils.join(
                        refs,
                        r -> r.field() != null ?
                                Instances.getInstanceDesc(r.parent()) + "." + r.field().getName()
                                : Instances.getInstanceDesc(r.parent()),
                        "->"
                );
                var existingPath = pathMap.put(instance, path);
                if (existingPath != null) {
                    DEBUG_LOGGER.info("Duplicate path detected. {} and {}", existingPath, path);
                }
            }
            if (!visited.add(instance)) {
                throw new BusinessException(ErrorCode.MULTI_PARENT, Instances.getInstanceDesc(instance));
            }
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
        try (var ignored = getProfiler().enter("buildDifference")) {
            ContextDifference difference =
                    new ContextDifference(appId, id -> internalGet(id).getType().getId());
            difference.diff(headContext.trees(), bufferedTrees);
            difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedTrees));
            return difference;
        }
    }

    private Patch processChanges(Patch patch, List<DurableInstance> nonPersistedOrphans, PatchContext patchContext) {
        try (var ignored = getProfiler().enter("processChanges")) {
            if (DebugEnv.DEBUG_ON) {
                DEBUG_LOGGER.info("nonPersistedOrphans: [{}]", NncUtils.join(nonPersistedOrphans, i -> Instances.getInstanceDesc(i) + "/" + i.getStringId()));
            }
            var ref = new Object() {
                boolean changed = false;
            };
            if (removeOrphans(patch, nonPersistedOrphans))
                ref.changed = true;
            patch.entityChange.forEachInsertOrUpdate(v -> {
                if (patchContext.changeNotified.add(v.id())) {
                    if (onChange(internalGet(v.id()))) {
                        if (DebugEnv.DEBUG_ON && !ref.changed)
                            DEBUG_LOGGER.info("insert/update change detected {}, numBuilds: {}", Instances.getInstancePath(internalGet(v.id())), patchContext.numBuild);
                        ref.changed = true;
                    }
                }
            }, true);
            patch.entityChange.deletes().forEach(v -> {
                if (patchContext.changeNotified.add(v.id())) {
                    if (onRemove(internalGet(v.id()))) {
                        if (DebugEnv.DEBUG_ON && !ref.changed)
                            DEBUG_LOGGER.info("removal change detected {}, numBuilds: {}", Instances.getInstancePath(internalGet(v.id())), patchContext.numBuild);
                        ref.changed = true;
                    }
                }
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
            i = i.getParent();
        }
        return NncUtils.join(path, this::getInstanceDesc, "/");
    }

    private void checkRemoval() {
        var visitor = new GraphVisitor() {

            private final LinkedList<String> path = new LinkedList<>();

            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                if (DebugEnv.DEBUG_ON)
                    path.addLast(getInstancePath(instance));
                if (instance.isRemoved()) {
                    if (DebugEnv.DEBUG_ON)
                        DEBUG_LOGGER.info(String.join("->", path));
                    throw new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, Instances.getInstanceDesc(instance));
                }
                if (DebugEnv.DEBUG_ON) {
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
            var idsToUpdate = NncUtils.mapUnique(entityChange.updates(), v -> v.id().getPhysicalId());
            var ref = instanceStore.getFirstReference(
                    appId, idsToRemove, mergeSets(NncUtils.mapUnique(idsToRemove, Id::getPhysicalId), idsToUpdate)
            );
            if (ref != null)
                throw BusinessException.strongReferencesPreventRemoval(getRoot(ref.getSourceTreeId()),
                        internalGet(ref.getTargetInstanceId()));
        }
    }

//    private boolean isChildReference(ReferencePO referencePO) {
//        long sourceId = referencePO.getSourceId();
//        Instance source = isRemoved(sourceId) ? getRemoved(sourceId) : get(new PhysicalId(sourceId));
//        if (source instanceof ArrayInstance array) {
//            return array.isChildArray();
//        } else {
//            ClassType type = (ClassType) source.getType();
//            var field = type.getField(referencePO.getFieldId());
//            return field.isChild();
//        }
//    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public static final int MAX_ITERATION = 5;

    private IdentityHashMap<Tree, Instance> getBufferedTrees() {
        try (var ignored = getProfiler().enter("getBufferedTrees")) {
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
                                instance.getNextNodeId(),
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
                if (DebugEnv.DEBUG_ON) {
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
                        DEBUG_LOGGER.info("field: {}, root: {}, log: {}", EntityUtils.getEntityDesc(field), EntityUtils.getEntityDesc(root), log);
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
                getTypeProvider(),
                getMappingProvider(),
                parameterizedFlowProvider(),
                compositeTypeFacade(),
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
