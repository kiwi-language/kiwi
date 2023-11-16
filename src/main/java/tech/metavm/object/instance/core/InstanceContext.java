package tech.metavm.object.instance.core;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.mergeSets;

public class InstanceContext extends BaseInstanceContext {

    private boolean finished;
    private final SubContext headContext = new SubContext();
    private final boolean asyncPostProcessing;

    private final LoadingBuffer loadingBuffer;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private IEntityContext entityContext;
    private final TypeResolver typeResolver;

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           long profileLogThreshold
    ) {
        this(tenantId, instanceStore, idService, executor, asyncPostProcessing, plugins, parent,
                new DefaultTypeResolver(),
                profileLogThreshold
        );
    }

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeResolver typeResolver,
                           long profileLogThreshold
    ) {
        this(tenantId, instanceStore, idService, executor, asyncPostProcessing, plugins,
                ModelDefRegistry.getDefContext(),
                parent, typeResolver, profileLogThreshold);
    }

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           DefContext defContext,
                           IInstanceContext parent,
                           TypeResolver typeResolver,
                           long profileLogThreshold
    ) {
        super(tenantId, idService, instanceStore, defContext, parent, profileLogThreshold);
        this.asyncPostProcessing = asyncPostProcessing;
        this.plugins = plugins;
        this.executor = executor;
        this.typeResolver = typeResolver;
        loadingBuffer = new LoadingBuffer(this);
        entityContext = new EntityContext(
                this,
                NncUtils.get(parent, IInstanceContext::getEntityContext),
                defContext
        );
        setBindHook(job -> getEntityContext().bind(job));
    }

    @Override
    protected void onReplace(List<Instance> replacements) {
        for (Instance replacement : replacements) {
            preload(replacement.getId());
        }
        for (Instance replacement : replacements) {
            InstancePO existingPO = loadingBuffer.getInstancePO(replacement.getId());
            if (existingPO != null) {
                headContext.add(existingPO, replacement.getType());
            }
        }
    }

    @Override
    public void preload(Collection<Long> ids, LoadingOption... options) {
        for (Long id : ids) {
            preload(id, options);
        }
    }

    public void preload(long id, LoadingOption... options) {
        loadingBuffer.load(new LoadRequest(id, LoadingOption.of(options)));
    }

    private <I extends Instance> I constructInstance(Class<I> klass, long id) {
        long typeId = idService.getTypeId(id);
        Type type = getType(typeId);
        Class<?> typeClass = EntityUtils.getEntityType(type.getClass());
//        if (typeClass == EnumType.class) {
//            typeClass = ClassType.class;
//        }
        Constructor<I> constructor = ReflectUtils.getConstructor(klass, typeClass);
        return ReflectUtils.invokeConstructor(constructor, type);
    }

    @Override
    protected Instance createInstance(long id) {
        preload(id);
        Type type = getType(idService.getTypeId(id));
        return EntityProxyFactory.getProxy(
                getInstanceJavaType(type),
                id,
                klass -> constructInstance(klass, id),
                this::initializeInstance
        );
    }

    private Class<? extends Instance> getInstanceJavaType(Type type) {
        if (type instanceof ArrayType) {
            return ArrayInstance.class;
        }
        if (type instanceof PrimitiveType) {
            return PrimitiveInstance.class;
        }
        if (type instanceof ClassType) {
            return ClassInstance.class;
        }
        throw new InternalException("Can not resolve instance type for type " + type.getName());
    }

    private void initializeInstance(Instance instance) {
        try (var entry = getProfiler().enter("initializeInstance")) {
            entry.addMessage("id", instance.getIdRequired());
            loadForest(List.of(instance), 0);
        }

//        preload(instance.getIdRequired());
//        List<InstancePO> loaded = NncUtils.exclude(
//                loadingBuffer.flush(), i -> instance.idEquals(i.getIdRequired()));
//        InstancePO instancePO = loadingBuffer.getInstancePO(instance.getIdRequired());
//        if (instancePO == null)
//            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, instance.getId());
//        initializeInstance(instance, instancePO);
//        loadForest(List.of(instance));
//        batchInitialize(loaded);
    }

    private void batchInitialize(List<InstancePO> instancePOs) {
        for (InstancePO instancePO : instancePOs) {
            var instance = get(instancePO.getIdRequired());
            if (EntityUtils.isModelUninitialized(instance)) {
                initializeInstance(instance, instancePO);
            } else
                LOGGER.warn(String.format("Instance %s/%d is already initialized, state: %s",
                        instance.getType().getName(), instance.getIdRequired(),
                        EntityUtils.getProxyState(instance)));
        }
    }

