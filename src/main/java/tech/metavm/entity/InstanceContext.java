package tech.metavm.entity;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.dto.ErrorCode;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

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
    private DefContext defContext;

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent
    ) {
        this(tenantId, instanceStore, idService, executor, asyncPostProcessing, plugins, parent,
                new DefaultTypeResolver()
        );
    }

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           IInstanceContext parent,
                           TypeResolver typeResolver
    ) {
        this(tenantId, instanceStore, idService, executor, asyncPostProcessing, plugins,
                ModelDefRegistry.getDefContext(),
                parent, typeResolver);
    }

    public InstanceContext(long tenantId,
                           IInstanceStore instanceStore,
                           EntityIdProvider idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins,
                           DefContext defContext,
                           IInstanceContext parent,
                           TypeResolver typeResolver
    ) {
        super(tenantId, idService, instanceStore, defContext, parent);
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
        this.defContext = defContext;
        setCreateJob(job -> getEntityContext().bind(job));
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
        InstancePO instancePO = loadingBuffer.getInstancePO(instance.getId());
        if (instancePO == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, instance.getId());
        }
        headContext.add(EntityUtils.copyPojo(instancePO), instance.getType());
        clearStaleReferences(instancePO);
        initializeInstance(instance, instancePO);
    }

    private void initializeInstance(Instance instance, InstancePO instancePO) {
        if (instance instanceof ArrayInstance arrayInstance) {
            InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
            List<Instance> elements = NncUtils.map(
                    arrayPO.getElements(),
                    e -> resolveColumnValue(TypeUtil.getElementType(arrayInstance.getType()), e)
            );
            arrayInstance.setElementAsChild(arrayPO.isElementAsChild());
            arrayInstance.initialize(elements);
        } else if (instance instanceof ClassInstance classInstance) {
            classInstance.initialize(
                    getInstanceFields(instancePO, classInstance.getType()),
                    instancePO.getVersion(), instancePO.getSyncVersion()
            );
        }
    }

    private Map<Field, Instance> getInstanceFields(InstancePO instancePO, ClassType type) {
        Map<Field, Instance> data = new HashMap<>();
        for (Field field : type.getFields()) {
            data.put(field, resolveColumnValue(field.getType(), instancePO.get(field.getColumnName())));
        }
        return data;
    }

    @Override
    protected boolean isPersisted(long id) {
        return headContext.get(id) != null;
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
            return InstanceUtils.resolvePrimitiveValue(fieldType, columnValue, defContext::getType);
        }
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }

    public void finish() {
        if (finished) {
            throw new IllegalStateException("Already finished");
        }
        rebind();
        initIds();
        entityContext.afterContextIntIds();
        IdentityHashMap<InstancePO, Instance> bufferedPOs = getBufferedInstancePOs();
        ContextDifference difference = buildDifference(bufferedPOs.keySet());
        Set<Instance> orphans = getOrphans(difference);
        if (!orphans.isEmpty()) {
            batchRemove(orphans);
            initIds();
            bufferedPOs = getBufferedInstancePOs();
            difference = buildDifference(bufferedPOs.keySet());
        }
        processUpdate(difference.getEntityChange(), bufferedPOs);
        processRemoval(difference.getEntityChange());
        processEntityChangeHelper(difference.getEntityChange());
        instanceStore.saveReferences(difference.getReferenceChange().toChangeList());
        headContext.clear();
        for (InstancePO instancePO : bufferedPOs.keySet()) {
            headContext.add(EntityUtils.copyPojo(instancePO), getType(instancePO.getTypeId()));
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            registerTransactionSynchronization();
        } else {
            postProcess();
        }
        finished = true;
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

    private void processUpdate(EntityChange<InstancePO> entityChange,
                               IdentityHashMap<InstancePO, Instance> bufferedInstancePOs) {
        List<InstancePO> insertOrUpdate = NncUtils.merge(entityChange.inserts(), entityChange.updates());
        for (InstancePO instancePO : insertOrUpdate) {
            Instance instance = bufferedInstancePOs.get(instancePO);
            if (instance instanceof ClassInstance classInstance) {
                var model = entityContext.getModel(Object.class, instance);
                if (model instanceof UpdateAware updateAware) {
                    updateAware.onUpdate(classInstance);
                }
            }
        }
    }

    private void processRemoval(EntityChange<InstancePO> entityChange) {
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

    private Set<Instance> getOrphans(ContextDifference difference) {
        Set<Long> removed = NncUtils.mapUnique(difference.getEntityChange().deletes(), InstancePO::getId);
        Set<Instance> orphans = new HashSet<>();
        for (ReferencePO removedRef : difference.getReferenceChange().deletes()) {
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
            return array.isElementAsChild();
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

    private void processEntityChangeHelper(EntityChange<InstancePO> change) {
        plugins.forEach(p -> p.beforeSaving(change, this));
        instanceStore.save(change.toChangeList());
        plugins.forEach(p -> p.afterSaving(change, this));
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
        return loadingBuffer.isRefTargetAlive(id);
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
                getParent()
        );
    }

}
