//package tech.metavm.entity;
//
//import org.springframework.transaction.support.TransactionSynchronization;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//import tech.metavm.object.instance.ContextPlugin;
//import tech.metavm.object.instance.IInstanceStore;
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.instance.InstanceArray;
//import tech.metavm.object.instance.persistence.IndexKeyPO;
//import tech.metavm.object.instance.persistence.InstanceArrayPO;
//import tech.metavm.object.instance.persistence.InstancePO;
//import tech.metavm.object.instance.persistence.ReferencePO;
//import tech.metavm.object.meta.Field;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.IdentitySet;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.ReflectUtils;
//import tech.metavm.util.ValueUtil;
//
//import java.lang.reflect.Constructor;
//import java.util.*;
//import java.util.concurrent.Executor;
//import java.util.function.Function;
//
//import static tech.metavm.util.NncUtils.requireNonNull;
//
//public class InstanceContextBak implements IInstanceContext {
//
//    private boolean finished;
//    private final long tenantId;
//    private final SubContext headContext = new SubContext();
//    //    private final SubContext bufferContext = new SubContext();
//    private final EntityIdProvider idService;
//    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
//    private final boolean asyncPostProcessing;
//    private final Map<Long, Instance> instanceMap = new HashMap<>();
//    private final Set<Instance> instances = new IdentitySet<>();
//    private final IInstanceStore instanceStore;
//
//    private final LoadingBuffer loadingBuffer;
//    private final List<ContextPlugin> plugins;
//    private final Executor executor;
//    private IEntityContext entityContext;
//    private final IInstanceContext parent;
//    private final Set<InstanceSinkListener> listeners = new LinkedHashSet<>();
//    private TypeResolver typeResolver;
//
//    public InstanceContextBak(long tenantId,
//                           IInstanceStore instanceStore,
//                           EntityIdProvider idService,
//                           Executor executor,
//                           boolean asyncPostProcessing,
//                           List<ContextPlugin> plugins,
//                           IInstanceContext parent
//    ) {
//        this(tenantId, instanceStore, idService, executor, asyncPostProcessing, plugins, parent,
//                new DefaultTypeResolver()
//        );
//    }
//
//    public InstanceContextBak(long tenantId,
//                           IInstanceStore instanceStore,
//                           EntityIdProvider idService,
//                           Executor executor,
//                           boolean asyncPostProcessing,
//                           List<ContextPlugin> plugins,
//                           IInstanceContext parent,
//                           TypeResolver typeResolver
//    ) {
//        this.tenantId = tenantId;
//        this.instanceStore = instanceStore;
//        this.asyncPostProcessing = asyncPostProcessing;
//        this.idService = idService;
//        this.plugins = plugins;
//        this.executor = executor;
//        this.typeResolver = typeResolver;
//        loadingBuffer = new LoadingBuffer(this);
//        this.parent = parent;
//        entityContext = new EntityContext(
//                this,
//                NncUtils.get(parent, IInstanceContext::getEntityContext)
//        );
//    }
//
//    public void addListener(InstanceSinkListener listener) {
//        listeners.add(listener);
//    }
//
//    public void replace(Instance instance) {
//        replace(List.of(instance));
//    }
//
//    @Override
//    public void replace(Collection<Instance> instances) {
//        instances = getAllNewInstances(instances);
//        if(NncUtils.isEmpty(instances)) {
//            return;
//        }
//        List<Instance> newInstances = NncUtils.filter(instances, inst -> inst.getId() == null);
//        List<Instance> persistedInstances = NncUtils.filter(instances, inst -> inst.getId() != null);
//
//        newInstances.forEach(this::add);
//        replacePersisted(persistedInstances);
//    }
//
//    private void replacePersisted(List<Instance> persistedInstances) {
//        List<Instance> parentInstances = NncUtils.filter(persistedInstances, inst -> isIdInParent(inst.getId()));
//        List<Instance> selfInstances = NncUtils.filterNot(persistedInstances, inst -> isIdInParent(inst.getId()));
//
//        if(NncUtils.isNotEmpty(parentInstances)) {
//            parent.replace(parentInstances);
//        }
//
//        for (Instance instance : selfInstances) {
//            loadingBuffer.load(LoadRequest.create(instance.getId()));
//        }
//        for (Instance instance : selfInstances) {
//            Instance existing = instanceMap.get(instance.getId());
//            if(existing != null) {
//                instances.remove(existing);
//            }
//            InstancePO existingPO = loadingBuffer.getEntityPO(instance.getId());
//            if(existingPO != null) {
//                headContext.add(existingPO);
//            }
//            add(instance);
//        }
//    }
//
//    private Set<Instance> getAllNewInstances(Collection<Instance> instances) {
//        Set<Instance> result = new IdentitySet<>();
//        getAllNewInstances(instances, result);
//        return result;
//    }
//
//    private void getAllNewInstances(Collection<Instance> instances, Set<Instance> result) {
//        List<Instance> newInstances = NncUtils.filter(instances,
//                inst -> !this.instances.contains(inst) && !result.contains(inst) && !inst.isValue()
//        );
//        result.addAll(newInstances);
//        Set<Instance> refInstances = new IdentitySet<>(NncUtils.flatMap(newInstances, Instance::getRefInstances));
//        if(!refInstances.isEmpty()) {
//            getAllNewInstances(refInstances, result);
//        }
//    }
//
//    private boolean isIdInParent(long id) {
//        return parent != null && parent.containsId(id);
//    }
//
//    public List<Instance> batchGet(Collection<Long> ids) {
//        return batchGet(ids, LoadingOption.none());
//    }
//
//    @Override
//    public boolean containsInstance(Instance instance) {
//        return instances.contains(instance) || parent != null && parent.containsInstance(instance);
//    }
//
//    public void setTypeResolver(TypeResolver typeResolver) {
//        this.typeResolver = typeResolver;
//    }
//
//    public void load(long id, LoadingOption...options) {
//        load(id, new HashSet<>(Arrays.asList(options)));
//    }
//
//    public void load(Collection<Long> ids, LoadingOption...options) {
//        for (Long id : ids) {
//            load(id, options);
//        }
//    }
//
//    public void load(Collection<Long> ids, Set<LoadingOption> options) {
//        for (Long id : ids) {
//            load(id, options);
//        }
//    }
//
//    public void load(long id, Set<LoadingOption> options) {
//        loadingBuffer.load(new LoadRequest(id, options));
//    }
//
//    @Override
//    public List<Instance> batchGet(Collection<Long> ids,
//                                   LoadingOption firstOption, LoadingOption... restOptions) {
//        return batchGet(ids, LoadingOption.of(firstOption, restOptions));
//    }
//
//    @Override
//    public List<Instance> batchGet(Collection<Long> ids, Set<LoadingOption> options) {
//        if(parent == null) {
//            return batchGetSelf(ids, options);
//        }
//        else {
//            Collection<Long> parentIds = NncUtils.filter(ids, parent::containsId);
//            Collection<Long> selfIds = NncUtils.filterNot(ids, parent::containsId);
//            return NncUtils.merge(
//                    parent.batchGet(parentIds, options),
//                    batchGetSelf(selfIds, options)
//            );
//        }
//    }
//
//    private List<Instance> batchGetSelf(Collection<Long> ids, Set<LoadingOption> options) {
//        load(ids, options);
//        return NncUtils.map(
//                ids,
//                this::get0
//        );
//    }
//
//    public Map<Long, Instance> getInstanceMap() {
//        return instanceMap;
//    }
//
//    private <I extends Instance> I constructInstance(Class<I> klass, long id) {
//        long typeId = idService.getTypeId(id);
//        Type type = getType(typeId);
//        Constructor<I> constructor = ReflectUtils.getConstructor(klass, Type.class);
//        return ReflectUtils.invokeConstructor(constructor, type);
//    }
//
//    private Instance get0(long id) {
//        Instance instance = instanceMap.get(id);
//        if(instance == null) {
//            instance = createInstanceProxy(id);
//        }
//        return instance;
//    }
//
//    private Instance createInstanceProxy(long id) {
//        loadingBuffer.load(new LoadRequest(id, LoadingOption.none()));
//        Type type = getType(idService.getTypeId(id));
//        Instance instance = EntityProxyFactory.getProxy(
//                getInstanceJavaType(type),
//                id,
//                this::initializeInstance,
//                klass -> constructInstance(klass, id)
//        );
//        add(instance);
//        return instance;
//    }
//
//    private Class<? extends Instance> getInstanceJavaType(Type type) {
//        if(type.isArray()) {
//            return InstanceArray.class;
//        }
//        else {
//            return Instance.class;
//        }
//    }
//
//    private void initializeInstance(Instance instance) {
//        InstancePO instancePO = loadingBuffer.getEntityPO(instance.getId());
//        headContext.add(instancePO);
//        initializeInstance(instance, instancePO);
//    }
//
//    private void initializeInstance(Instance instance, InstancePO instancePO) {
//        if(instance instanceof InstanceArray instanceArray) {
//            InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
//            List<Object> elements = NncUtils.map(
//                    arrayPO.getElements(),
//                    e -> resolveColumnValue(instanceArray.getType().getElementType(), e)
//            );
//            instanceArray.initialize(elements);
//        }
//        else {
//            instance.initialize(getInstanceFields(instancePO, instance.getType()));
//        }
//    }
//
//    private Map<Field, Object> getInstanceFields(InstancePO instancePO, Type type) {
//        Map<Field, Object> data = new HashMap<>();
//        for (Field field : type.getFields()) {
//            data.put(field, resolveColumnValue(field.getType(), instancePO.get(field.getColumnName())));
//        }
//        return data;
//    }
//
//    private Object resolveColumnValue(Type fieldType, Object columnValue) {
//        if(columnValue instanceof ReferencePO referencePO) {
//            return get0(referencePO.id());
//        }
//        else if(columnValue instanceof InstancePO instancePO) {
//            Class<? extends Instance> instanceType =
//                    instancePO instanceof InstanceArrayPO ? InstanceArray.class : Instance.class;
//            Type type = getType(instancePO.getTypeId());
//            Instance instance = InstanceFactory.allocate(instanceType, type);
//            initializeInstance(instance, instancePO);
//            return instance;
//        }
//        else if(ValueUtil.isInteger(columnValue)) {
//            if(fieldType.isInt()) {
//                return ((Number) columnValue).intValue();
//            }
//            else {
//                return ((Number) columnValue).longValue();
//            }
//        }
//        else if(ValueUtil.isFloat(columnValue)) {
//            return ((Number) columnValue).doubleValue();
//        }
//        else {
//            return columnValue;
//        }
//    }
//
//    private boolean isConstructed(long id) {
//        return instanceMap.containsKey(id);
//    }
//
//    public Instance get(long id) {
//        return get(id, LoadingOption.none());
//    }
//
//    public Instance selectByUniqueKey(IndexKeyPO key) {
//        return NncUtils.getFirst(selectByKey(key));
//    }
//
//    public List<Instance> selectByKey(IndexKeyPO key) {
//        List<Long> instanceIds = instanceStore.selectByKey(key, this);
//        return NncUtils.map(instanceIds, this::get);
//    }
//
//    public Instance get(long id, Set<LoadingOption> options) {
//        return NncUtils.getFirst(batchGet(List.of(id), options));
//    }
//
//    public void remove(Instance instance) {
//        boolean removed = instances.remove(get0(instance.getId()));
//        if(removed) {
//            for (Field field : instance.getType().getFields()) {
//                if (field.isChildField()) {
//                    Instance ref = instance.getInstance(field);
//                    if (ref != null) {
//                        remove(ref);
//                    }
//                }
//            }
//            if(instance instanceof InstanceArray instanceArray) {
//                if(instanceArray.isElementAsChild()) {
//                    for (Object element : instanceArray.getElements()) {
//                        if(element instanceof Instance elementInst) {
//                            remove(elementInst);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private long getTypeIdById(long id) {
//        return idService.getTypeId(id);
//    }
//
//    public void initIds() {
//        Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
//        List<Instance> instancesToInit = NncUtils.filter(instances, inst -> inst.getId() == null);
//        if(instancesToInit.isEmpty()) {
//            return;
//        }
//        Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInit, Instance::getType);
//        Map<Type,List<Long>> idMap = idGenerator.apply(countMap);
//        Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInit, Instance::getType);
//        type2instances.forEach((type, instances) -> {
//            List<Long> ids = idMap.get(type);
//            NncUtils.biForEach(instances, ids, Instance::initId);
//        });
//        for (Instance instance : instances) {
//            instanceMap.put(instance.getId(), instance);
//            listeners.forEach(l -> l.onIdInitialized(instance));
//        }
//    }
//
//    public void finish() {
//        if(finished) {
//            throw new IllegalStateException("Already finished");
//        }
////        if(parent != null) {
////            parent.finish();
////        }
//        finished = true;
//        initIds();
//        ContextDifference difference = new ContextDifference();
//        List<InstancePO> bufferedPOs = getBufferedEntityPOs();
//        difference.diff(headContext.getEntities(), bufferedPOs);
////        difference.diffValues(headContext.values(), bufferContext.values());
//
//        processEntityChangeHelper(difference.getEntityChange());
//
////        List<EntityChange<?>> changes = new ArrayList<>(difference.getChangeMap().values());
////        Collections.sort(changes);
////        for (EntityChange<?> change : changes) {
////            if(!change.isEmpty()) {
////                processEntityChangeHelper(change);
////            }
////        }
//
////        List<ValueChange<?>> valueChanges = new ArrayList<>(difference.getValueChangeMap().values());
////        for (ValueChange<?> valueChange : valueChanges) {
////            if(!valueChange.isEmpty()) {
////                processValueChange(valueChange);
////            }
////        }
//
//        headContext.clear();
//        for (InstancePO entity : bufferedPOs) {
////            entity.setPersisted(true);
//            headContext.add(EntityUtils.copyPojo(entity));
//        }
//        if(TransactionSynchronizationManager.isActualTransactionActive()) {
//            registerTransactionSynchronization();
//        }
//        else {
//            postProcess();
//        }
//    }
//
//    private List<InstancePO> getBufferedEntityPOs() {
//        return NncUtils.map(instances, instance -> instance.toPO(tenantId));
//    }
//
//    private void registerTransactionSynchronization() {
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//                        postProcess();
//                    }
//                }
//        );
//    }
//
//    private void postProcess() {
//        if(asyncPostProcessing) {
//            executor.execute(this::postProcess0);
//        }
//        else {
//            postProcess0();
//        }
//    }
//
//    private void postProcess0() {
//        for (ContextPlugin plugin : plugins) {
//            plugin.postProcess(this);
//        }
//    }
//
//    private void processEntityChangeHelper(EntityChange<InstancePO> change) {
//        plugins.forEach(p -> p.beforeSaving(change, this));
//        instanceStore.save(change.toChangeList());
//        plugins.forEach(p -> p.afterSaving(change, this));
//    }
//
//    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
//        return (typeId2count) -> idService.allocate(tenantId, typeId2count);
//    }
//
//    public void bind(Instance instance) {
//        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
//        getAllNewInstances(List.of(instance)).forEach(this::add);
//    }
//
//    private void add(Instance instance) {
//        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
//        instances.add(instance);
//        if(instance.getId() != null) {
//            instanceMap.put(instance.getId(), instance);
//        }
//    }
//
//    public long getTenantId() {
//        return tenantId;
//    }
//
//    @SuppressWarnings("unused")
//    public <T> void setAttribute(ContextAttributeKey<T> key, T value) {
//        if(key.isNotNull()) {
//            requireNonNull(value);
//        }
//        attributes.put(key, value);
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> T getAttribute(ContextAttributeKey<T> key) {
//        if(!attributes.containsKey(key) && key.getDefaultValueSupplier() != null) {
//            T defaultValue = key.getDefaultValueSupplier().get();
//            attributes.put(key, defaultValue);
//            return defaultValue;
//        }
//        else {
//            return (T) attributes.get(key);
//        }
//    }
//
//    public IInstanceStore getInstanceStore() {
//        return instanceStore;
//    }
//
//    public IEntityContext getEntityContext() {
//        return entityContext;
//    }
//
//    @Override
//    public boolean containsId(long id) {
//        return parent != null && parent.containsId(id) || instanceMap.get(id) != null;
//    }
//
//    @Override
//    public Type getType(long id) {
//        return typeResolver.getType(this, id);
//    }
//
//    public EntityIdProvider getIdService() {
//        return idService;
//    }
//
//    public void setEntityContext(IEntityContext entityContext) {
//        this.entityContext = entityContext;
//    }
//
//}
