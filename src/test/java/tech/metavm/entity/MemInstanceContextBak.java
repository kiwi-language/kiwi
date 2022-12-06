//package tech.metavm.entity;
//
//import tech.metavm.object.instance.IInstanceStore;
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.instance.persistence.IndexKeyPO;
//import tech.metavm.object.instance.persistence.InstancePO;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.*;
//
//import javax.annotation.Nullable;
//import java.util.*;
//import java.util.function.Function;
//
//import static tech.metavm.util.Constants.ROOT_TENANT_ID;
//import static tech.metavm.util.InstanceUtils.getAllNewInstances;
//import static tech.metavm.util.TestConstants.TENANT_ID;
//
//public class MemInstanceContextBak implements IInstanceContext {
//
//    private final Map<IndexKeyPO, List<Long>> index = new HashMap<>();
//    private final IInstanceContext parent;
//    private final long tenantId;
//    private EntityContext entityContext;
//    private final Set<Instance> instances = new IdentitySet<>();
//    private final Map<Long, Instance> instanceMap = new HashMap<>();
//    private final EntityIdProvider idProvider;
//    private final IInstanceStore instanceStore;
//    private final Set<InstanceSinkListener> listeners = new LinkedHashSet<>();
//    private final Map<ContextAttributeKey<?>, Object> map = new HashMap<>();
//    private @Nullable Function<Long, Type> typeProvider;
//
//    public MemInstanceContextBak() {
//        idProvider = new MockIdProvider();
//        instanceStore = new MemInstanceStore();
//        parent = null;
//        tenantId = TENANT_ID;
//    }
//
//    public MemInstanceContextBak(long tenantId, EntityIdProvider idProvider, IInstanceStore instanceStore, IInstanceContext parent) {
//        this.tenantId = tenantId;
//        this.idProvider = idProvider;
//        this.instanceStore = instanceStore;
//        this.parent = parent;
//    }
//
//    public MemInstanceContextBak initData(Collection<Instance> instances) {
//        replace(instances);
//        return this;
//    }
//
//    @Override
//    public void replace(Collection<Instance> instances) {
//        instances = getAllNewInstances(instances, this);
//        for (Instance instance : instances) {
//            if (instance.getId() != null) {
//                if (parent != null && parent.containsId(instance.getId())) {
//                    parent.remove(instance);
//                } else {
//                    Instance existing = instanceMap.get(instance.getId());
//                    if (existing != null) {
//                        this.instances.remove(existing);
//                    }
//                    instanceMap.put(instance.getId(), instance);
//                    add(instance);
//                }
//            } else {
//                add(instance);
//            }
//        }
//    }
//
//    public void setTypeProvider(@Nullable Function<Long, Type> typeProvider) {
//        this.typeProvider = typeProvider;
//    }
//
//    void addIndex(IndexKeyPO key, long instanceId) {
//        index.computeIfAbsent(key, k -> new ArrayList<>()).add(instanceId);
//    }
//
//    @Override
//    public List<Instance> batchGet(Collection<Long> ids, Set<LoadingOption> options) {
//        if (parent == null) {
//            return batchGetSelf(ids);
//        } else {
//            List<Long> parentIds = NncUtils.filter(ids, parent::containsId);
//            List<Long> selfIds = NncUtils.filterNot(ids, parent::containsId);
//
//            return NncUtils.merge(
//                    parent.batchGet(parentIds, options),
//                    batchGetSelf(selfIds)
//            );
//        }
//    }
//
//    private List<Instance> batchGetSelf(Collection<Long> ids) {
//        return NncUtils.map(ids, instanceMap::get);
//    }
//
//    @Override
//    public IEntityContext getEntityContext() {
//        return entityContext;
//    }
//
//    @Override
//    public boolean containsInstance(Instance instance) {
//        return instances.contains(instance) || parent != null && parent.containsInstance(instance);
//    }
//
//    @Override
//    public boolean containsId(long id) {
//        return instanceMap.containsKey(id) || parent != null && parent.containsId(id);
//    }
//
//    @Override
//    public void finish() {
//        initIds();
//        ChangeList<InstancePO> changeList = ChangeList.inserts(
//                NncUtils.map(instances, instance -> instance.toPO(ROOT_TENANT_ID))
//        );
//        instanceStore.save(changeList);
//    }
//
//    @Override
//    public void addListener(InstanceSinkListener listener) {
//        listeners.add(listener);
//    }
//
//    @Override
//    public void initIds() {
//        List<Instance> instancesToInit = NncUtils.filter(instances, instance -> instance.getId() == null);
//        for (Instance instance : instancesToInit) {
//            instance.initId(idProvider.allocateOne(tenantId, instance.getType()));
//            instanceMap.put(instance.getId(), instance);
//            for (InstanceSinkListener listener : listeners) {
//                listener.onIdInitialized(instance);
//            }
//        }
//    }
//
//    @Override
//    public long getTenantId() {
//        return tenantId;
//    }
//
//    @Override
//    public Type getType(long id) {
//        NncUtils.requireNonNull(typeProvider, "typeProvider not set");
//        return typeProvider.apply(id);
//    }
//
//    @Override
//    public void remove(Instance instance) {
//        if (parent != null && parent.containsInstance(instance)) {
//            parent.remove(instance);
//        } else {
//            instances.remove(instance);
//            if (instance.getId() != null) {
//                instanceMap.remove(instance.getId());
//            }
//        }
//    }
//
//    @Override
//    public List<Instance> selectByKey(IndexKeyPO indexKeyPO) {
//        List<Long> instanceIds = index.get(indexKeyPO);
//        return NncUtils.map(instanceIds, this::get);
//    }
//
//    @Override
//    public void bind(Instance instance) {
//        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
//        getAllNewInstances(instance, this).forEach(this::add);
//    }
//
//    private void add(Instance instance) {
//        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
//        this.instances.add(instance);
//        if(instance.getId() != null) {
//            instanceMap.put(instance.getId(), instance);
//        }
//    }
//
//    @Override
//    public <E> E getAttribute(ContextAttributeKey<E> key) {
//        Object value = map.get(key);
//        if(value == null && key.getDefaultValueSupplier() != null) {
//            value = key.getDefaultValueSupplier().get();
//            map.put(key, value);
//        }
//        return (E) value;
//    }
//
//    public void setEntityContext(EntityContext entityContext) {
//        this.entityContext = entityContext;
//    }
//}
