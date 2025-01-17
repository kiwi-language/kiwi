package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.type.Klass;
import org.metavm.object.type.RedirectStatusProvider;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable, Iterable<Instance>, CallContext, TypeDefProvider, RedirectStatusProvider {

    public static final Logger logger = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long appId;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Id, Instance> instanceMap = new HashMap<>();
    private Instance head;
    private Instance tail;
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
    private final IndexSource indexSource;
    private int seq;
    private final long startAt = System.currentTimeMillis();
    private long timeout;
    private String description = "unnamed";
    private final Set<ClassInstance> reindexSet = new HashSet<>();
    private final Set<ClassInstance> searchReindexSet = new HashSet<>();

    private ParameterizedMap parameterizedMap;
    private final transient WeakReference<IInstanceContext> enclosingEntityContext;

    public BaseInstanceContext(long appId,
                               IInstanceContext parent,
                               boolean readonly,
                               IndexSource indexSource,
                               long timeout) {
        this.appId = appId;
        this.readonly = readonly;
        this.parent = parent;
        this.indexSource = indexSource;
        memIndex = new InstanceMemoryIndex();
        enclosingEntityContext = new WeakReference<>(ContextUtil.getEntityContext());       this.timeout = timeout;
        ContextUtil.setContext(this);
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
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        return Utils.map(indexSource.scan(from, to, this), this::createReference);
    }

    public Reference createReference(Id id) {
        return new Reference(id, () -> get(id));
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return indexSource.count(from, to, this);
    }

    public List<Reference> indexSelect(IndexKeyRT key) {
        return query(key.toQuery());
    }

    @Override
    public boolean containsUniqueKey(IndexKeyRT key) {
        if(!memIndex.query(key.toQuery()).isEmpty())
            return true;
        return parent != null && parent.containsUniqueKey(key);
    }

    @Override
    public List<Reference> query(InstanceIndexQuery query) {
        var memResults = Utils.map(memIndex.query(query), Instance::getReference);
        if (query.memoryOnly() || !memResults.isEmpty() && query.index().isUnique())
            return memResults;
        var storeResults = Utils.map(indexSource.query(query, this), this::createReference);
        return Instances.merge(memResults, storeResults, query.desc(), Utils.orElse(query.limit(), -1L));
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
    public List<Instance> batchGet(Collection<Id> ids) {
        ids.forEach(this::buffer);
        return Utils.map(ids, this::get);
    }

    @Override
    public Instance get(Id id) {
        var found = getBuffered(id);
        if (found != null) {
            if (found.isRemoved())
                throw new InternalException(
                        String.format("Can not get instance '%s' because it's already removed", found));
            return found;
        } else if (id.isTemporary()) {
            return null;
        } else {
            buffer(id);
            initializeInstance(id);
            return requireNonNull(getBuffered(id), () -> "Failed to initialize instance " + id);
//            return add(id);
        }
    }

    @Override
    public Instance internalGet(Id id) {
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
    public @Nullable Instance getBuffered(Id id) {
        var found = instanceMap.get(id);
        if (found == null && parent != null)
            found = parent.getBuffered(id);
        return found;
    }

    protected Instance getSelfBuffered(Id id) {
        return instanceMap.get(id);
    }

    @Override
    public List<Id> filterAlive(List<Id> ids) {
        buffer(ids);
        return Utils.filter(ids, this::isAlive);
    }

    @Override
    public final boolean isAlive(Id id) {
        if (parent != null && parent.containsId(id))
            return parent.isAlive(id);
        var instance = getSelfBuffered(id);
        if (instance != null) {
            return !instance.isRemoved();
        }
        return checkAliveInStore(id);
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

    private Reference add(Id id) {
        var instance = allocateInstance(id);
//        add(instance);
        return instance;
    }

    protected Reference allocateInstance(Id id) {
        return new Reference(id, () -> get(id));
    }

    @Override
    public TypeDefProvider getTypeDefProvider() {
        return this;
    }

    @Override
    public RedirectStatusProvider getRedirectStatusProvider() {
        return this;
    }

    @Override
    public boolean containsInstance(Instance instance) {
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
        beforeFinish();
        try (var ignored = getProfiler().enter("finish")) {
            finishInternal();
        }
        if(timeout > 0) {
            var elapsed = System.currentTimeMillis() - startAt;
            if (elapsed > timeout)
                throw new SessionTimeoutException("Timeout: " + timeout + ". Elapsed: " + elapsed);
        }
        if (DebugEnv.dumpContextAfterFinish) {
            for (Instance instance : this) {
//                if (instance.isRoot())
                    logger.debug("{} {}", instance.tryGetTreeId(), instance.getText());
            }
        }
        if (DebugEnv.dumpClassAfterContextFinish) {
            var klasses = new HashSet<Klass>();
            for (Instance instance : this) {
                if (instance instanceof MvClassInstance o) klasses.add(o.getInstanceKlass());
            }
            for (Klass klass : klasses) {
                logger.debug("{}", klass.getText());
            }
        }
    }

    private void beforeFinish() {
        for (var o : this) {
            if (o instanceof ContextFinishWare c)
                c.onContextFinish(this);
        }
        for (ContextListener listener : listeners) {
            listener.beforeFinish();
        }
    }

    @Override
    public void close() {
        if (closed)
            throw new IllegalStateException("Context already closed");
        ContextUtil.setContext(enclosingEntityContext.get());
        closed = true;
    }

    protected abstract void finishInternal();

    public Instance getRemoved(Id id) {
        var instance = requireNonNull(instanceMap.get(id));
        Utils.require(instance.isRemoved());
        return instance;
    }

    protected boolean isRemoved(Id id) {
        var instance = instanceMap.get(id);
        return instance != null && instance.isRemoved();
    }

    @Override
    public abstract boolean isFinished();


    @Override
    public void initIdManually(Instance instance, Id id) {
        Utils.require(instance.getContext() == this,
                () -> "Instance " + instance + " is not managed by this context");
        instance.initId(id);
        onIdInitialized(instance);
    }

    protected void onIdInitialized(Instance instance) {
        instanceMap.put(instance.tryGetId(), instance);
        listeners.forEach(l -> l.onInstanceIdInit(instance));
    }

    protected void onContextInitializeId() {
        try (var ignored = getProfiler().enter("BaseInstanceContext.onContextInitializeId")) {
            forEach(instance -> {
                if (instance instanceof Entity entity && instance.isNew() && instance.setAfterContextInitIdsNotified())
                    entity.afterContextInitIds();
            });
            for (ContextListener listener : listeners) {
                listener.afterContextIntIds();
            }
        }
    }

    protected boolean onChange(Instance instance) {
        boolean anyChange = false;
        if (instance instanceof ChangeAware changeAware && changeAware.isChangeAware()) {
            changeAware.onChange(this);
            anyChange = true;
        }
        for (ContextListener listener : listeners) {
            if (listener.onChange(instance))
                anyChange = true;
        }
        return anyChange;
    }

    protected boolean onRemove(Instance instance) {
        boolean anyChange = false;
        if (instance instanceof PostRemovalAware removalAware) {
            removalAware.postRemove(this);
            anyChange = true;
        }
        for (var listener : listeners) {
            if (listener.onRemove(instance))
                anyChange = true;
        }
        return anyChange;
    }

    protected void onPatchBuild() {
        for (Instance instance : this) {
            if(instance instanceof MvClassInstance clsInst && clsInst.getNativeObject() != null) {
                var nat = clsInst.getNativeObject();
                nat.flush();
            }
        }
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
    public void batchRemove(Collection<Instance> instances) {
        for (Instance instance : instances) {
            if(instance.tryGetOldId() != null)
                buffer(instance.getOldId());
        }
        for (Instance instance : instances) {
            if(instance.tryGetOldId() != null)
                loadTree(instance.getOldId().getTreeId());
        }
        var removalBatch = getRemovalBatch(instances);
        removalBatch.forEach(i -> remove0(i, removalBatch));
        for (Instance instance : instances) {
            if(instance instanceof MvInstance mvInst && mvInst.tryGetOldId() != null)
                removeForwardingPointer(mvInst, false);
        }
    }

    public boolean remove(Instance instance) {
        if (instance.isRemoved())
            throw new InternalException("Instance " + instance + " is already removed");
        batchRemove(List.of(instance));
        return true;
    }

    private void remove0(Instance instance, RemovalSet removalBatch) {
        if (instance.isRemoved())
            return;
        var parent = instance.getParent();
        if (parent != null && !parent.isRemoved() && !removalBatch.contains(parent)) {
            switch (parent) {
                case ClassInstance classParent -> {
                    if (instance instanceof MvClassInstance mvInst) {
                        var parentField = requireNonNull(mvInst.getParentField());
                        if (classParent.getField(parentField) instanceof Reference r && r.get() == instance) {
                            if (parentField.isNullable())
                                classParent.setField(parentField, Instances.nullInstance());
                            else {
                                throw new BusinessException(
                                        ErrorCode.STRONG_REFS_PREVENT_REMOVAL2,
                                        ((MvInstance) instance).getQualifiedTitle(),
                                        classParent.getTitle()
                                );
                            }
                        }
                    }
                }
                case ArrayInstance arrayParent -> arrayParent.remove(instance.getReference());
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

    private RemovalSet getRemovalBatch(Collection<Instance> instances) {
        var results = new RemovalSet();
        instances.forEach(i -> beforeRemove(i, results));
        return results;
    }

    private void beforeRemove(Instance instance, RemovalSet removalSet) {
        if(!removalSet.add(instance))
            return;
        var cascade = new ArrayList<Instance>();
        if (instance instanceof RemovalAware removalAware)
            cascade.addAll(removalAware.beforeRemove(this));

        for (ContextListener listener : listeners) {
            cascade.addAll(listener.beforeRemove(instance, removalSet::contains));
        }
        instance.forEachChild(cascade::add);
        for (Instance i : cascade) {
            beforeRemove(i, removalSet);
        }
    }

    private void createReferenceCleanupJob(Value instance) {
//        if (createJob != null && isPersisted(instance)) {
//            createJob.accept(new ReferenceCleanupTask(
//                    instance.getIdRequired(),
//                    instance.getType().getName(),
//                    instance.getTitle()
//            ));
//        }
    }

    @Override
    public void batchBind(Collection<Instance> instances) {
        instances.forEach(this::checkForBind);
        for (var inst : instances) {
            if (inst.tryGetTreeId() == null) add(inst);
            if (inst instanceof Entity entity) updateMemoryIndex(entity);
        }
    }

    private void checkForBind(Instance instance) {
        //        NncUtils.requireFalse(instance.isEphemeral(), "Can not bind an ephemeral instance");
//        NncUtils.requireFalse(instance.isValue(), "Can not bind a value instance");
        Utils.require(instance.getContext() == null, "Instance already bound");
        Utils.require(instance.tryGetTreeId() == null,
                () -> {
                    throw new InternalException("Can not bind a persisted instance: " + instance);
                });
        Utils.require(!instance.isRemoved(),
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
        this.forEach(Instance::clearMarked);
    }

    protected List<Instance> computeNonPersistedOrphans() {
        clearMarks();
        for (var instance : this) {
            if (instance.isRoot() && !instance.isRemoved())
                instance.forEachDescendant(Instance::setMarked);
        }
        var orphans = new ArrayList<Instance>();
        for (var instance : this) {
            if (instance.isNew() && !instance.isValue() && !instance.isRemoved() && !instance.isMarked())
                orphans.add(instance);
        }
        return orphans;
    }

    @Override
    public InstanceInput createInstanceInput(InputStream stream) {
        return new InstanceInput(stream, this::internalGet, this::add, this);
    }

    private void add(Instance instance) {
        Utils.require(instance.getContext() == null);
        instance.setContext(this);
        if (instance.tryGetId() == null && instance.isValue())
            return;
        Utils.require(instance.getNext() == null && instance.getPrev() == null);
        Utils.require(!instance.isRemoved(),
                () -> String.format("Can not add a removed instance: %d", instance.tryGetTreeId()));
        instance.setSeq(seq++);
        if (tail == null)
            head = tail = instance;
        else {
            tail.insertAfter(instance);
            tail = instance;
        }
        if (instance.tryGetCurrentId() != null) {
//            logger.info("Adding instance {} to context, treeId: {}", instance.getId(), instance.getId().tryGetTreeId());
            if (instanceMap.put(instance.tryGetCurrentId(), instance) != null)
                logger.warn("Duplicate instance add to context: " + instance.tryGetCurrentId());
        }
        if (instance.tryGetOldId() != null)
            instanceMap.put(instance.tryGetOldId(), instance);
    }

    protected void mapManually(Id id, Instance instance) {
        instanceMap.put(id, instance);
    }

    protected void onInstanceInitialized(Instance instance) {
        for (var listener : listeners) {
            listener.onInstanceInitialized(instance);
        }
        if(instance instanceof Entity) {
            var loadAware = new ArrayList<LoadAware>();
            instance.accept(new StructuralInstanceVisitor() {

                @Override
                public Void visitInstance(Instance instance) {
                    super.visitInstance(instance);
                    if (instance instanceof LoadAware l)
                        loadAware.add(l);
                    return null;
                }
            });
            loadAware.forEach(LoadAware::onLoadPrepare);
            loadAware.forEach(LoadAware::onLoad);
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
            Collection<Instance> newInstances,
            Set<Value> visited
    ) {
    }

    private List<Instance> doCraw(Iterable<Instance> instances) {
        var added = new ArrayList<Instance>();
        var visited = new IdentitySet<Instance>();
        for (var instance : instances) {
            if (instance.isRoot() && !instance.isRemoved())
                instance.visitGraph(i -> {
                    if (i.isRemoved())
                        return false;
                    var ctx = i.getContext();
                    if (ctx != null && ctx != BaseInstanceContext.this)
                        return false;
                    if (ctx == null)
                        added.add(i);
                    return true;
                }, r -> r.tryGetId() == null || instanceMap.containsKey(r.getId()), visited);
        }
        return added;
    }


    //<editor-fold desc="getByType">

    //</editor-fold>

    @Override
    public List<Reference> selectByKey(IndexKeyRT indexKey) {
        return query(indexKey.toQuery());
    }

    @Override
    public Reference selectFirstByKey(IndexKeyRT key) {
        if(key.getIndex().isUnique() && parent != null && parent.containsUniqueKey(key))
            return parent.selectFirstByKey(key);
//        NncUtils.requireTrue(key.getIndex().isUnique());
//        var instances = memIndex.get(key);
//        if (NncUtils.isNotEmpty(instances)) return instances.iterator().next();
        return Utils.first(selectByKey(key));
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

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "InstanceContext-" + description + "-" + System.identityHashCode(this);
    }

    @Override
    public void forceReindex(ClassInstance instance) {
        reindexSet.add(instance);
    }

    @Override
    public Set<ClassInstance> getReindexSet() {
        return Collections.unmodifiableSet(reindexSet);
    }

    @Override
    public void forceSearchReindex(ClassInstance instance) {
        searchReindexSet.add(instance);
    }

    public Set<ClassInstance> getSearchReindexSet() {
        return Collections.unmodifiableSet(searchReindexSet);
    }

    @Override
    public long getAppId(Instance instance) {
        if (instance.getContext() == this)
            return getAppId();
        else if (parent != null)
            return parent.getAppId(instance);
        else
            throw new InternalException("Instance " + instance + " is not contained in the context");
    }

    @Override
    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
        return Utils.filterByType(this, entityClass);
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Value... values) {
        try (var ignored = getProfiler().enter("selectByKey")) {
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            var instances = selectByKey(indexKey);
            return createEntityList(indexDef.getType(), instances);
        }
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        var instance = selectFirstByKey(createIndexKey(indexDef, values));
        return instance == null ? null : createEntityList(indexDef.getType(), List.of(instance)).getFirst();
    }

    private IndexKeyRT createIndexKey(IndexDef<?> indexDef, Value... values) {
        var constraint = indexDef.getIndex();
        Objects.requireNonNull(constraint);
        return constraint.createIndexKey(List.of(values));
    }

    private <T> List<T> createEntityList(Class<T> javaType, List<? extends Reference> references) {
        var list = new ArrayList<T>();
        references.forEach(ref -> list.add(javaType.cast(ref.get())));
        return list;
    }


    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        Class<T> javaClass = query.indexDef().getType();
        var instances = query(convertToInstanceIndexQuery(query));
        return createEntityList(javaClass, instances);
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return count(convertToInstanceIndexQuery(query));
    }

    private InstanceIndexQuery convertToInstanceIndexQuery(EntityIndexQuery<?> query) {
        var index = query.indexDef().getIndex();
        return new InstanceIndexQuery(
                index,
                query.from() != null ? new InstanceIndexKey(index, query.from().values()) : null,
                query.to() != null ? new InstanceIndexKey(index, query.to().values()) : null,
                query.desc(),
                query.limit()
        );
    }

    @Override
    public boolean containsUniqueKey(IndexDef<?> indexDef, Value... values) {
        for (var value : values) {
            if(value instanceof Reference ref && !containsId(ref.getId()))
                return false ;
        }
        return containsUniqueKey(createIndexKey(indexDef, values));
    }
    @Override
    public ParameterizedMap getParameterizedMap() {
        return parameterizedMap;
    }

    @Override
    public void setParameterizedMap(ParameterizedMap parameterizedMap) {
        this.parameterizedMap = parameterizedMap;
    }
}
