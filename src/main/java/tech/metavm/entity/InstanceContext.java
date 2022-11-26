package tech.metavm.entity;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class InstanceContext {

    private boolean finished;
    private final long tenantId;
    private final SubContext headContext = new SubContext();
    private final SubContext bufferContext = new SubContext();
    private final IdService idService;
    private final IdentitySet<Instance> loadedInstances = new IdentitySet<>();
//    private final Map<EntityKey, Entity> refMap = new HashMap<>();
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final boolean asyncPostProcessing;
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    private final Set<Instance> instances = new LinkedHashSet<>();
    private final InstanceStore instanceStore;

    private final LoadingBuffer loadingBuffer;
    private final List<ContextPlugin> plugins;
    private final Executor executor;
    private final EntityContext entityContext;

    public InstanceContext(long tenantId,
                           InstanceStore instanceStore,
                           IdService idService,
                           Executor executor,
                           boolean asyncPostProcessing,
                           List<ContextPlugin> plugins
    ) {
        this.tenantId = tenantId;
        this.instanceStore = instanceStore;
        this.asyncPostProcessing = asyncPostProcessing;
        this.idService = idService;
        this.plugins = plugins;
        this.executor = executor;
        entityContext = new EntityContext(this);
        loadingBuffer = new LoadingBuffer(this);
    }
//
//    public List<Instance> batchGetInstances(Collection<Long> ids) {
//        return batchGet(Instance.class, ids, LoadingOption.none());
//    }

    public List<IInstance> batchGet(Collection<Long> ids) {
        return batchGet(ids, LoadingOption.none());
    }

//    public <T extends Entity> List<T> getLoadingList(ForeignKeyDef<T> foreignKeyDef, long refId) {
//        return new LoadingList<>(() -> getByForeignKey(foreignKeyDef, refId));
//    }
////
//    public <T extends Entity> List<T> getByForeignKey(ForeignKeyDef<T> foreignKeyDef, long refId) {
//        return getByForeignKey(foreignKeyDef.getEntityType(), new ForeignKey(foreignKeyDef, refId));
//    }
//
//    public <T extends Entity> List<T> getByForeignKey(Class<T> entityType, ForeignKey foreignKey) {
//        foreignKey.check(entityType);
//        List<Long> ids = loadingBuffer.getByForeignKey(foreignKey);
//        return NncUtils.map(ids, id -> getRef(entityType, id));
//    }

//    public <T extends Value> List<T> getByValueKey(ValueKeyDef<T> valueKeyDef, long refId) {
//        return loadingBuffer.getValueList(new ValueKey<>(valueKeyDef, refId));
//    }

//    public void loadValueKey(ValueKeyDef<?> valueKeyDef, List<Long> refIds) {
//        for (Long refId : refIds) {
//            loadingBuffer.loadValueKey(new ValueKey<>(valueKeyDef, refId));
//        }
//    }

//    public void loadForeignKey(ForeignKeyDef<?> foreignKeyDef, List<Long> refIds) {
//        ForeignKeyRequest.create(foreignKeyDef, refIds).forEach(loadingBuffer::loadForeignKey);
//    }

    public void load(long id, LoadingOption...options) {
        load(id, new HashSet<>(Arrays.asList(options)));
    }

    public void load(Collection<Long> ids, LoadingOption...options) {
        for (Long id : ids) {
            load(id, options);
        }
    }

    public void load(Collection<Long> ids, Set<LoadingOption> options) {
        for (Long id : ids) {
            load(id, options);
        }
    }

    public void load(long id, Set<LoadingOption> options) {
        loadingBuffer.load(new LoadRequest(id, options));
    }

    public List<IInstance> getByType(Type type) {
        return createLoadingList(loadingBuffer.getByType(type));
    }

    private LoadingList<IInstance> createLoadingList(List<InstancePO> instancePOs) {
        return LoadingList.create(instancePOs, instancePO -> InstanceFactory.create(instancePO, this));
    }

    public List<IInstance> batchGet(Collection<Long> ids,
                                               LoadingOption firstOption, LoadingOption...restOptions) {
        return batchGet(ids, LoadingOption.of(firstOption, restOptions));
    }

    public List<IInstance> batchGet(Collection<Long> ids, Set<LoadingOption> options) {
        load(ids, options);
        return NncUtils.map(ids, id -> new InstanceRef(id, getEntityType(id), () -> getReal(id)));
    }

    public Instance getReal(long id) {
        if(!instanceMap.containsKey(id)) {
            InstancePO instancePO = loadingBuffer.getEntityPO(id);
            if (instancePO == null) {
                throw new InternalException("Instance not found, id: " + id);
            }
            instanceMap.put(id, InstanceFactory.create(instancePO, this));
        }
        return instanceMap.get(id);
    }

    private void ensureCreated(long id) {
        if(!isConstructed(id)) {
            InstancePO instancePO = loadingBuffer.getEntityPO(id);
            headContext.add(instancePO);
            instanceMap.put(
                    id,
                    InstanceFactory.create(instancePO, this)
            );
        }
    }

    private boolean isConstructed(long id) {
        return instanceMap.containsKey(id);
    }

    public void preload(List<InstancePO> instancePOs) {
        for (InstancePO entityPO : instancePOs) {
            loadingBuffer.preload(entityPO.getId(), entityPO);
        }
    }

    public IInstance get(long id) {
        return get(id, LoadingOption.none());
    }

    public IInstance selectByUniqueKey(IndexKeyPO key) {
        return NncUtils.getFirst(selectByKey(key));
    }

    public List<IInstance> selectByKey(IndexKeyPO key) {
        return instanceStore.selectByKey(key, this);
    }

    public IInstance get(long id, Set<LoadingOption> options) {
        return NncUtils.getFirst(batchGet(List.of(id), options));
    }

    public void add(Instance instance) {
        if(instance.getId() != null) {
            throw new RuntimeException("Can not add an already persisted entity, objectId: " + instance.getId());
        }
        instances.add(instance);
    }

    public void remove(IInstance instance) {
        boolean removed = instances.remove(getReal(instance.getId()));
        if(removed) {
            for (Field field : instance.getType().getFields()) {
                if (field.isChildField()) {
                    IInstance ref = instance.getInstance(field);
                    if (ref != null) {
                        remove(ref);
                    }
                }
            }
            if(instance instanceof InstanceArray instanceArray) {
                if(instanceArray.isElementAsChild()) {
                    instanceArray.getElements().forEach(this::remove);
                }
            }
        }
    }

    public void remove(Value value) {
        bufferContext.remove(value);
    }

    private long getTypeIdById(long id) {
        return idService.getBydId(id).getTypeId();
    }

    public void initIds() {
        Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
        List<Instance> instancesToInit = NncUtils.filter(instances, inst -> inst.getId() == null);
        Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInit, Instance::getType);
        Map<Type,List<Long>> idMap = idGenerator.apply(countMap);
        Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInit, Instance::getType);
        type2instances.forEach((type, instances) -> {
            List<Long> ids = idMap.get(type);
            NncUtils.biForEach(instances, ids, Instance::initId);
        });

    }

    public void finish() {
        if(finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        initIds();
        ContextDifference difference = new ContextDifference();
        difference.diff(headContext.getEntities(), getBufferedEntityPOs());
        difference.diffValues(headContext.values(), bufferContext.values());

        processEntityChangeHelper(difference.getEntityChange());

//        List<EntityChange<?>> changes = new ArrayList<>(difference.getChangeMap().values());
//        Collections.sort(changes);
//        for (EntityChange<?> change : changes) {
//            if(!change.isEmpty()) {
//                processEntityChangeHelper(change);
//            }
//        }

//        List<ValueChange<?>> valueChanges = new ArrayList<>(difference.getValueChangeMap().values());
//        for (ValueChange<?> valueChange : valueChanges) {
//            if(!valueChange.isEmpty()) {
//                processValueChange(valueChange);
//            }
//        }

        headContext.clear();
        for (InstancePO entity : bufferContext.getEntities()) {
//            entity.setPersisted(true);
            headContext.add(EntityUtils.copyPojo(entity));
        }
        registerTransactionSynchronization();
    }

    public Class<?> getEntityType(long id) {
        return EntityTypeRegistry.getEntityType(idService.getBydId(id).getTypeId());
    }

    private List<InstancePO> getBufferedEntityPOs() {
        return NncUtils.map(instances, Instance::toPO);
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

    public void postProcess() {
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

//    private <T extends Value> void processValueChange(ValueChange<T> change) {
//        ValueStore<T> store = getValueStore(change.getValueType());
//        change.apply(store);
//    }

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idService.allocate(tenantId, typeId2count);
    }

    public void bind(Instance instance) {
        loadedInstances.add(instance);
        instances.add(instance);
//        bufferContext.add(entity);
//        if(entity.isPersisted()) {
//            headContext.add(EntityUtils.copyEntity(entity));
//        }
    }

    public void bindValue(Value value) {
        bufferContext.addValue(value);
        if(value.isPersisted()) {
            headContext.addValue(value);
        }
    }

    public long getTenantId() {
        return tenantId;
    }


    @SuppressWarnings("unused")
    public <T> void setAttribute(ContextAttributeKey<T> key, T value) {
        if(key.isNotNull()) {
            NncUtils.requireNonNull(value);
        }
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(ContextAttributeKey<T> key) {
        if(!attributes.containsKey(key) && key.getDefaultValueSupplier() != null) {
            T defaultValue = key.getDefaultValueSupplier().get();
            attributes.put(key, defaultValue);
            return defaultValue;
        }
        else {
            return (T) attributes.get(key);
        }
    }

    public InstanceStore getInstanceStore() {
        return instanceStore;
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    public Type getType(Long id) {
        return entityContext.getType(id);
    }

    public Field getField(Long id) {
        return entityContext.getField(id);
    }

    public IdService getIdService() {
        return idService;
    }
}
