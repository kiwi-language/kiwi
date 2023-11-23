package tech.metavm.object.instance.core;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static tech.metavm.util.NncUtils.mergeSets;

public class InstanceContext extends BaseInstanceContext {

    private boolean finished;
    private final SubContext headContext;
    private final boolean asyncPostProcessing;
    private final LoadingBuffer loadingBuffer;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private IEntityContext entityContext;
    private final TypeResolver typeResolver;
    private final boolean childrenLazyLoading;
    private final Cache cache;

    public InstanceContext(long tenantId,
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
                           boolean readonly
    ) {
        super(tenantId, idService, instanceStore, defContext, parent, readonly);
        headContext = new SubContext(tenantId);
        this.asyncPostProcessing = asyncPostProcessing;
        this.plugins = plugins;
        this.executor = executor;
        this.childrenLazyLoading = childrenLazyLoading;
        this.typeResolver = typeResolver;
        loadingBuffer = new LoadingBuffer(this, cache);
        entityContext = new EntityContext(
                this,
                NncUtils.get(parent, IInstanceContext::getEntityContext),
                defContext
        );
        this.cache = cache;
//        setBindHook(job -> getEntityContext().bind(job));
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
    public void buffer(long id) {
        if(parent != null && parent.containsId(id))
            parent.buffer(id);
        else
            loadingBuffer.buffer(id);
    }

    @Override
    protected Instance allocateInstance(long id) {
        Type type = getType(idService.getTypeId(id));
        if (type instanceof ArrayType arrayType) {
            return new ArrayInstance(id, arrayType, this::initializeInstance);
        } else {
            return new ClassInstance(id, (ClassType) type, this::initializeInstance);
        }
    }

    @Override
    protected boolean checkAliveInStore(long id) {
        return loadingBuffer.tryGetTree(id) != null;
    }

    private void initializeInstance(Instance instance) {
        var tree = loadingBuffer.getTree(instance.getIdRequired());
        headContext.add(tree);
        var input = new InstanceInput(new ByteArrayInputStream(tree.data()), this::internalGet);
        readInstance(input);
    }

    private Instance readInstance(InstanceInput input) {
        try(var entry = getProfiler().enter("readInstance")) {
            Instance instance = input.readMessage();
            entry.addMessage("id", instance.getIdRequired());
            onInstanceInitialized(instance);
            return instance;
        }
    }

    @Override
    public void invalidateCache(Instance instance) {
        if (cache != null) {
            cache.remove(instance.getIdRequired());
        }
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
                new ContextDifference(tenantId, id -> internalGet(id).getType().getIdRequired());
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
                    tenantId, idsToRemove, mergeSets(idsToRemove, idsToUpdate)
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
            new ReferenceExtractor(tree.openInput(), tenantId, references::add).visitMessage();
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

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public IInstanceContext createSame(long tenantId) {
        return new InstanceContext(
                tenantId,
                instanceStore,
                idService,
                executor,
                asyncPostProcessing,
                plugins,
                getDefContext(),
                getParent(),
                typeResolver,
                childrenLazyLoading,
                cache,
                isReadonly()
        );
    }

}
