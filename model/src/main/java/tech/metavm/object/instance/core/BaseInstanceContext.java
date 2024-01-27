package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.ContextAttributeKey;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.LockMode;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.*;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable, Iterable<DurableInstance> {

    public static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long appId;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Id, DurableInstance> instanceMap = new HashMap<>();
    private final Set<Id> creatingIds = new HashSet<>();
    private final IdentityHashMap<DurableInstance, List<DurableInstance>> source2views = new IdentityHashMap<>();
    private DurableInstance head;
    private DurableInstance tail;
    private LockMode lockMode = LockMode.NONE;
    protected final Profiler profiler = ContextUtil.getProfiler();

    protected final IInstanceContext parent;
    private final Set<ContextListener> listeners = new LinkedHashSet<>();
    @Nullable
    private Consumer<Object> bindHook;
    //    private final Map<IndexKeyRT, Set<ClassInstance>> memIndex = new HashMap<>();
//    private final Map<ClassInstance, Set<IndexKeyRT>> indexKeys = new HashMap<>();
    private final InstanceMemoryIndex memIndex;
    //    private final Map<Long, Instance> tmpId2Instance = new HashMap<>();
    private boolean closed;
    private final boolean readonly;
    private final String clientId = ContextUtil.getClientId();
    private final TypeProvider typeProvider;
    private final IndexSource indexSource;
    private final MappingProvider mappingProvider;
    private final ParameterizedFlowProvider parameterizedFlowProvider;

    public BaseInstanceContext(long appId,
                               IInstanceContext parent,
                               boolean readonly,
                               IndexSource indexSource,
                               TypeProvider typeProvider,
                               MappingProvider mappingProvider,
                               ParameterizedFlowProvider parameterizedFlowProvider) {
        this.appId = appId;
        this.readonly = readonly;
        this.parent = parent;
        this.indexSource = indexSource;
        this.typeProvider = typeProvider;
        this.mappingProvider = mappingProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        memIndex = new InstanceMemoryIndex(parameterizedFlowProvider);
    }


    @Nullable
    public Consumer<Object> getBindHook() {
        return bindHook;
    }

    public void setBindHook(@Nullable Consumer<Object> bindHook) {
        this.bindHook = bindHook;
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

    @Override
    public List<ClassInstance> query(InstanceIndexQuery query) {
        var memResults = memIndex.query(query);
        if (query.memoryOnly())
            return memResults;
        var storeResults = NncUtils.map(indexSource.query(query, this), id -> (ClassInstance) get(new PhysicalId(id)));
        return Instances.merge(memResults, storeResults, query.desc(), NncUtils.orElse(query.limit(), -1L));
    }

    @Override
    public long count(InstanceIndexQuery query) {
        return indexSource.count(query, this);
    }


//    @Override
//    public Instance get(InstanceId id) {
//        return switch (id) {
//            case PhysicalInstanceId durableId -> get(durableId.getId());
//            case ViewInstanceId viewId -> getView(viewId);
//            case TmpInstanceId tmpId -> getByTmpId(tmpId.getTmpId());
//            default -> throw new IllegalStateException("Unexpected value: " + id);
//        };
//    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    private void evict(DurableInstance instance) {
        if (instance == head)
            head = instance.getNext();
        if (instance == tail)
            tail = instance.getPrev();
        var id = instance.getId();
        if (id != null)
            instanceMap.remove(id);
        instance.setContext(null);
        instance.unlink();
    }

    public void updateMemoryIndex(ClassInstance instance) {
        memIndex.save(instance);
//        removeFromMemIndex(instance);
//        var keys = instance.getIndexKeys(parameterizedFlowProvider);
//        indexKeys.put(instance, keys);
//        for (IndexKeyRT key : keys) {
//            memIndex.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(instance);
//        }
    }

    private void removeFromMemIndex(ClassInstance instance) {
        memIndex.remove(instance);
//        var keys = indexKeys.get(instance);
//        if (keys != null) {
//            for (IndexKeyRT key : keys) {
//                memIndex.get(key).remove(instance);
//            }
//        }
    }

    protected void onReplace(List<DurableInstance> replacements) {
    }

//    @Override
//    public Instance get(RefDTO ref) {
//        if (ref.isEmpty())
//            return null;
//        if (ref.isPersisted())
//            return get(ref.id());
//        else
//            return getByTmpId(ref.tmpId());
//    }

//    private Instance getByTmpId(long tmpId) {
//        var found = getIfPresentByTmpId(tmpId);
//        if (found == null && parent != null)
//            found = parent.getIfPresentByTmpId(tmpId);
//        if (found != null) {
//            if (found.isRemoved())
//                throw new InternalException(
//                        String.format("Can not get instance '%s' because it's already removed", found));
//            return found;
//        } else
//            return null;
//    }

    @Override
    public boolean contains(Id id) {
        return instanceMap.containsKey(id) || parent != null && parent.contains(id);
//        return id.isPersisted() ? instanceMap.containsKey(id.id()) : tmpId2Instance.containsKey(id.tmpId())
//                || parent != null && parent.containsRef(id);
    }

    @Override
    public List<DurableInstance> batchGet(Collection<Id> ids) {
        return NncUtils.map(ids, this::get);
    }

    @Override
    public DurableInstance get(Id id) {
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
    public DurableInstance internalGet(Id id) {
        var found = getBuffered(id);
        if (found != null)
            return found;
        else {
            return add(id);
        }
    }

    @Override
    public @Nullable DurableInstance getBuffered(Id id) {
        var found = instanceMap.get(id);
        if (found == null && parent != null)
            found = parent.getBuffered(id);
        return found;
    }

    protected DurableInstance getSelfBuffered(Id id) {
        return instanceMap.get(id);
    }

    @Override
    public List<Long> filterAlive(List<Long> ids) {
        buffer(NncUtils.map(ids, PhysicalId::of));
        return NncUtils.filter(ids, this::isAlive);
    }

    @Override
    public final boolean isAlive(long id) {
        if (parent != null && parent.containsId(id))
            return parent.isAlive(id);
        var instance = getSelfBuffered(PhysicalId.of(id));
        if (instance != null) {
            if (instance.isRemoved())
                return false;
            if (instance.isLoaded())
                return true;
        }
        return checkAliveInStore(id);
    }

    @Override
    public ParameterizedFlowProvider getParameterizedFlowProvider() {
        return parameterizedFlowProvider;
    }

    private DurableInstance allocateView(ViewId viewId) {
        var viewType = mappingProvider.getMapping(viewId.getMappingId()).getTargetType();
        DurableInstance view;
        if(viewType instanceof ClassType classViewType) {
            view = ClassInstanceBuilder.newBuilder(classViewType)
                    .sourceRef(viewId.getSourceRef(this, mappingProvider))
                    .load(this::initializeView)
                    .id(viewId)
                    .ephemeral(true)
                    .build();
        }
        else if(viewType instanceof ArrayType arrayViewType) {
            view = new ArrayInstance(viewId, arrayViewType, true, this::initializeView);
            view.setSourceRef(viewId.getSourceRef(this, mappingProvider));
        }
        else
            throw new InternalException("Invalid view type: " + viewType);
//        source2views.computeIfAbsent(view.tryGetSource(), k -> new ArrayList<>()).add(view);
        return view;
    }

    private void initializeView(DurableInstance view) {
        var id = (ViewId) requireNonNull(view.getId());
        view = internalGet(id.getRootId());
        var mapping = mappingProvider.getMapping(view.getMappingId());
        var r = mapping.mapRoot(view.getSource(), this, parameterizedFlowProvider);
        r.accept(new InstanceCopier(r) {
            @Override
            protected Instance getExisting(Instance instance) {
                var id = instance.getId();
                if (id != null) {
                    var existing = internalGet(id);
                    existing.setLoaded(requireNonNull(((DurableInstance) instance).tryGetSource()).isLoadedFromCache());
                    return existing;
                } else
                    return null;
            }
        });
    }

    private long getTypeId(Id id) {
        return switch (id) {
            case PhysicalId physicalId -> getTypeId(physicalId.getId());
            case ViewId viewId -> mappingProvider.getMapping(viewId.getMappingId()).getTargetType().tryGetId();
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
    }

    protected abstract long getTypeId(long id);

    protected abstract void initializeInstance(DurableInstance instance);

    protected abstract boolean checkAliveInStore(long id);

//    @Override
//    public Instance getIfPresentByTmpId(long tmpId) {
//        return tmpId2Instance.get(tmpId);
//    }

    private DurableInstance add(Id id) {
        var instance = allocateInstance(id);
        add(instance);
        return instance;
    }

    protected DurableInstance allocateInstance(Id id) {
        Type type = getType(getTypeId(id));
        if (type instanceof ArrayType arrayType) {
            return switch (id) {
                case PhysicalId physicalId ->
                        new ArrayInstance(physicalId, arrayType, false, this::initializeInstance);
                case ViewId viewId -> allocateView(viewId);
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        } else {
            return switch (id) {
                case PhysicalId physicalId ->
                        new ClassInstance(physicalId, (ClassType) type, false, this::initializeInstance);
                case ViewId viewId -> allocateView(viewId);
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }
    }

    @Override
    public TypeProvider getTypeProvider() {
        return typeProvider;
    }

    @Override
    public MappingProvider getMappingProvider() {
        return mappingProvider;
    }

    @Override
    public boolean containsInstance(DurableInstance instance) {
        return instance.getContext() == this || parent != null && parent.containsInstance(instance);
    }

    @Override
    public boolean containsId(long id) {
        return instanceMap.containsKey(PhysicalId.of(id)) || parent != null && parent.containsId(id);
    }

    @Override
    public final void finish() {
        if (closed)
            throw new IllegalStateException("Context closed");
        if (readonly)
            throw new IllegalStateException("Can not finish a readonly context");
        for (ContextListener listener : listeners) {
            listener.beforeFinish();
        }
        try (var ignored = getProfiler().enter("finish")) {
            finishInternal();
            for (var instance : this) {
                if (instance.isNew())
                    instance.setLoaded(false);
            }
        }
    }

    protected void saveViews() {
        for (Instance value : instanceMap.values()) {
            if (value instanceof ClassInstance classInstance) {
                if (classInstance.isView() && !classInstance.isRemoved())
                    saveView(classInstance);
            }
        }
    }

    private void saveView(ClassInstance view) {
        var mapping = mappingProvider.getMapping(view.getMappingId());
        mapping.unmap(view, this, parameterizedFlowProvider);
    }

    @Override
    public void close() {
        if (closed)
            throw new IllegalStateException("Context already closed");
        closed = true;
    }

    protected abstract void finishInternal();

    public DurableInstance getRemoved(long id) {
        var instance = NncUtils.requireNonNull(instanceMap.get(PhysicalId.of(id)));
        NncUtils.requireTrue(instance.isRemoved());
        return instance;
    }

    protected boolean isRemoved(long id) {
        var instance = instanceMap.get(PhysicalId.of(id));
        return instance != null && instance.isRemoved();
    }

    @Override
    public abstract boolean isFinished();


    @Override
    public void initIdManually(DurableInstance instance, long id) {
        NncUtils.requireTrue(instance.getContext() == this);
        instance.initId(PhysicalId.of(id));
        onIdInitialized(instance);
    }

    protected void onIdInitialized(DurableInstance instance) {
        instanceMap.put(instance.getId(), instance);
        listeners.forEach(l -> l.onInstanceIdInit(instance));
        forEachView(instance, v ->
                v.initId(new DefaultViewId(v.getSourceRef().mapping().getId(), v.getSource().getId())));
    }

    protected void onContextInitializeId() {
        for (ContextListener listener : listeners) {
            listener.afterContextIntIds();
        }
    }

    protected boolean onChange(Instance instance) {
        boolean anyChange = false;
        for (ContextListener listener : listeners) {
            if (listener.onChange(instance))
                anyChange = true;
        }
        return anyChange;
    }

    protected void onPatchBuild() {
        for (ContextListener listener : listeners) {
            listener.onPatchBuild();
        }
    }

    @Override
    public void addListener(ContextListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getAppId() {
        return appId;
    }

    @Override
    public Type getType(long id) {
        return typeProvider.getType(id);
    }

    @Override
    public void batchRemove(Collection<DurableInstance> instances) {
        var removalBatch = getRemovalBatch(instances);
        var referencesByTarget = new HashMap<DurableInstance, List<ReferenceRT>>();
        instances.forEach(i -> referencesByTarget.put(i, new ArrayList<>()));
        for (var instance : instanceMap.values()) {
            for (ReferenceRT ref : instance.getOutgoingReferences()) {
                var refs = referencesByTarget.get(ref.target());
                if (refs != null)
                    refs.add(ref);
            }
        }
        // remove views first otherwise uninitialized views in the removal batch may fail to initialize
        var sortedRemovalBatch = NncUtils.sort(removalBatch, Comparator.comparingInt(i -> i.isView() ? 0 : 1));
        for (var toRemove : sortedRemovalBatch) {
            remove0(toRemove, removalBatch, referencesByTarget);
        }
    }

    public boolean remove(DurableInstance instance) {
        if (instance.isRemoved())
            throw new InternalException("Instance " + instance + " is already removed");
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(DurableInstance instance, Set<DurableInstance> removalBatch, Map<DurableInstance, List<ReferenceRT>> referencesByTarget) {
        if (instance.isRemoved())
            return;
        if (instance.getParent() != null && !removalBatch.contains(instance.getParent())) {
            switch (instance.getParent()) {
                case ClassInstance classParent -> {
                    var parentField = requireNonNull(instance.getParentField());
                    if (classParent.getField(parentField) == instance) {
                        if (parentField.isNullable())
                            classParent.setField(parentField, Instances.nullInstance());
                        else {
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
//        Set<ReferenceRT> refsOutsideOfRemoval = NncUtils.filterUnique(
//                referencesByTarget.get(instance), ref -> !removalBatch.contains(ref.source())
//        );
//        ReferenceRT strongRef;
//        if ((strongRef = NncUtils.find(refsOutsideOfRemoval, ReferenceRT::isStrong)) != null)
//            throw BusinessException.strongReferencesPreventRemoval(strongRef.source(), instance);
//        new ArrayList<>(refsOutsideOfRemoval).forEach(ReferenceRT::remove);
        instance.setRemoved();
        if (instance instanceof ClassInstance classInstance)
            removeFromMemIndex(classInstance);
        for (ReferenceRT ref : new ArrayList<>(instance.getOutgoingReferences())) {
            if (!removalBatch.contains(ref.target()))
                ref.clear();
        }
        for (var listener : listeners) {
            listener.onInstanceRemoved(instance);
        }
        createReferenceCleanupJob(instance);
    }

    private Set<DurableInstance> getRemovalBatch(Collection<DurableInstance> instances) {
        var result = new IdentitySet<DurableInstance>();
        var visitor = new InstanceVisitor<Boolean>() {

            @Override
            public Boolean visitClassInstance(ClassInstance instance) {
                if (instance.isView())
                    return Objects.requireNonNull(instance.tryGetSource()).accept(this);
                else {
                    if (super.visitClassInstance(instance)) {
                        forEachView(instance, result::add);
                        return true;
                    } else
                        return false;
                }
            }

            @Override
            public Boolean visitInstance(Instance instance) {
                return false;
            }

            @Override
            public Boolean visitDurableInstance(DurableInstance instance) {
                if (result.add(instance)) {
                    instance.acceptChildren(this);
                    return true;
                } else
                    return false;
            }

            @Override
            public Boolean visitNullInstance(NullInstance instance) {
                return false;
            }
        };
        instances.forEach(visitor::visit);
        return result;
    }

    private void forEachView(DurableInstance instance, Consumer<DurableInstance> action) {
        if (instance.isView())
            action.accept(instance);
        var views = source2views.get(instance);
        if (views != null)
            views.forEach(v -> forEachView(v, action));
    }

    private void getRemovalBatch0(DurableInstance instance, IdentitySet<Instance> result) {
        if (result.contains(instance))
            return;
        result.add(instance);
        var children = instance.getChildren();
        for (var child : children) {
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

    @Override
    public void batchBind(Collection<DurableInstance> instances) {
        instances.forEach(this::checkForBind);
        for (var inst : crawNewInstances(instances)) {
            if (inst.tryGetPhysicalId() == null)
                add(inst);
        }
    }

    private void checkForBind(DurableInstance instance) {
        //        NncUtils.requireFalse(instance.isEphemeral(), "Can not bind an ephemeral instance");
        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
        NncUtils.requireNull(instance.tryGetPhysicalId(), "Can not bind a persisted instance");
        NncUtils.requireFalse(instance.isRemoved(),
                () -> new InternalException("Can not bind instance " + instance + " because it's already removed. " +
                        "See issue 0001"));
    }

    protected void craw() {
        try (var entry = getProfiler().enter("craw")) {
            var newInstances = crawNewInstances(this);
            entry.addMessage("numNewInstances", newInstances.size());
            for (var inst : newInstances) {
                if (inst.tryGetPhysicalId() == null && !containsInstance(inst))
                    add(inst);
            }
        }
    }

    private void add(DurableInstance instance) {
        NncUtils.requireFalse(instance.isRemoved(),
                () -> new InternalException(String.format("Can not add a removed instance: %d", instance.tryGetPhysicalId())));
        NncUtils.requireTrue(instance.getContext() == null
                && instance.getNext() == null && instance.getPrev() == null);
        if (tail == null)
            head = tail = instance;
        else {
            tail.insertAfter(instance);
            tail = instance;
        }
        instance.setContext(this);
        if(instance.isView())
            source2views.computeIfAbsent(instance.getSource(), k -> new ArrayList<>()).add(instance);
        if(instance.getId() != null) {
            if (instanceMap.put(instance.getId(), instance) != null)
                LOGGER.warn("Duplicate instance add to context: " + instance.getId());
        }
//        if (instance.getTmpId() != null) {
//            tmpId2Instance.put(instance.getTmpId(), instance);
//        }
//        if (instance.getId() != null) {
//            instanceMap.put(instance.getId(), instance);
//        }
    }

    protected void onInstanceInitialized(DurableInstance instance) {
        for (var listener : listeners) {
            listener.onInstanceInitialized(instance);
        }
    }

    @SuppressWarnings("unused")
    public <T> void setAttribute(ContextAttributeKey<T> key, T value) {
        if (key.isNotNull())
            requireNonNull(value);
        attributes.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(ContextAttributeKey<T> key) {
        if (!attributes.containsKey(key) && key.getDefaultValueSupplier() != null) {
            T defaultValue = key.getDefaultValueSupplier().get();
            attributes.put(key, defaultValue);
            return defaultValue;
        } else
            return (T) attributes.get(key);
    }

    private List<DurableInstance> crawNewInstances(Iterable<DurableInstance> instances) {
        try (var entry = getProfiler().enter("crawNewInstances")) {
            var result = new ArrayList<DurableInstance>();
            var visitor = new GraphVisitor() {

                @Override
                public Void visitDurableInstance(DurableInstance instance) {
                    if (instance.isInitialized()) {
                        if (instance.isRemoved())
                            return null;
//                            throw new InternalException("Can not reference a removed instance: " + instance);
                        if (parent != null && parent.containsInstance(instance))
                            return null;
                        if (!containsInstance(instance))
                            result.add(instance);
                        super.visitDurableInstance(instance);
                    }
                    return null;
                }

            };
            for (var instance : instances) {
                if (instance.isInitialized() && !instance.isRemoved())
                    visitor.visit(instance);
            }
            entry.addMessage("numCalls", visitor.numCalls);
            return result;
        }
    }

    private boolean isIdInParent(long id) {
        return parent != null && parent.containsId(id);
    }


    //<editor-fold desc="getByType">

    //</editor-fold>

    @Override
    public List<ClassInstance> selectByKey(IndexKeyRT indexKey) {
        return query(indexKey.toQuery());
    }

    @Override
    public DurableInstance selectFirstByKey(IndexKeyRT key) {
//        NncUtils.requireTrue(key.getIndex().isUnique());
//        var instances = memIndex.get(key);
//        if (NncUtils.isNotEmpty(instances)) return instances.iterator().next();
        return NncUtils.first(selectByKey(key));
    }

    public IInstanceContext getParent() {
        return parent;
    }

    @Override
    public void increaseVersionsForAll() {
        for (var instance : this)
            instance.incVersion();
    }

    @NotNull
    @Override
    public Iterator<DurableInstance> iterator() {
        return new Iterator<>() {

            DurableInstance current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public DurableInstance next() {
                var i = current;
                current = i.getNext();
                return i;
            }
        };
    }
}