//    private final IdentitySet<Instance> initializing = new IdentitySet<>();

    private void initializeInstance(Instance instance, InstancePO instancePO) {
        headContext.add(EntityUtils.copyPojo(instancePO), instance.getType());
        clearStaleReferences(instancePO);
        if (instancePO.getParentId() != null) {
            var parent = get(instancePO.getParentId());
            Field parentField = null;
            if (instancePO.getParentFieldId() != null) {
                var parentType = ((ClassInstance) parent).getType();
                parentField = parentType.getField(instancePO.getParentFieldId());
            }
            instance.reloadParent(new InstanceParentRef(parent, parentField));
        }
        if (instance instanceof ArrayInstance arrayInstance) {
            InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
            List<Instance> elements = NncUtils.map(
                    arrayPO.getElements(),
                    e -> resolveColumnValue(Types.getElementType(arrayInstance.getType()), e)
            );
            arrayInstance.reload(elements);
        } else if (instance instanceof ClassInstance classInstance) {
            classInstance.reload(
                    getInstanceFields(instancePO, classInstance.getType()),
                    instancePO.getVersion(), instancePO.getSyncVersion()
            );
        }
        EntityUtils.setProxyState(instance, EntityMethodHandler.State.INITIALIZED);
        onInstanceInitialized(instance);
//        for (Instance child : instance.getChildren()) {
//            if (!EntityUtils.isModelInitialized(child))
//                preload(child.getIdRequired());
//        }
    }

    private void loadForest(List<Instance> instances, int depth) {
        if (instances.isEmpty())
            return;
        try (var entry = getProfiler().enter("loadForest", true)) {
            entry.addMessage("depth", depth);
            List<Instance> descendants = new ArrayList<>();
            var visitor = new StructuralVisitor() {
                @Override
                public void visitInstance(Instance instance) {
                    if (EntityUtils.isModelInitialized(instance))
                        super.visitInstance(instance);
                    else {
                        preload(instance.getIdRequired());
                        descendants.add(instance);
                    }
                }

                @Override
                public void visitClassInstance(ClassInstance instance) {
                    if (EntityUtils.isModelInitialized(instance)) {
                        instance.forEachField((field, value) -> {
                            if (field.isChildField() && !field.isLazy())
                                value.accept(this);
                        });
                    } else {
                        preload(instance.getIdRequired());
                        descendants.add(instance);
                    }
                }
            };
            for (Instance instance : instances) {
                visitor.visit(instance);
            }
            for (Instance descendant : descendants) {
                initializeInstance(descendant, loadingBuffer.getInstancePO(descendant.getIdRequired()));
            }
            loadForest(descendants, depth + 1);
        }
    }

    private Map<Field, Instance> getInstanceFields(InstancePO instancePO, ClassType type) {
        Map<Field, Instance> data = new HashMap<>();
        for (Field field : type.getAllFields()) {
            data.put(field, resolveColumnValue(field.getType(), instancePO.get(
                    field.getDeclaringType().getIdRequired(), field.getColumnName()))
            );
        }
        return data;
    }

    private Instance resolveColumnValue(Type fieldType, Object columnValue) {
        if (columnValue == null) {
            return InstanceUtils.nullInstance();
        } else if (columnValue instanceof IdentityPO identityPO) {
            return get(identityPO.id());
        } else if (fieldType.isReference()) {
            return get(ValueUtil.getLong(columnValue));
        } else if (columnValue instanceof InstancePO instancePO) {
            Class<? extends Instance> instanceType =
                    instancePO instanceof InstanceArrayPO ? ArrayInstance.class : ClassInstance.class;
            Type type = getType(instancePO.getTypeId());
            Instance instance = InstanceFactory.allocate(instanceType, type);
            initializeInstance(instance, instancePO);
            return instance;
        } else {
            return InstanceUtils.resolvePersistedPrimitive(columnValue, getDefContext()::getType);
        }
    }

    @Override
    protected void finishInternal() {
        if (finished) {
            throw new IllegalStateException("Already finished");
        }
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
        for (InstancePO instancePO : patch.instancePOs.keySet()) {
            headContext.add(EntityUtils.copyPojo(instancePO), getType(instancePO.getTypeId()));
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            registerTransactionSynchronization();
        } else {
            try (var ignored = getProfiler().enter("postProcess")) {
                postProcess();
            }
        }
        finished = true;
    }

    private ContextPatch buildPatch(@Nullable ContextPatch prevPatch) {
        rebind();
        initIds();
        var instancePOs = getBufferedInstancePOs();
        var difference = buildDifference(instancePOs.keySet());
        var entityChange = difference.getEntityChange();
        var refChange = difference.getReferenceChange();
        if(prevPatch == null)
            entityContext.afterContextIntIds();
        else {
            prevPatch.entityChange.getAttributes().forEach((key, value) -> {
                //noinspection rawtypes,unchecked
                entityChange.setAttribute((DifferenceAttributeKey) key, value);
            });
        }
        return new ContextPatch(instancePOs, entityChange, refChange);
    }

    private record ContextPatch(IdentityHashMap<InstancePO, Instance> instancePOs,
                                EntityChange<InstancePO> entityChange,
                                EntityChange<ReferencePO> referenceChange) {}

    private ContextPatch beforeSaving(ContextPatch patch) {
        try(var ignored = getProfiler().enter("beforeSaving")) {
            for (ContextPlugin plugin : plugins) {
                if(plugin.beforeSaving(patch.entityChange, this)) {
                    patch = buildPatch(patch);
                }
            }
        }
        return patch;
    }

    private void afterSaving(ContextPatch patch) {
        try(var ignored = getProfiler().enter("afterSaving")) {
            plugins.forEach(plugin -> plugin.afterSaving(patch.entityChange, this));
        }
    }

    private Set<Instance> getInstancesToPersist() {
        Set<Instance> visited = new IdentitySet<>(), result = new IdentitySet<>();
        for (Instance instance : instances) {
            if (!isNewInstance(instance) || !instance.getType().isEphemeral()) {
                dfs(instance, visited, result);
            }
        }
        return result;
    }

    private void dfs(Instance instance, Set<Instance> visited, Set<Instance> result) {
        if (visited.contains(instance) || !instances.contains(instance)) {
            return;
        }
        visited.add(instance);
        result.add(instance);
        for (ReferenceRT ref : instance.getOutgoingReferences()) {
            dfs(ref.target(), visited, result);
        }
    }

    private ContextDifference buildDifference(Collection<InstancePO> bufferedPOs) {
        ContextDifference difference = new ContextDifference();
        difference.diff(headContext.getEntities(), bufferedPOs);
        difference.diffReferences(headContext.getReferences(), getBufferedReferences(bufferedPOs));
        return difference;
    }

    private void processUpdate(ContextPatch patch) {
        try (var ignored = getProfiler().enter("processUpdate")) {
            List<InstancePO> insertOrUpdate = NncUtils.union(patch.entityChange.inserts(), patch.entityChange.updates());
            for (InstancePO instancePO : insertOrUpdate) {
                Instance instance = patch.instancePOs.get(instancePO);
                if (instance instanceof ClassInstance classInstance) {
                    var model = entityContext.getModel(Object.class, instance);
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
            List<ReferencePO> references = instanceStore.getFirstStrongReferences(
                    tenantId, idsToRemove, mergeSets(idsToRemove, idsToUpdate)
            );
            if (NncUtils.isNotEmpty(references)) {
                Map<Instance, Instance> refMap = new HashMap<>();
                for (ReferencePO reference : references) {
                    refMap.put(get(reference.getSourceId()), getRemoved(reference.getTargetId()));
                }
                throw BusinessException.strongReferencesPreventRemoval(refMap);
            }
        }
    }

    private Set<Instance> getOrphans(ContextPatch patch) {
        Set<Long> removed = NncUtils.mapUnique(patch.entityChange().deletes(), InstancePO::getId);
        Set<Instance> orphans = new HashSet<>();
        for (ReferencePO removedRef : patch.referenceChange().deletes()) {
            if (isChildReference(removedRef) && !removed.contains(removedRef.getTargetId())) {
                orphans.add(get(removedRef.getTargetId()));
            }
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
            return field.isChildField();
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public static final int MAX_ITERATION = 5;

    private IdentityHashMap<InstancePO, Instance> getBufferedInstancePOs() {
        IdentityHashMap<InstancePO, Instance> instancePOs = new IdentityHashMap<>();
        Set<Instance> processed = new IdentitySet<>();
        int it = 0;
        for (; ; ) {
            if (it++ >= MAX_ITERATION) {
                throw new InternalException("getBufferedEntityPOs reached max number of iteration " +
                        "(" + MAX_ITERATION + ")");
            }
            boolean added = false;
            for (Instance instance : new ArrayList<>(instances)) {
                if (InstanceUtils.isInitialized(instance) && !processed.contains(instance)) {
                    instancePOs.put(instance.toPO(tenantId), instance);
                    processed.add(instance);
                    added = true;
                }
            }
            if (!added) {
                break;
            }
        }
        return instancePOs;
    }

    private List<ReferencePO> getBufferedReferences(Collection<InstancePO> instancePOs) {
        return NncUtils.flatMap(
                instancePOs,
                instancePO -> getType(instancePO.getTypeId()).extractReferences(instancePO)
        );
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

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idService.allocate(tenantId, typeId2count);
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
    protected boolean checkAliveInStore(long id) {
        return loadingBuffer.isAlive(id);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public IInstanceContext newContext(long tenantId) {
        return new InstanceContext(
                tenantId,
                instanceStore,
                idService,
                executor,
                asyncPostProcessing,
                plugins,
                getParent(),
                getProfilerLogThreshold()
        );
    }

}
