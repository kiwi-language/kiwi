package tech.metavm.object.instance.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.*;
import tech.metavm.tenant.TenantRT;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.getFirst;
import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable {

    public static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long tenantId;
    protected final EntityIdProvider idService;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    protected final Set<Instance> instances = new IdentitySet<>();
    private final Set<Instance> newInstances = new IdentitySet<>();
    private final Map<Long, Instance> removedInstanceMap = new HashMap<>();
    private final Set<Instance> removed = new HashSet<>();
    protected final IInstanceStore instanceStore;
    private LockMode lockMode = LockMode.NONE;
    protected final Profiler profiler = new Profiler();

    private final IInstanceContext parent;
    private final Set<InstanceIdInitListener> listeners = new LinkedHashSet<>();
    private final Set<Consumer<Instance>> removalListeners = new LinkedHashSet<>();
    private final Set<Consumer<Instance>> initializationListeners = new LinkedHashSet<>();
    private @Nullable Consumer<Object> bindHook;
    private final Map<IndexKeyRT, Set<ClassInstance>> memIndex = new HashMap<>();
    private final Map<ClassInstance, List<IndexKeyRT>> indexKeys = new HashMap<>();
    private final Map<Long, Instance> tmpId2Instance = new HashMap<>();
    private boolean closed;
    private final long profilerLogThreshold;
    private DefContext defContext;

    public BaseInstanceContext(long tenantId,
                               EntityIdProvider idService,
                               IInstanceStore instanceStore,
                               DefContext defContext, IInstanceContext parent, long profilerLogThreshold) {
        this.tenantId = tenantId;
        this.instanceStore = instanceStore;
        this.profilerLogThreshold = profilerLogThreshold;
        this.defContext = defContext;
        this.idService = new WrappedIdProvider(
                this::interceptGetTypeId,
                idService
        );
        this.parent = parent;
    }

    private Long interceptGetTypeId(long id) {
        return id == -1L ? defContext.getType(TenantRT.class).getId() : null;
    }

    @Nullable
    public Consumer<Object> getBindHook() {
        return bindHook;
    }

    public void setBindHook(@Nullable Consumer<Object> bindHook) {
        this.bindHook = bindHook;
    }

    @Override
    public void replace(Collection<Instance> instances) {
        instances = getAllNewInstances(instances);
        if (NncUtils.isEmpty(instances)) {
            return;
        }
        List<Instance> newInstances = NncUtils.filter(instances, inst -> inst.getId() == null);
        List<Instance> persistedInstances = NncUtils.filter(instances, inst -> inst.getId() != null);

        newInstances.forEach(this::add);
        replaceActually(persistedInstances);
    }

    public Profiler getProfiler() {
        if(ContextUtil.isContextAvailable())
            return ContextUtil.getProfiler();
        else
            return profiler;
    }

    @Override
    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    public LockMode getLockMode() {
        return lockMode;
    }

    private void replaceActually(List<Instance> persistedInstances) {
        List<Instance> parentInstances = NncUtils.filter(persistedInstances, inst -> isIdInParent(inst.getIdRequired()));
        List<Instance> selfInstances = NncUtils.exclude(persistedInstances, inst -> isIdInParent(inst.getIdRequired()));

        if (NncUtils.isNotEmpty(parentInstances)) {
            parent.replace(parentInstances);
        }
        onReplace(selfInstances);
        for (Instance instance : selfInstances) {
            Instance existing = instanceMap.get(instance.getId());
            if (existing != null) {
                instances.remove(existing);
            }
            add(instance);
        }
    }

    public long getProfilerLogThreshold() {
        return profilerLogThreshold;
    }

    public DefContext getDefContext() {
        return defContext;
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }

    public void updateMemoryIndex(ClassInstance instance) {
        removeFromMemIndex(instance);
        var keys = instance.getIndexKeys(getEntityContext());
        indexKeys.put(instance, keys);
        for (IndexKeyRT key : keys) {
            memIndex.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(instance);
        }
    }

    private void removeFromMemIndex(ClassInstance instance) {
        var keys = indexKeys.get(instance);
        if (keys != null) {
            for (IndexKeyRT key : keys) {
                memIndex.get(key).remove(instance);
            }
        }
    }

    protected void onReplace(List<Instance> replacements) {
    }

    @Override
    public void preload(Collection<Long> ids, LoadingOption... options) {
    }

    @Override
    public Instance get(RefDTO ref) {
        if (ref.id() != null && ref.id() != 0L) {
            return get(ref.id());
        } else {
            return tmpId2Instance.get(ref.tmpId());
        }
    }

    @Override
    public List<Instance> batchGet(Collection<Long> ids) {
        if (parent == null) {
            return batchGetSelf(ids);
        } else {
            Collection<Long> parentIds = NncUtils.filter(ids, parent::containsId);
            Collection<Long> selfIds = NncUtils.exclude(ids, parent::containsId);
            return NncUtils.union(
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
        if (removedInstanceMap.containsKey(id)) {
            throw new InternalException("Can not get instance " + id + " because it's already removed");
        }
        Instance instance = instanceMap.get(id);
        if (instance == null) {
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
        return parent != null && parent.containsId(id) || instanceMap.get(id) != null
                || removedInstanceMap.get(id) != null;
    }

    @Override
    public final void finish() {
        if (closed) {
            throw new IllegalStateException("Context closed");
        }
        try (var ignored = getProfiler().enter("finish")) {
            finishInternal();
            newInstances.clear();
        }
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Context already closed");
        }
        closed = true;
        for (Instance instance : instances) {
            for (ReferenceRT ref : new ArrayList<>(instance.getOutgoingReferences())) {
                ref.clear();
            }
        }
        if(getProfiler() == profiler) {
            var profilerResult = getProfiler().finish();
            if (profilerLogThreshold != -1L && profilerResult.duration() > profilerLogThreshold) {
                LOGGER.info(profilerResult.output());
            }
        }
    }

    protected abstract void finishInternal();

    public Instance getRemoved(long id) {
        return NncUtils.requireNonNull(removedInstanceMap.get(id));
    }

    protected boolean isRemoved(long id) {
        return removedInstanceMap.containsKey(id);
    }

    @Override
    public abstract boolean isFinished();

    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
            List<Instance> instancesToInitId = NncUtils.filter(instances, inst -> inst.getId() == null);
            if (instancesToInitId.isEmpty()) {
                return;
            }
            Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
            Map<Type, List<Long>> idMap = idGenerator.apply(countMap);
            Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
            Map<Long, Instance> allocatedMap = new HashMap<>();
            type2instances.forEach((type, instances) -> {
                List<Long> ids = idMap.get(type);
                for (Long id : ids) {
                    boolean contains1 = allocatedMap.containsKey(id);
                    if (contains1) {
                        throw new InternalException();
                    }
                    boolean contains = instanceMap.containsKey(id);
                    if (contains) {
                        throw new InternalException();
                    }
                }
                for (Instance instance : instances) {
                    allocatedMap.put(instance.getId(), instance);
                }
                NncUtils.biForEach(instances, ids, Instance::initId);
            });
            for (Instance instance : instancesToInitId) {
                onIdInitialized(instance);
            }
        }
    }

    @Override
    public void initIdManually(Instance instance, long id) {
        NncUtils.requireTrue(instances.contains(instance));
        instance.initId(id);
        onIdInitialized(instance);
    }

    private void onIdInitialized(Instance instance) {
        instanceMap.put(instance.getId(), instance);
        listeners.forEach(l -> l.onIdInitialized(instance));
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
        if (removed.contains(instance)) {
            throw new InternalException("Instance " + instance + " is already removed");
        }
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(Instance instance, Set<Instance> removalBatch) {
        if (!instances.contains(instance)) {
            return;
        }
        if (instance.getParent() != null && !removalBatch.contains(instance.getParent())) {
            switch (instance.getParent()) {
                case ClassInstance classParent -> {
                    var parentField = requireNonNull(instance.getParentField());
                    if (classParent.getField(parentField) == instance) {
                        if (parentField.isNullable()) {
                            classParent.setChild(parentField, nullInstance());
                        } else {
                            throw new BusinessException(
                                    ErrorCode.STRONG_REFS_PREVENT_REMOVAL2,
                                    instance,
                                    classParent
                            );
                        }
                    }
                }
                case ArrayInstance arrayParent -> arrayParent.removeChild(instance);
                case null -> {
                }
                default -> throw new IllegalStateException("Unexpected value: " + instance.getParent());
            }
        }
        Set<ReferenceRT> refsOutsideOfRemoval = NncUtils.filterUnique(
                instance.getIncomingReferences(), r -> !removalBatch.contains(r.source())
        );
        ReferenceRT strongRef;
        if ((strongRef = NncUtils.find(refsOutsideOfRemoval, ReferenceRT::isStrong)) != null) {
            throw BusinessException.strongReferencesPreventRemoval(Map.of(strongRef.source(), instance));
        }
        new ArrayList<>(refsOutsideOfRemoval).forEach(ReferenceRT::remove);
        instances.remove(instance);
        removed.add(instance);
        if (instance.getTmpId() != null) {
            tmpId2Instance.remove(instance.getTmpId());
        }
        if (instance.getId() != null) {
            instanceMap.remove(instance.getId());
            removedInstanceMap.put(instance.getId(), instance);
        }
        if (instance instanceof ClassInstance classInstance) {
            removeFromMemIndex(classInstance);
        }
        for (ReferenceRT ref : new ArrayList<>(instance.getOutgoingReferences())) {
            if (!removalBatch.contains(ref.target())) {
                ref.clear();
            }
        }
        for (Consumer<Instance> removalListener : removalListeners) {
            removalListener.accept(instance);
        }
        createReferenceCleanupJob(instance);
    }

    private NullInstance nullInstance() {
        return new NullInstance((PrimitiveType) getEntityContext().getDefContext().getType(Null.class));
    }

    private Set<Instance> getRemovalBatch(Collection<Instance> instances) {
        Set<Instance> result = new IdentitySet<>();
        for (Instance instance : instances) {
            getRemovalBatch0(instance, result);
        }
        return result;
    }

    @Override
    public void addRemovalListener(Consumer<Instance> listener) {
        removalListeners.add(listener);
    }

    private void getRemovalBatch0(Instance instance, Set<Instance> result) {
        if (result.contains(instance)) {
            return;
        }
        result.add(instance);
        Set<Instance> children = instance.getChildren();
        for (Instance child : children) {
            getRemovalBatch0(child, result);
        }
    }

    private void createReferenceCleanupJob(Instance instance) {
//        if (createJob != null && isPersisted(instance)) {
//            createJob.accept(new ReferenceCleanupTask(
//                    instance.getIdRequired(),
//                    instance.getType().getName(),
//                    instance.getTitle()
//            ));
//        }
    }

    @Override
    public boolean isPersistedInstance(Instance instance) {
        return !isNewInstance(instance);
//        if (instance.getId() == null) {
//            return false;
//        }
//        return isPersisted(instance.getId());
    }

//    protected boolean isPersisted(long id) {
//        return true;
//    }

    public void bind(Instance instance) {
        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
        NncUtils.requireFalse(removed.contains(instance),
                () -> new InternalException("Can not bind instance " + instance + " because it's already removed. " +
                        "See issue 0001"));
        for (Instance inst : getAllNewInstances(List.of(instance))) {
            if (inst.getId() == null) {
                add(inst);
            }
        }
    }

    protected void rebind() {
        try (var entry = getProfiler().enter("rebind")) {
            var newInstances = getAllNewInstances(instances);
            entry.addMessage("numNewInstances", newInstances.size());
            for (Instance inst : newInstances) {
                if (inst.getId() == null && !containsInstance(inst)) {
                    add(inst);
                }
            }
        }
    }

    private void add(Instance instance) {
        NncUtils.requireFalse(instance.getType().isEphemeral(), "Can not bind an ephemeral instance");
        NncUtils.requireFalse(instance.isValue(), "Can not add a value instance");
        NncUtils.requireFalse(removed.contains(instance),
                () -> new InternalException(String.format("Trying to add a removed instance: %d", instance.getId())));
        instances.add(instance);
        if (instance.getTmpId() != null) {
            tmpId2Instance.put(instance.getTmpId(), instance);
        }
        if (instance.getId() != null) {
            instanceMap.put(instance.getId(), instance);
        } else {
            newInstances.add(instance);
        }
    }

    @Override
    public void addInitializationListener(Consumer<Instance> listener) {
        initializationListeners.add(listener);
    }

    protected void onInstanceInitialized(Instance instance) {
        for (Consumer<Instance> listener : initializationListeners) {
            listener.accept(instance);
        }
    }

    @Override
    public boolean isNewInstance(Instance instance) {
        return newInstances.contains(instance);
    }

    @SuppressWarnings("unused")
    public <T> void setAttribute(ContextAttributeKey<T> key, T value) {
        if (key.isNotNull()) {
            requireNonNull(value);
        }
        attributes.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(ContextAttributeKey<T> key) {
        if (!attributes.containsKey(key) && key.getDefaultValueSupplier() != null) {
            T defaultValue = key.getDefaultValueSupplier().get();
            attributes.put(key, defaultValue);
            return defaultValue;
        } else {
            return (T) attributes.get(key);
        }
    }

    private List<Instance> getAllNewInstances(Collection<Instance> instances) {
        try(var entry = getProfiler().enter("getAllNewInstances")) {
            var result = new ArrayList<Instance>();
            var visitor = new GraphVisitor() {
                @Override
                public void visitInstance(Instance instance) {
                    if(!EntityUtils.isModelInitialized(instance))
                        return;
                    if(parent != null && parent.containsInstance(instance))
                        return;
                    if ((instance instanceof ClassInstance || instance instanceof ArrayInstance)
                            && !instance.isValue() && !containsInstance(instance)) {
                        result.add(instance);
                    }
                    super.visitInstance(instance);
                }
            };
            for (Instance instance : instances) {
                if(EntityUtils.isModelInitialized(instance))
                    visitor.visit(instance);
            }
            entry.addMessage("numCalls", visitor.numCalls);
            return result;
        }
    }

    protected void clearStaleReferences(InstancePO instancePO) {
        Type type = getType(instancePO.getTypeId());
        if (type instanceof ClassType classType) {
            clearStaleReferencesForObject(instancePO, classType);
        } else if (type instanceof ArrayType arrayType) {
            clearStaleReferencesForArray((InstanceArrayPO) instancePO, arrayType);
        }
    }

    private void clearStaleReferencesForObject(InstancePO instancePO, ClassType type) {
        for (Field field : type.getAllFields()) {
            boolean fieldIsRef = field.isReference();
            long declaringTypeId = field.getDeclaringType().getIdRequired();
            Object fieldValue = instancePO.get(declaringTypeId, field.getColumnName());
            Long targetId = extractId(fieldValue, fieldIsRef);
            Instance removed;
            if (field.isNotNull() && (removed = removedInstanceMap.get(targetId)) != null) {
                throw BusinessException.strongReferencesPreventRemovalFromPO(Map.of(instancePO, removed));
            }
            if (targetId != null && !checkAlive(targetId)) {
                instancePO.set(declaringTypeId, field.getColumnName(), null);
            }
        }
    }

    private void clearStaleReferencesForArray(InstanceArrayPO arrayPO, ArrayType arrayType) {
        if (arrayType.getElementType().isPrimitive()) {
            return;
        }
        boolean elementIsRef = arrayType.getElementType().isReference();
        List<Object> elements = arrayPO.getElements();
        List<Object> newElements = new ArrayList<>();
        for (Object element : elements) {
            Long targetId = extractId(element, elementIsRef);
            Instance removed;
            if ((removed = removedInstanceMap.get(targetId)) != null) {
                throw BusinessException.strongReferencesPreventRemovalFromPO(Map.of(arrayPO, removed));
            }
            if (targetId != null && checkAlive(targetId)) {
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

    private Long extractId(Object fieldValue, boolean isRef) {
        if (isRef && (fieldValue instanceof Long id)) {
            return id;
        }
        if (fieldValue instanceof IdentityPO identityPO) {
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

    //<editor-fold desc="getByType">
    // TODO loadByType should be implemented by a dedicated buffer.
    //  TODO The caching strategy requires optimization.
    private record LoadByTypeRequest(Type type, Instance startExclusive, long limit) {
    }

    private final Map<LoadByTypeRequest, List<InstancePO>> loadByTypeCache = new HashMap<>();

    private List<InstancePO> loadByType(LoadByTypeRequest request) {
        var cachedResult = loadByTypeCache.get(request);
        if (cachedResult != null)
            return cachedResult;
        var result = instanceStore.queryByTypeIds(
                List.of(
                        new ByTypeQuery(
                                NncUtils.requireNonNull(request.type().getId(), "Type id is not initialized"),
                                request.startExclusive().isNull() ? -1L : request.startExclusive().getIdRequired() + 1L,
                                request.limit()
                        )
                ),
                this
        );
        loadByTypeCache.put(request, result);
        return result;
    }

    @Override
    public List<Instance> getByType(Type type, Instance startExclusive, long limit) {
        return getByType(type, startExclusive, limit, false);
    }

    private List<Instance> getByType(Type type, Instance startExclusive, long limit, boolean persistedOnly) {
        if (startExclusive == null) {
            startExclusive = InstanceUtils.nullInstance();
        }
        List<InstancePO> instancePOs = loadByType(new LoadByTypeRequest(type, startExclusive, limit));
        List<Instance> persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getIdRequired()));
        if (persistedResult.size() >= limit || persistedOnly) {
            return persistedResult;
        }
        Set<Long> persistedIds = NncUtils.mapUnique(persistedResult, Instance::getId);
        var result = NncUtils.union(
                persistedResult,
                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()), persistedIds)
        );
        return result;
    }

    private List<Instance> getByTypeFromBuffer(Type type, Instance startExclusive, int limit, Set<Long> persistedIds) {
        List<Instance> typeInstances = NncUtils.filter(
                new ArrayList<>(instances),
                i -> type.isInstance(i) && !persistedIds.contains(i.getId())
        );
        if (startExclusive.isNull()) {
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        }
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    //</editor-fold>

    @Override
    public List<Instance> scan(Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.scan(List.of(
                        new ScanQuery(startExclusive.isNull() ? 0L : startExclusive.getIdRequired() + 1L, limit)
                ), this),
                instancePO -> get(instancePO.getIdRequired())
        );
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        if (NncUtils.anyMatch(instances, i -> type.isInstance(i) && (!persistedOnly || isPersistedInstance(i)))) {
            return true;
        }
        return type.getId() != null && NncUtils.isNotEmpty(getByType(type, null, 1, persistedOnly));
    }

    @Override
    public List<Instance> selectByKey(IndexKeyRT indexKey) {
//        long indexTenantId = getEntityContext().getTenantId(indexKey.getIndex());
        var tenantIds = NncUtils.deduplicate(List.of(/*indexTenantId,*/ getTenantId()));
        List<Long> instanceIds = instanceStore.selectByKey(tenantIds, indexKey.toPO(), this);
        return NncUtils.map(instanceIds, this::get);
    }

    @Override
    public Instance selectByUniqueKey(IndexKeyRT key) {
        var instances = memIndex.get(key);
        if (NncUtils.isNotEmpty(instances)) return instances.iterator().next();
        return getFirst(selectByKey(key));
    }

    @Override
    public List<Instance> query(InstanceIndexQuery query) {
        List<Long> instanceIds = instanceStore.query(query, this);
        return NncUtils.map(instanceIds, this::get);
    }

    public IInstanceContext getParent() {
        return parent;
    }

    @Override
    public void increaseVersionsForAll() {
        instances.forEach(Instance::incVersion);
    }

}
