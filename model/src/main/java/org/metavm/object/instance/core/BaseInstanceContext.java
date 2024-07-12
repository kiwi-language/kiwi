package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ContextAttributeKey;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.LockMode;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

import static org.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable, Iterable<DurableInstance>, CallContext {

    public static final Logger logger = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long appId;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Id, DurableInstance> instanceMap = new HashMap<>();
    private final IdentityHashMap<DurableInstance, List<DurableInstance>> source2views = new IdentityHashMap<>();
    private DurableInstance head;
    private DurableInstance tail;
    private LockMode lockMode = LockMode.NONE;
    protected final Profiler profiler = ContextUtil.getProfiler();

    protected final IInstanceContext parent;
    private final Set<ContextListener> listeners = new LinkedHashSet<>();
    @Nullable
    private Consumer<Object> bindHook;
    private final InstanceMemoryIndex memIndex;
    private boolean closed;
    private final boolean readonly;
    private final String clientId = ContextUtil.getClientId();
    private final TypeDefProvider typeDefProvider;
    private final IndexSource indexSource;
    private final MappingProvider mappingProvider;
    private int seq;

    public BaseInstanceContext(long appId,
                               IInstanceContext parent,
                               boolean readonly,
                               IndexSource indexSource,
                               TypeDefProvider typeDefProvider,
                               MappingProvider mappingProvider) {
        this.appId = appId;
        this.readonly = readonly;
        this.parent = parent;
        this.indexSource = indexSource;
//        this.typeProvider = typeProvider;
        this.typeDefProvider = typeDefProvider;
        this.mappingProvider = mappingProvider;
        memIndex = new InstanceMemoryIndex();
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
    public List<InstanceReference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        return NncUtils.map(indexSource.scan(from, to, this), this::createReference);
    }

    public InstanceReference createReference(Id id) {
        return new InstanceReference(id, () -> get(id));
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return indexSource.count(from, to, this);
    }

    public List<InstanceReference> indexSelect(IndexKeyRT key) {
        return query(key.toQuery());
    }

    @Override
    public List<InstanceReference> query(InstanceIndexQuery query) {
        var memResults = NncUtils.map(memIndex.query(query), DurableInstance::getReference);
        if (query.memoryOnly())
            return memResults;
        var storeResults = NncUtils.map(indexSource.query(query, this), this::createReference);
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
        var id = instance.tryGetId();
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
                        String.format("Can not get instance '%s' because it's already removed",
                                found.getMappedEntity() != null ? EntityUtils.getEntityDesc(found.getMappedEntity()) : found));
            return found;
        } else {
            if(id instanceof ViewId viewId)
                initializeView(viewId);
            else {
                buffer(id);
                initializeInstance(id);
            }
            return Objects.requireNonNull(getBuffered(id), () -> "Failed to initialize instance " + id);
//            return add(id);
        }
    }

    @Override
    public DurableInstance internalGet(Id id) {
        var found = getBuffered(id);
        if (found != null)
            return found;
        else {
            buffer(id);
            initializeInstance(id);
            return getBuffered(id);
//            return add(id);
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
    public List<Id> filterAlive(List<Id> ids) {
        buffer(ids);
        return NncUtils.filter(ids, this::isAlive);
    }

    @Override
    public final boolean isAlive(Id id) {
        if (parent != null && parent.containsId(id))
            return parent.isAlive(id);
        var instance = getSelfBuffered(id);
        if (instance != null) {
            if (instance.isRemoved())
                return false;
            if (instance.isLoaded())
                return true;
        }
        return checkAliveInStore(id);
    }

    private InstanceReference allocateView(ViewId viewId) {
        var ref = new InstanceReference(viewId, () -> initializeView(viewId));
        ref.setView(true);
        return ref;
//        var viewType = viewId.getViewTypeKey(mappingProvider, typeDefProvider).toType(typeDefProvider);
//        DurableInstance view;
//        if (viewType instanceof ClassType classViewType) {
//            view = ClassInstanceBuilder.newBuilder(classViewType)
//                    .sourceRef(viewId.getSourceRef(this, mappingProvider, typeDefProvider))
//                    .load(this::initializeView)
//                    .id(viewId)
//                    .ephemeral(true)
//                    .build();
//        } else if (viewType instanceof ArrayType arrayViewType) {
//            view = new ArrayInstance(viewId, arrayViewType, true, this::initializeView);
//            view.setSourceRef(viewId.getSourceRef(this, mappingProvider, typeDefProvider));
//        } else
//            throw new InternalException("Invalid view type: " + viewType);
////        source2views.computeIfAbsent(view.tryGetSource(), k -> new ArrayList<>()).add(view);
//        return view;
    }

    private DurableInstance initializeView(ViewId id) {
        var rootId = id.getRootId();
        var mappingKey = Objects.requireNonNull(rootId.getMappingKey(),
                () -> "The MappingKey is missing in the id: " + rootId.getClass().getName() + "-" + rootId);
        var mapping = mappingKey.toMapping(mappingProvider, typeDefProvider);
        var sourceRef = Objects.requireNonNull(rootId.getSourceRef(this, mappingProvider, typeDefProvider));
        var view =  mapping.mapRoot(sourceRef.source().resolveObject(), this);
        view.forEachDescendant(this::add);
        return view;
//        r.accept(new InstanceCopier(r) {
//            @Override
//            protected Instance getExisting(Instance instance) {
//                var id = instance.tryGetId();
//                if (id != null) {
//                    var existing = internalGet(id);
//                    existing.setLoaded(requireNonNull(((DurableInstance) instance).tryGetSource()).isLoadedFromCache());
//                    return existing;
//                } else
//                    return null;
//            }
//        });
    }

//    private Type getTypeByInstanceId(Id id) {
//        TypeKey typeKey = switch (id) {
//            case PhysicalId physicalId -> physicalId.getTypeKey();
//            case ViewId viewId -> viewId.getViewTypeKey(mappingProvider, typeDefProvider);
//            default -> throw new IllegalStateException("Unexpected value: " + id);
//        };
//        return typeKey.toType(typeDefProvider);
//    }

//    protected abstract long getTypeId(long id);

    protected abstract void initializeInstance(Id id);

    protected abstract boolean checkAliveInStore(Id id);

//    @Override
//    public Instance getIfPresentByTmpId(long tmpId) {
//        return tmpId2Instance.get(tmpId);
//    }

    private InstanceReference add(Id id) {
        var instance = allocateInstance(id);
//        add(instance);
        return instance;
    }

    protected InstanceReference allocateInstance(Id id) {
        if (id.isArray()) {
            return switch (id) {
                case PhysicalId physicalId -> new InstanceReference(physicalId, () -> get(physicalId));
//                        new ArrayInstance(physicalId, Types.getAnyArrayType(), false, this::initializeInstance);
                case ViewId viewId -> allocateView(viewId);
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        } else {
            return switch (id) {
                case PhysicalId physicalId -> new InstanceReference(physicalId, () -> get(physicalId));
//                        new ClassInstance(physicalId, ClassInstance.uninitializedKlass.getType(), false, instance -> initializeInstance());
                case ViewId viewId -> allocateView(viewId);
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }
    }

    @Override
    public TypeDefProvider getTypeDefProvider() {
        return typeDefProvider;
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
    public boolean containsId(Id id) {
        return instanceMap.containsKey(id) || parent != null && parent.containsId(id);
    }

    @Override
    public boolean containsIdSelf(Id id) {
        return instanceMap.containsKey(id);
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
        try (var ignored = getProfiler().enter("saveViews")) {
            NncUtils.enhancedForEach(instanceMap.values(), value -> {
                if (value.isView()) {
                    var classInstance = (ClassInstance) value;
                    if (!classInstance.isList() && !classInstance.isRemoved() && !classInstance.isViewSaved())
                        saveView(classInstance);
                }
            });
        }
    }

    private void saveView(ClassInstance view) {
        view.setViewSaved();
        var mapping = view.getMappingKey().toMapping(mappingProvider, typeDefProvider);
        mapping.unmap(view.getReference(), this);
    }

    @Override
    public void close() {
        if (closed)
            throw new IllegalStateException("Context already closed");
        closed = true;
    }

    protected abstract void finishInternal();

    public DurableInstance getRemoved(Id id) {
        var instance = NncUtils.requireNonNull(instanceMap.get(id));
        NncUtils.requireTrue(instance.isRemoved());
        return instance;
    }

    protected boolean isRemoved(Id id) {
        var instance = instanceMap.get(id);
        return instance != null && instance.isRemoved();
    }

    @Override
    public abstract boolean isFinished();


    @Override
    public void initIdManually(DurableInstance instance, Id id) {
        NncUtils.requireTrue(instance.getContext() == this);
        instance.initId(id);
        onIdInitialized(instance);
    }

    protected void onIdInitialized(DurableInstance instance) {
        instanceMap.put(instance.tryGetId(), instance);
        listeners.forEach(l -> l.onInstanceIdInit(instance));
        forEachView(instance, v -> {
            if(v.getSourceRef().mapping() != null) {
                v.initId(
                        new DefaultViewId(
                                v instanceof ArrayInstance,
                                Objects.requireNonNull(v.getSourceRef().mapping(),
                                        () -> "Mapping is missing in the SourceRef: " + v.getSourceRef() + ", source: " + instance
                                ).toKey(),
                                v.getSource().tryGetId()
                        )
                );
            }
            // TODO handle the case of mapping being null
        });
    }

    protected void onContextInitializeId() {
        try (var ignored = getProfiler().enter("BaseInstanceContext.onContextInitializeId")) {
            for (ContextListener listener : listeners) {
                listener.afterContextIntIds();
            }
        }
    }

    protected boolean onChange(DurableInstance instance) {
        boolean anyChange = false;
        for (ContextListener listener : listeners) {
            if (listener.onChange(instance))
                anyChange = true;
        }
        return anyChange;
    }

    protected boolean onRemove(DurableInstance instance) {
        boolean anyChange = false;
        for (var listener : listeners) {
            if (listener.onRemove(instance))
                anyChange = true;
        }
        return anyChange;
    }

    protected void onPatchBuild() {
        try (var ignored = getProfiler().enter("onPatchBuild")) {
            for (ContextListener listener : listeners) {
                listener.onPatchBuild();
            }
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

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @Override
    public void batchRemove(Collection<DurableInstance> instances) {
        var removalBatch = getRemovalBatch(instances);
        removalBatch.forEach(i -> remove0(i, removalBatch));
    }

    public boolean remove(DurableInstance instance) {
        if (instance.isRemoved())
            throw new InternalException("Instance " + instance + " is already removed");
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(DurableInstance instance, RemovalSet removalBatch) {
        if (instance.isRemoved())
            return;
        var parent = instance.getParent();
        if (parent != null && !parent.isRemoved() && !removalBatch.contains(parent.resolve())) {
            switch (parent.resolve()) {
                case ClassInstance classParent -> {
                    var parentField = requireNonNull(instance.getParentField());
                    if (classParent.getField(parentField) instanceof InstanceReference r && r.resolve() == instance) {
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
                case ArrayInstance arrayParent -> arrayParent.removeChild(instance.getReference());
                default -> throw new IllegalStateException("Unexpected value: " + instance);
            }
        }
        instance.setRemoved();
        if (instance instanceof ClassInstance classInstance)
            removeFromMemIndex(classInstance);
        for (var listener : listeners) {
            listener.onInstanceRemoved(instance);
        }
        createReferenceCleanupJob(instance.getReference());
    }

    private RemovalSet getRemovalBatch(Collection<DurableInstance> instances) {
        var visited = new HashSet<DurableInstance>();
        var results = new RemovalSet();
        for (var instance : instances) {
            instance.accept(new DurableInstanceVisitor() {
                @Override
                public void visitDurableInstance(DurableInstance instance) {
                    if(visited.add(instance)) {
                        results.add(instance);
                        instance.forEachChild(c -> c.accept(this));
                        forEachView(instance, v -> v.accept(this));
                    }
                }
            });
        }
        return results;
    }

    private void forEachView(DurableInstance instance, Consumer<DurableInstance> action) {
        if (instance.isView())
            action.accept(instance);
        var views = source2views.get(instance);
        if (views != null)
            views.forEach(v -> forEachView(v, action));
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
    public void batchBind(Collection<DurableInstance> instances) {
        instances.forEach(this::checkForBind);
        for (var inst : instances) {
            if (inst.tryGetTreeId() == null)
                add(inst);
        }
    }

    private void checkForBind(DurableInstance instance) {
        //        NncUtils.requireFalse(instance.isEphemeral(), "Can not bind an ephemeral instance");
//        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
        NncUtils.requireNull(instance.getContext(), "Instance already bound");
        NncUtils.requireNull(instance.tryGetTreeId(),
                () -> {
                    throw new InternalException("Can not bind a persisted instance: " + instance);
                });
        NncUtils.requireFalse(instance.isRemoved(),
                () -> "Can not bind instance " + instance + " because it's already removed");
    }

    protected void craw() {
        try (var entry = getProfiler().enter("craw")) {
            var added = doCraw(this);
            entry.addMessage("numNewInstances", added.size());
            for (var inst : added) {
                if (inst.tryGetTreeId() == null && !containsInstance(inst)) {
                    add(inst);
                }
            }
        }
    }

    protected void clearMarks() {
        this.forEach(i -> i.setMarked(false));
    }

    protected List<DurableInstance> computeNonPersistedOrphans() {
        clearMarks();
        for (var instance : this) {
            if (instance.isInitialized() && instance.isRoot() && !instance.isRemoved())
                instance.forEachDescendant(d -> d.setMarked(true));
        }
        var orphans = new ArrayList<DurableInstance>();
        for (var instance : this) {
            if (instance.isNew() && !instance.isValue() && !instance.isRemoved() && !instance.isMarked())
                orphans.add(instance);
        }
        return orphans;
    }

    @Override
    public InstanceInput createInstanceInput(InputStream stream) {
        return new InstanceInput(stream, this::internalGet, this::add, getTypeDefProvider());
    }

    private void add(DurableInstance instance) {
        NncUtils.requireTrue(instance.getContext() == null);
        instance.setContext(this);
        if (instance.tryGetId() == null && instance.isValue())
            return;
        NncUtils.requireTrue(instance.getNext() == null && instance.getPrev() == null);
        NncUtils.requireFalse(instance.isRemoved(),
                () -> String.format("Can not add a removed instance: %d", instance.tryGetTreeId()));
        instance.setSeq(seq++);
        if (tail == null)
            head = tail = instance;
        else {
            tail.insertAfter(instance);
            tail = instance;
        }
        if (instance.isView())
            source2views.computeIfAbsent(instance.getSource().resolve(), k -> new ArrayList<>()).add(instance);
        if (instance.tryGetId() != null) {
//            logger.info("Adding instance {} to context, treeId: {}", instance.getId(), instance.getId().tryGetTreeId());
            if (instanceMap.put(instance.tryGetId(), instance) != null)
                logger.warn("Duplicate instance add to context: " + instance.tryGetId());
        }
    }

    protected void mapManually(Id id, DurableInstance instance) {
        instanceMap.put(id, instance);
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

    private record CrawResult(
            Collection<DurableInstance> newInstances,
            Set<Instance> visited
    ) {
    }

    private List<DurableInstance> doCraw(Iterable<DurableInstance> instances) {
        var added = new ArrayList<DurableInstance>();
        var visited = new IdentitySet<DurableInstance>();
        for (var instance : instances) {
            if (instance.isRoot() && !instance.isRemoved())
                instance.visitGraph(i -> {
                    if (i.isRemoved())
                        return false;
                    if (i.context != null && i.context != BaseInstanceContext.this)
                        return false;
                    if (i.context == null)
                        added.add(i);
                    return true;
                }, r -> r.tryGetId() == null || instanceMap.containsKey(r.getId()), visited);
        }
        return added;
    }


    //<editor-fold desc="getByType">

    //</editor-fold>

    @Override
    public List<InstanceReference> selectByKey(IndexKeyRT indexKey) {
        return query(indexKey.toQuery());
    }

    @Override
    public InstanceReference selectFirstByKey(IndexKeyRT key) {
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
