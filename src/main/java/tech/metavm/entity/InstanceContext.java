package tech.metavm.entity;

import javassist.util.proxy.ProxyObject;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.dto.ErrorCode;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static tech.metavm.object.meta.TypeUtil.*;

public class InstanceContext extends BaseInstanceContext {

    private boolean finished;
    private final SubContext headContext = new SubContext();
    private final boolean asyncPostProcessing;
    private final IInstanceStore instanceStore;

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

        super(tenantId, idService, parent);
        this.instanceStore = instanceStore;
        this.asyncPostProcessing = asyncPostProcessing;
        this.plugins = plugins;
        this.executor = executor;
        this.typeResolver = typeResolver;
        loadingBuffer = new LoadingBuffer(this);
        entityContext = new EntityContext(
                this,
                NncUtils.get(parent, IInstanceContext::getEntityContext)
        );
    }

    @Override
    protected void onReplace(List<Instance> replacements) {
        for (Instance replacement : replacements) {
            preload(replacement.getId());
        }
        for (Instance replacement : replacements) {
            InstancePO existingPO = loadingBuffer.getEntityPO(replacement.getId());
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
        Constructor<I> constructor = ReflectUtils.getConstructor(klass, type.getClass());
        return ReflectUtils.invokeConstructor(constructor, type);
    }

    @Override
    protected Instance createInstance(long id) {
        preload(id);
        Type type = getType(idService.getTypeId(id));
        return EntityProxyFactory.getProxy(
                getInstanceJavaType(type),
                id,
                this::initializeInstance,
                klass -> constructInstance(klass, id)
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
        InstancePO instancePO = loadingBuffer.getEntityPO(instance.getId());
        if(instancePO == null) {
            throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, instance.getId());
        }
        headContext.add(instancePO);
        initializeInstance(instance, instancePO);
    }

    private void initializeInstance(Instance instance, InstancePO instancePO) {
        if(instance instanceof ArrayInstance arrayInstance) {
            InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
            List<Instance> elements = NncUtils.map(
                    arrayPO.getElements(),
                    e -> resolveColumnValue(TypeUtil.getElementType(arrayInstance.getType()), e)
            );
            arrayInstance.initialize(elements);
        }
        else if (instance instanceof ClassInstance classInstance){
            classInstance.initialize(getInstanceFields(instancePO, classInstance.getType()));
        }
    }

    private Map<Field, Instance> getInstanceFields(InstancePO instancePO, ClassType type) {
        Map<Field, Instance> data = new HashMap<>();
        for (Field field : type.getFields()) {
            data.put(field, resolveColumnValue(field.getType(), instancePO.get(field.getColumnName())));
        }
        return data;
    }

    private Instance resolveColumnValue(Type fieldType, Object columnValue) {
        if(columnValue == null) {
            return InstanceUtils.nullInstance();
        }
        else if(columnValue instanceof ReferencePO referencePO) {
            return get(referencePO.id());
        }
        else if(fieldType.isReference()) {
            return get((long) columnValue);
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

    public List<Instance> selectByKey(IndexKeyPO key) {
        List<Long> instanceIds = instanceStore.selectByKey(key, this);
        return NncUtils.map(instanceIds, this::get);
    }

    public void finish() {
        if(finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        initIds();
        ContextDifference difference = new ContextDifference();
        List<InstancePO> bufferedPOs = getBufferedEntityPOs();
        difference.diff(headContext.getEntities(), bufferedPOs);
        processEntityChangeHelper(difference.getEntityChange());
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

    @Override
    public boolean isFinished() {
        return finished;
    }

    private List<InstancePO> getBufferedEntityPOs() {
        return NncUtils.filterAndMap(
                instances,
                InstanceUtils::isInitialized,
                instance -> instance.toPO(tenantId)
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

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

}
