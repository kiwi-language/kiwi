package tech.metavm.entity;

import tech.metavm.job.Job;
import tech.metavm.job.ReferenceCleanupJob;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext{
    protected final long tenantId;
    protected final EntityIdProvider idService;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    protected final Set<Instance> instances = new IdentitySet<>();
    private final Map<Long, Instance> removedInstanceMap = new HashMap<>();
    protected final IInstanceStore instanceStore;

    private final IInstanceContext parent;
    private final Set<InstanceIdInitListener> listeners = new LinkedHashSet<>();
    private final Set<Consumer<Instance>> removalListeners = new LinkedHashSet<>();
    private Consumer<Job> createJob;

    public BaseInstanceContext(long tenantId,
                               EntityIdProvider idService,
                               IInstanceStore instanceStore,
                               IInstanceContext parent) {
        this.tenantId = tenantId;
        this.instanceStore = instanceStore;
        this.idService = idService;
        this.parent = parent;
    }

    public void setCreateJob(Consumer<Job> createJob) {
        this.createJob = createJob;
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

    @Override
    public abstract boolean isFinished();

    public void initIds() {
        Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
        List<Instance> instancesToInitId = NncUtils.filter(instances, inst -> inst.getId() == null);
        if(instancesToInitId.isEmpty()) {
            return;
        }
        Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
        Map<Type,List<Long>> idMap = idGenerator.apply(countMap);
        Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
        Map<Long, Instance> allocatedMap = new HashMap<>();
        type2instances.forEach((type, instances) -> {
            List<Long> ids = idMap.get(type);
            for (Long id : ids) {
                boolean contains1 = allocatedMap.containsKey(id);
                if(contains1) {
                    throw new InternalException();
                }
                boolean contains = instanceMap.containsKey(id);
                if(contains) {
                    throw new InternalException();
                }
            }
            for (Instance instance : instances) {
                allocatedMap.put(instance.getId(), instance);
            }
            NncUtils.biForEach(instances, ids, Instance::initId);
        });
        for (Instance instance : instancesToInitId) {
            instanceMap.put(instance.getId(), instance);
            listeners.forEach(l -> l.onIdInitialized(instance));
        }
    }

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idService.allocate(tenantId, typeId2count);
    }

    @Override
    public void addListener(InstanceIdInitListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public abstract Type getType(long id);

    @Override
    public void batchRemove(Collection<Instance> instances) {
        Set<Instance> removalBatch = getRemovalBatch(instances);
        for (Instance toRemove : removalBatch) {
            remove0(toRemove, removalBatch);
        }
    }

    public boolean remove(Instance instance) {
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(Instance instance, Set<Instance> removalBatch) {
        Set<ReferenceRT> refsOutsideOfRemoval = NncUtils.filterUnique(
                instance.getIncomingReferences(), r -> !removalBatch.contains(r.source())
        );
        if(NncUtils.anyMatch(refsOutsideOfRemoval, ReferenceRT::isStrong)) {
            throw BusinessException.strongReferencesPreventRemoval(List.of(instance));
        }
        new ArrayList<>(refsOutsideOfRemoval).forEach(ReferenceRT::remove);
        if(!instances.remove(instance)) {
            return;
        }
        if(instance.getId() != null) {
            instanceMap.remove(instance.getId());
            removedInstanceMap.put(instance.getId(), instance);
        }
        for (ReferenceRT ref : new ArrayList<>(instance.getOutgoingReferences())) {
            if(!removalBatch.contains(ref.target())) {
                ref.clear();
            }
        }
        for (Consumer<Instance> removalListener : removalListeners) {
            removalListener.accept(instance);
        }
        createReferenceCleanupJob(instance);
    }

    private Set<Instance> getRemovalBatch(Collection<Instance> instances) {
        Set<Instance> result = new IdentitySet<>();
        for (Instance instance : instances) {
            getRemovalBatch0(instance, result);
        }
        return result;
    }

    @Override
    public void addRemovalListener(Consumer<Instance> removalListener) {
        removalListeners.add(removalListener);
    }

    private void getRemovalBatch0(Instance instance, Set<Instance> result) {
        if(result.contains(instance)) {
            return;
        }
        result.add(instance);
        Set<Instance> children = instance.getChildren();
        for (Instance child : children) {
            getRemovalBatch0(child, result);
        }
    }

    private void createReferenceCleanupJob(Instance instance) {
        if(createJob != null && isPersisted(instance)) {
            createJob.accept(new ReferenceCleanupJob(
                    instance.getId(),
                    instance.getType().getName(),
                    instance.getTitle()
            ));
        }
    }

    private boolean isPersisted(Instance instance) {
        if(instance.getId() == null) {
            return false;
        }
        return isPersisted(instance.getId());
    }

    protected boolean isPersisted(long id) {
        return true;
    }

    public void bind(Instance instance) {
        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
        for (Instance inst : getAllNewInstances(List.of(instance))) {
            if(inst.getId() == null) {
                add(inst);
            }
        }
    }

    protected void rebind() {
        for (Instance instance : new ArrayList<>(instances)) {
            for (Instance inst : getAllNewInstances(List.of(instance))) {
                if (inst.getId() == null && !containsInstance(inst)) {
                    add(inst);
                }
            }
        }
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
        getAllNewInstances(instances, result, new IdentitySet<>());
        return result;
    }

    private void getAllNewInstances(Collection<Instance> instances, Set<Instance> result, Set<Instance> visited) {
        instances = NncUtils.filterNot(instances, visited::contains);
        visited.addAll(instances);
        List<Instance> newInstances = NncUtils.filter(instances,
                inst -> !containsInstance(inst) && !result.contains(inst) && !inst.isValue()
        );
        result.addAll(newInstances);
        Set<Instance> refInstances = new IdentitySet<>(NncUtils.flatMap(instances, Instance::getRefInstances));
        if(!refInstances.isEmpty()) {
            getAllNewInstances(refInstances, result, visited);
        }
    }

    protected void clearStaleReferences(InstancePO instancePO) {
        Type type = getType(instancePO.getTypeId());
        if(type instanceof ClassType classType) {
            clearStaleRefIdsForObject(instancePO, classType);
        }
        else if(type instanceof ArrayType arrayType) {
            clearStaleRefIdsForArray((InstanceArrayPO) instancePO, arrayType);
        }
    }


    private void clearStaleRefIdsForObject(InstancePO instancePO, ClassType type) {
        for (Field field : type.getFields()) {
            boolean fieldIsRef = field.isReference();
            Object fieldValue = instancePO.get(field.getColumnName());
            Long refId = convertToRefId(fieldValue, fieldIsRef);
            Instance removed;
            if(field.isNotNull() && (removed = removedInstanceMap.get(refId)) != null) {
                throw BusinessException.strongReferencesPreventRemoval(List.of(removed));
            }
            if(refId != null && !checkAlive(refId)) {
                instancePO.set(field.getColumnName(), null);
            }
        }
    }

    private void clearStaleRefIdsForArray(InstanceArrayPO arrayPO, ArrayType arrayType) {
        boolean elementIsRef = arrayType.getElementType().isReference();
        List<Object> elements = arrayPO.getElements();
        List<Object> newElements = new ArrayList<>();
        for (Object element : elements) {
            Long refId = convertToRefId(element, elementIsRef);
            Instance removed;
            if((removed = removedInstanceMap.get(refId)) != null) {
                throw BusinessException.strongReferencesPreventRemoval(List.of(removed));
            }
            if(refId != null && checkAlive(refId)) {
                newElements.add(element);
            }
        }
        arrayPO.setElements(newElements);
    }

    private boolean checkAlive(long id) {
        return !removedInstanceMap.containsKey(id) &&
                (parent != null && parent.containsId(id) || checkAliveInStore(id));
    }

    protected abstract boolean checkAliveInStore(long id);

    private Long convertToRefId(Object fieldValue, boolean isRef) {
        if(isRef && (fieldValue instanceof Long refId)) {
            return refId;
        }
        if(fieldValue instanceof IdentityPO identityPO) {
            return identityPO.id();
        }
        return null;
    }

    private boolean isIdInParent(long id) {
        return parent != null && parent.containsId(id);
    }


    @Override
    public List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.getByReferenceTargetId(
                        targetId,
                        NncUtils.getOrElse(startExclusive, Instance::getId, -1L),
                        limit,
                        this
                ),
                this::get
        );
    }

    @Override
    public List<Instance> getByType(Type type, Instance startExclusive, long limit) {
        if(startExclusive == null) {
            startExclusive = InstanceUtils.nullInstance();
        }
        List<InstancePO> instancePOs = instanceStore.getByTypeIds(
                List.of(NncUtils.requireNonNull(type.getId(), "Type id is not initialized")),
                startExclusive.isNull() ? -1L : startExclusive.getId(),
                limit,
                this
        );
        List<Instance> persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getId()));
        if(persistedResult.size() >= limit) {
            return persistedResult;
        }
        return NncUtils.merge(
                persistedResult,
                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()))
        );
    }

    public List<Instance> getByTypeFromBuffer(Type type, Instance startExclusive, int limit) {
        List<Instance> typeInstances = NncUtils.filter(
                instances,
                type::isInstance
        );
        if(startExclusive.isNull()) {
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        }
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    @Override
    public boolean existsInstances(Type type) {
        return NncUtils.isNotEmpty(getByType(type, null, 1));
    }

    @Override
    public List<Instance> selectByKey(IndexKeyPO key) {
        List<Long> instanceIds = instanceStore.selectByKey(key, this);
        return NncUtils.map(instanceIds, this::get);
    }

    @Override
    public List<Instance> query(InstanceIndexQuery query) {
        List<Long> instanceIds = instanceStore.query(query, this);
        return NncUtils.map(instanceIds, this::get);
    }

}
