package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext{
    protected final long tenantId;
    protected final EntityIdProvider idService;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    protected final Set<Instance> instances = new IdentitySet<>();

    private final IInstanceContext parent;
    private final Set<InstanceSinkListener> listeners = new LinkedHashSet<>();

    public BaseInstanceContext(long tenantId, EntityIdProvider idService, IInstanceContext parent) {
        this.tenantId = tenantId;
        this.idService = idService;
        this.parent = parent;
    }

    @Override
    public void replace(Collection<Instance> instances) {
        instances = getAllNewInstances(instances);
        if(NncUtils.isEmpty(instances)) {
            return;
        }
        List<Instance> newInstances = NncUtils.filter(instances, inst -> inst.getId() == null);
        List<Instance> persistedInstances = NncUtils.filter(instances, inst -> inst.getId() != null);

        newInstances.forEach(this::add);
        replaceActually(persistedInstances);
    }

    private void replaceActually(List<Instance> persistedInstances) {
        List<Instance> parentInstances = NncUtils.filter(persistedInstances, inst -> isIdInParent(inst.getId()));
        List<Instance> selfInstances = NncUtils.filterNot(persistedInstances, inst -> isIdInParent(inst.getId()));

        if(NncUtils.isNotEmpty(parentInstances)) {
            parent.replace(parentInstances);
        }
        onReplace(selfInstances);
        for (Instance instance : selfInstances) {
            Instance existing = instanceMap.get(instance.getId());
            if(existing != null) {
                instances.remove(existing);
            }
            add(instance);
        }
    }

    protected void onReplace(List<Instance> replacements) {}

    @Override
    public void preload(Collection<Long> ids, LoadingOption... options) {}

    @Override
    public List<Instance> batchGet(Collection<Long> ids) {
        if(parent == null) {
            return batchGetSelf(ids);
        }
        else {
            Collection<Long> parentIds = NncUtils.filter(ids, parent::containsId);
            Collection<Long> selfIds = NncUtils.filterNot(ids, parent::containsId);
            return NncUtils.merge(
                    parent.batchGet(parentIds),
                    batchGetSelf(selfIds)
            );
        }
    }

    private List<Instance> batchGetSelf(Collection<Long> ids) {
        return NncUtils.map(
                ids,
                this::getSelf
        );
    }

    private Instance getSelf(long id) {
        Instance instance = instanceMap.get(id);
        if(instance == null) {
            instance = createInstance(id);
            add(instance);
        }
        return instance;
    }

    protected abstract Instance createInstance(long id);

    @Override
    public abstract IEntityContext getEntityContext();

    @Override
    public boolean containsInstance(Instance instance) {
        return instances.contains(instance) || parent != null && parent.containsInstance(instance);
    }

    @Override
    public boolean containsId(long id) {
        return parent != null && parent.containsId(id) || instanceMap.get(id) != null;
    }

    @Override
    public abstract void finish();

    public void initIds() {
        Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
        List<Instance> instancesToInit = NncUtils.filter(instances, inst -> inst.getId() == null);
        if(instancesToInit.isEmpty()) {
            return;
        }
        Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInit, Instance::getType);
        Map<Type,List<Long>> idMap = idGenerator.apply(countMap);
        Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInit, Instance::getType);
        type2instances.forEach((type, instances) -> {
            List<Long> ids = idMap.get(type);
            NncUtils.biForEach(instances, ids, Instance::initId);
        });
        for (Instance instance : instances) {
            instanceMap.put(instance.getId(), instance);
            listeners.forEach(l -> l.onIdInitialized(instance));
        }
    }

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idService.allocate(tenantId, typeId2count);
    }

    @Override
    public void addListener(InstanceSinkListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public abstract Type getType(long id);

    public void remove(Instance instance) {
        boolean removed = instances.remove(instance);
        if(removed) {
            for (Field field : instance.getType().getFields()) {
                if (field.isChildField()) {
                    Instance ref = instance.getInstance(field);
                    if (ref != null) {
                        remove(ref);
                    }
                }
            }
            if(instance instanceof InstanceArray instanceArray) {
                if(instanceArray.isElementAsChild()) {
                    for (Object element : instanceArray.getElements()) {
                        if(element instanceof Instance elementInst) {
                            remove(elementInst);
                        }
                    }
                }
            }
        }
    }
    @Override
    public List<Instance> selectByKey(IndexKeyPO indexKeyPO) {
        return null;
    }

    public void bind(Instance instance) {
        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
        getAllNewInstances(List.of(instance)).forEach(this::add);
    }

    private void add(Instance instance) {
        NncUtils.requireFalse(instance.isValue(), "Can not add a value instance");
        instances.add(instance);
        if(instance.getId() != null) {
            instanceMap.put(instance.getId(), instance);
        }
    }

    @SuppressWarnings("unused")
    public <T> void setAttribute(ContextAttributeKey<T> key, T value) {
        if(key.isNotNull()) {
            requireNonNull(value);
        }
        attributes.put(key, value);
    }

    @Override
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

    private Set<Instance> getAllNewInstances(Collection<Instance> instances) {
        Set<Instance> result = new IdentitySet<>();
        getAllNewInstances(instances, result);
        return result;
    }

    private void getAllNewInstances(Collection<Instance> instances, Set<Instance> result) {
        List<Instance> newInstances = NncUtils.filter(instances,
                inst -> !this.instances.contains(inst) && !result.contains(inst) && !inst.isValue()
        );
        result.addAll(newInstances);
        Set<Instance> refInstances = new IdentitySet<>(NncUtils.flatMap(newInstances, Instance::getRefInstances));
        if(!refInstances.isEmpty()) {
            getAllNewInstances(refInstances, result);
        }
    }

    private boolean isIdInParent(long id) {
        return parent != null && parent.containsId(id);
    }

}
