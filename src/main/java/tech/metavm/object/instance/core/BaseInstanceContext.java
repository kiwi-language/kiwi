package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.object.type.Type;
import tech.metavm.tenant.TenantRT;
import tech.metavm.util.*;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static tech.metavm.util.NncUtils.getFirst;
import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable, Iterable<Instance> {

    public static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long tenantId;
    protected final EntityIdProvider idService;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    private Instance head;
    private Instance tail;
    protected final IInstanceStore instanceStore;
    private LockMode lockMode = LockMode.NONE;
    protected final Profiler profiler = ContextUtil.getProfiler();

    protected final IInstanceContext parent;
    private final Set<InstanceIdInitListener> listeners = new LinkedHashSet<>();
    private final Set<Consumer<Instance>> removalListeners = new LinkedHashSet<>();
    private final Set<Consumer<Instance>> initializationListeners = new LinkedHashSet<>();
    private @Nullable Consumer<Object> bindHook;
    private final Map<IndexKeyRT, Set<ClassInstance>> memIndex = new HashMap<>();
    private final Map<ClassInstance, List<IndexKeyRT>> indexKeys = new HashMap<>();
    private final Map<Long, Instance> tmpId2Instance = new HashMap<>();
    private boolean closed;
    private final boolean readonly;
    private final String clientId = ContextUtil.getClientId();
    private DefContext defContext;

    public BaseInstanceContext(long tenantId,
                               EntityIdProvider idService,
                               IInstanceStore instanceStore,
                               DefContext defContext, IInstanceContext parent, boolean readonly) {
        this.tenantId = tenantId;
        this.instanceStore = instanceStore;
        this.defContext = defContext;
        this.readonly = readonly;
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
        instances = crawNewInstances(instances);
        if (NncUtils.isEmpty(instances)) {
            return;
        }
        List<Instance> newInstances = NncUtils.filter(instances, inst -> inst.getId() == null);
        List<Instance> persistedInstances = NncUtils.filter(instances, inst -> inst.getId() != null);
        newInstances.forEach(this::add);
        replaceActually(persistedInstances);
    }

    public Profiler getProfiler() {
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
        if (NncUtils.isNotEmpty(parentInstances))
            parent.replace(parentInstances);
        onReplace(selfInstances);
        for (Instance instance : selfInstances) {
            Instance existing = instanceMap.get(instance.getId());
            if (existing != null)
                evict(existing);
            add(instance);
        }
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    private void evict(Instance instance) {
        if (instance == head)
            head = instance.getNext();
        if (instance == tail)
            tail = instance.getPrev();
        Long id = instance.getId();
        if (id != null)
            instanceMap.remove(id);
        instance.setContext(null);
        instance.unlink();
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
    public Instance get(RefDTO ref) {
        if (ref.isEmpty())
            return null;
        if (ref.isPersisted())
            return get(ref.id());
        else {
            var found = getIfPresentByTmpId(ref.tmpId());
            if (found == null && parent != null)
                found = parent.getIfPresentByTmpId(ref.tmpId());
            if (found != null) {
                if (found.isRemoved())
                    throw new InternalException(
                            String.format("Can not get instance '%s' because it's already removed", found));
                return found;
            } else
                return null;
        }
    }

    @Override
    public boolean containsRef(RefDTO ref) {
        return ref.isPersisted() ? instanceMap.containsKey(ref.id()) : tmpId2Instance.containsKey(ref.tmpId())
                || parent != null && parent.containsRef(ref);
    }

    @Override
    public List<Instance> batchGet(Collection<Long> ids) {
        return NncUtils.map(ids, this::get);
    }

    @Override
    public Instance get(long id) {
        var found = getBuffered(id);
        if (found != null) {
            if (found.isRemoved())
                throw new InternalException(
                        String.format("Can not get instance '%s' because it's already removed", found));
            return found;
        } else {
            buffer(id);
            return add(id);
        }
    }

    @Override
    public Instance internalGet(long id) {
        var found = getBuffered(id);
        if (found != null)
            return found;
        else {
            return add(id);
        }
    }

    @Override
    public @Nullable Instance getBuffered(long id) {
        var found = instanceMap.get(id);
        if (found == null && parent != null)
            found = parent.getBuffered(id);
        return found;
    }

    protected Instance getSelfBuffered(long id) {
        return instanceMap.get(id);
    }

    @Override
    public List<Long> filterAlive(List<Long> ids) {
        buffer(ids);
        return NncUtils.filter(ids, this::isAlive);
    }

    @Override
    public final boolean isAlive(long id) {
        if(parent != null && parent.containsId(id))
            return parent.isAlive(id);
        var instance = getSelfBuffered(id);
        if(instance != null) {
            if(instance.isRemoved())
                return false;
            if(instance.isLoaded())
                return true;
        }
        return checkAliveInStore(id);
    }

    protected abstract boolean checkAliveInStore(long id);

    @Override
    public Instance getIfPresentByTmpId(long tmpId) {
        return tmpId2Instance.get(tmpId);
    }

    private Instance add(long id) {
        var instance = allocateInstance(id);
        add(instance);
        return instance;
    }

    protected abstract Instance allocateInstance(long id);

    @Override
    public abstract IEntityContext getEntityContext();

    @Override
    public boolean containsInstance(Instance instance) {
        return instance.getContext() == this || parent != null && parent.containsInstance(instance);
    }

    @Override
    public boolean containsId(long id) {
        return instanceMap.containsKey(id) || parent != null && parent.containsId(id);
    }

    @Override
    public final void finish() {
        if (closed)
            throw new IllegalStateException("Context closed");
        if (readonly)
            throw new IllegalStateException("Can not finish a readonly context");
        try (var ignored = getProfiler().enter("finish")) {
            finishInternal();
            for (Instance instance : this) {
                if (instance.isNew())
                    instance.setLoaded(false);
            }
        }
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Context already closed");
        }
        closed = true;
    }

    protected abstract void finishInternal();

    public Instance getRemoved(long id) {
        var instance = NncUtils.requireNonNull(instanceMap.get(id));
        NncUtils.requireTrue(instance.isRemoved());
        return instance;
    }

    protected boolean isRemoved(long id) {
        var instance = instanceMap.get(id);
        return instance != null && instance.isRemoved();
    }

    @Override
    public abstract boolean isFinished();

    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
            List<Instance> instancesToInitId = NncUtils.filter(this, Instance::isIdNull);
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
        NncUtils.requireTrue(instance.getContext() == this);
        instance.initId(id);
        onIdInitialized(instance);
    }

    private void onIdInitialized(Instance instance) {
        instanceMap.put(instance.getId(), instance);
        for (InstanceIdInitListener listener : listeners) {
            listener.onIdInitialized(instance);
        }
//        listeners.forEach(l -> l.onIdInitialized(instance));
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
        Map<Instance, List<ReferenceRT>> referencesByTarget = new HashMap<>();
        for (Instance instance : instances) {
            for (ReferenceRT ref : instance.getOutgoingReferences()) {
                referencesByTarget.computeIfAbsent(ref.target(), k -> new ArrayList<>()).add(ref);
            }
        }
        for (Instance toRemove : removalBatch) {
            remove0(toRemove, removalBatch, referencesByTarget);
        }
    }

    public boolean remove(Instance instance) {
        if (instance.isRemoved()) {
            throw new InternalException("Instance " + instance + " is already removed");
        }
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(Instance instance, Set<Instance> removalBatch, Map<Instance, List<ReferenceRT>> referencesByTarget) {
        if (instance.isRemoved()) {
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
                                    instance.getQualifiedTitle(),
                                    classParent.getQualifiedTitle()
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
                referencesByTarget.get(instance), ref -> !removalBatch.contains(ref.source())
        );
        ReferenceRT strongRef;
        if ((strongRef = NncUtils.find(refsOutsideOfRemoval, ReferenceRT::isStrong)) != null) {
            throw BusinessException.strongReferencesPreventRemoval(strongRef.source(), instance);
        }
        new ArrayList<>(refsOutsideOfRemoval).forEach(ReferenceRT::remove);
        instance.setRemoved();
        if (instance instanceof ClassInstance classInstance) {
            removeFromMemIndex(classInstance);
        }
        for (ReferenceRT ref : new ArrayList<>(instance.getOutgoingReferences())) {
            if (!removalBatch.contains(ref.target()))
                ref.clear();
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

    @SuppressWarnings({"unused", "CommentedOutCode"})
    private void createReferenceCleanupJob(Instance instance) {
//        if (createJob != null && isPersisted(instance)) {
//            createJob.accept(new ReferenceCleanupTask(
//                    instance.getIdRequired(),
//                    instance.getType().getName(),
//                    instance.getTitle()
//            ));
//        }
    }

    public void bind(Instance instance) {
        NncUtils.requireFalse(instance.getType().isEphemeral(), "Can not bind an ephemeral instance");
        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
        NncUtils.requireNull(instance.getId(), "Can not bind a persisted instance");
        NncUtils.requireFalse(instance.isRemoved(),
                () -> new InternalException("Can not bind instance " + instance + " because it's already removed. " +
                        "See issue 0001"));
        for (Instance inst : crawNewInstances(List.of(instance))) {
            if (inst.getId() == null) {
                add(inst);
            }
        }
    }

    protected void craw() {
        try (var entry = getProfiler().enter("craw")) {
            var newInstances = crawNewInstances(this);
            entry.addMessage("numNewInstances", newInstances.size());
            for (Instance inst : newInstances) {
                if (inst.getId() == null && !containsInstance(inst)) {
                    add(inst);
                }
            }
        }
    }

    private void add(Instance instance) {
        NncUtils.requireFalse(instance.isRemoved(),
                () -> new InternalException(String.format("Can not add a removed instance: %d", instance.getId())));
        NncUtils.requireTrue(instance.getContext() == null
                && instance.getNext() == null && instance.getPrev() == null);
        if (tail == null)
            head = tail = instance;
        else {
            tail.insertAfter(instance);
            tail = instance;
        }
        instance.setContext(this);
        if (instance.getTmpId() != null) {
            tmpId2Instance.put(instance.getTmpId(), instance);
        }
        if (instance.getId() != null) {
            instanceMap.put(instance.getId(), instance);
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

    private List<Instance> crawNewInstances(Iterable<Instance> instances) {
        try (var entry = getProfiler().enter("getAllNewInstances")) {
            var result = new ArrayList<Instance>();
            var visitor = new GraphVisitor() {
                @Override
                public void visitInstance(Instance instance) {
                    if (!instance.isInitialized())
                        return;
                    if (parent != null && parent.containsInstance(instance))
                        return;
                    if ((instance instanceof ClassInstance || instance instanceof ArrayInstance)
                            && !containsInstance(instance)) {
                        result.add(instance);
                    }
                    super.visitInstance(instance);
                }
            };
            for (Instance instance : instances) {
                if (instance.isInitialized())
                    visitor.visit(instance);
            }
            entry.addMessage("numCalls", visitor.numCalls);
            return result;
        }
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
        List<Instance> persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getId()));
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
                this,
                i -> type == i.getType() && !persistedIds.contains(i.getId())
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
                instancePO -> get(instancePO.getId())
        );
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        if (NncUtils.anyMatch(this, i -> type.isInstance(i) && (!persistedOnly || i.isPersisted()))) {
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
        for (Instance instance : this)
            instance.incVersion();
    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        return new Iterator<>() {

            Instance current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Instance next() {
                var i = current;
                current = i.getNext();
                return i;
            }
        };
    }
}
