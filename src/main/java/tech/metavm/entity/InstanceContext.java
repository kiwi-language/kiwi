package tech.metavm.entity;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.dto.ErrorCode;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
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

        super(tenantId, idService, instanceStore, parent);
        this.asyncPostProcessing = asyncPostProcessing;
        this.plugins = plugins;
        this.executor = executor;
        this.typeResolver = typeResolver;
        loadingBuffer = new LoadingBuffer(this);
        entityContext = new EntityContext(
                this,
                NncUtils.get(parent, IInstanceContext::getEntityContext)
        );
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
                headContext.add(existingPO);
            }
        }
    }

    @Override
    public void preload(Collection<Long> ids, LoadingOption...options) {
        for (Long id : ids) {
            preload(id, options);
        }
    }

    public void preload(long id, LoadingOption...options) {
        loadingBuffer.load(new LoadRequest(id, LoadingOption.of(options)));
    }

    private <I extends Instance> I constructInstance(Class<I> klass, long id) {
        long typeId = idService.getTypeId(id);
        Type type = getType(typeId);
        Class<?> typeClass = EntityUtils.getEntityType(type.getClass());
        if(typeClass == EnumType.class) {
            typeClass = ClassType.class;
        }
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
        if(type instanceof ArrayType) {
            return ArrayInstance.class;
        }
        if(type instanceof PrimitiveType) {
            return PrimitiveInstance.class;
        }
        if(type instanceof ClassType){
            return ClassInstance.class;
        }
        throw new InternalException("Can not resolve instance type for type " + type.getName());
    }

    private void initializeInstance(Instance instance) {
        InstancePO instancePO = loadingBuffer.getInstancePO(instance.getId());
        if(instancePO == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, instance.getId());
        }
        headContext.add(EntityUtils.copyPojo(instancePO));
        clearStaleReferences(instancePO);
        initializeInstance(instance, instancePO);
    }

    private void initializeInstance(Instance instance, InstancePO instancePO) {
        if(instance instanceof ArrayInstance arrayInstance) {
            InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
            List<Instance> elements = NncUtils.map(
                    arrayPO.getElements(),
                    e -> resolveColumnValue(TypeUtil.getElementType(arrayInstance.getType()), e)
            );
            arrayInstance.setElementAsChild(arrayPO.isElementAsChild());
            arrayInstance.initialize(elements);
        }
        else if (instance instanceof ClassInstance classInstance){
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
        if(columnValue == null) {
            return InstanceUtils.nullInstance();
        }
        else if(columnValue instanceof IdentityPO identityPO) {
            return get(identityPO.id());
        }
        else if(fieldType.isReference()) {
            return get(ValueUtil.getLong(columnValue));
        }
        else if(columnValue instanceof InstancePO instancePO) {
            Class<? extends Instance> instanceType =
                    instancePO instanceof InstanceArrayPO ? ArrayInstance.class : ClassInstance.class;
            Type type = getType(instancePO.getTypeId());
            Instance instance = InstanceFactory.allocate(instanceType, type);
            initializeInstance(instance, instancePO);
            return instance;
        }
        else {
            return InstanceUtils.resolvePrimitiveValue(fieldType, columnValue);
        }
    }

    public Instance selectByUniqueKey(IndexKeyPO key) {
        return NncUtils.getFirst(selectByKey(key));
    }

    public void finish() {
        if(finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        rebind();
        initIds();
        ContextDifference difference = new ContextDifference(this);
        List<InstancePO> bufferedPOs = getBufferedEntityPOs();
        difference.diff(headContext.getEntities(), bufferedPOs);
        processRemoval(difference.getEntityChange());
        processEntityChangeHelper(difference.getEntityChange());
        instanceStore.saveReferences(difference.getReferenceChange().toChangeList());
        headContext.clear();
        for (InstancePO entity : bufferedPOs) {
            headContext.add(EntityUtils.copyPojo(entity));
        }
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            registerTransactionSynchronization();
        }
        else {
            postProcess();
        }
    }

    private void processRemoval(EntityChange<InstancePO> entityChange) {
        if(NncUtils.isEmpty(entityChange.deletes())) {
            return;
        }
        Set<Long> idsToRemove = NncUtils.mapUnique(entityChange.deletes(), InstancePO::getId);
        Set<Long> idsToUpdate = NncUtils.mapUnique(entityChange.updates(), InstancePO::getId);
        Set<Long> stronglyReferencedIds = instanceStore.getStronglyReferencedIds(
                tenantId, idsToRemove, mergeSets(idsToRemove, idsToUpdate)
        );
        if(NncUtils.isNotEmpty(stronglyReferencedIds)) {
            List<Instance> srInstances = NncUtils.map(stronglyReferencedIds, this::get);
            throw BusinessException.strongReferencesPreventRemoval(srInstances);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public static final int MAX_ITERATION = 5;

    private List<InstancePO> getBufferedEntityPOs() {
        List<InstancePO> instancePOs = new ArrayList<>();
        Set<Instance> processed = new IdentitySet<>();
        int it = 0;
        for(;;) {
            if(it++ >= MAX_ITERATION) {
                throw new InternalException("getBufferedEntityPOs reached max number of iteration " +
                        "(" + MAX_ITERATION + ")");
            }
            boolean added = false;
            for (Instance instance : new ArrayList<>(instances)) {
                if (InstanceUtils.isInitialized(instance) && !processed.contains(instance)) {
                    instancePOs.add(instance.toPO(tenantId));
                    processed.add(instance);
                    added = true;
                }
            }
            if(!added) {
                break;
            }
        }
        return instancePOs;
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
        if(asyncPostProcessing) {
            executor.execute(this::postProcess0);
        }
        else {
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
