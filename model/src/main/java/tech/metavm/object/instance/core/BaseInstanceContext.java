package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.object.type.Type;
import tech.metavm.util.*;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

import static tech.metavm.util.NncUtils.first;
import static tech.metavm.util.NncUtils.requireNonNull;

public abstract class BaseInstanceContext implements IInstanceContext, Closeable, Iterable<Instance> {

    public static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceContext.class);

    protected final long appId;
    private final Map<ContextAttributeKey<?>, Object> attributes = new HashMap<>();
    private final Map<Long, Instance> instanceMap = new HashMap<>();
    private Instance head;
    private Instance tail;
    private LockMode lockMode = LockMode.NONE;
    protected final Profiler profiler = ContextUtil.getProfiler();

    protected final IInstanceContext parent;
    private final Set<ContextListener> listeners = new LinkedHashSet<>();
    private @Nullable Consumer<Object> bindHook;
    private final Map<IndexKeyRT, Set<ClassInstance>> memIndex = new HashMap<>();
    private final Map<ClassInstance, Set<IndexKeyRT>> indexKeys = new HashMap<>();
    private final Map<Long, Instance> tmpId2Instance = new HashMap<>();
    private boolean closed;
    private final boolean readonly;
    private final String clientId = ContextUtil.getClientId();
    private DefContext defContext;
    private final IndexSource indexSource;

    public BaseInstanceContext(long appId, DefContext defContext, IInstanceContext parent, boolean readonly, IndexSource indexSource) {
        this.appId = appId;
        this.defContext = defContext;
        this.readonly = readonly;
        this.parent = parent;
        this.indexSource = indexSource;
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
    public List<Instance> query(InstanceIndexQuery query) {
        return NncUtils.map(indexSource.query(query, this), this::get);
    }

    @Override
    public long count(InstanceIndexQuery query) {
        return indexSource.count(query, this);
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
        for (ContextListener listener : listeners) {
            listener.beforeFinish();
        }
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



    @Override
    public void initIdManually(Instance instance, long id) {
        NncUtils.requireTrue(instance.getContext() == this);
        instance.initId(id);
        onIdInitialized(instance);
    }

    protected void onIdInitialized(Instance instance) {
        instanceMap.put(instance.getId(), instance);
        for (var listener : listeners) {
            listener.onInstanceIdInit(instance);
        }
//        listeners.forEach(l -> l.onIdInitialized(instance));
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
        for (var listener : listeners) {
            listener.onInstanceRemoved(instance);
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

    protected void onInstanceInitialized(Instance instance) {
        for (var listener : listeners) {
            listener.onInstanceInitialized(instance);
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


    //<editor-fold desc="getByType">

    //</editor-fold>


    @Override
    public List<Instance> selectByKey(IndexKeyRT indexKey) {
        return query(
                new InstanceIndexQuery(
                        indexKey.getIndex(),
                        NncUtils.map(
                                indexKey.getIndex().getFields(),
                                f -> new InstanceIndexQueryItem(
                                        f,
                                        IndexOperator.EQ,
                                        indexKey.getFields().get(f))

                        ),
                        false,
                        null
                )
        );
    }

    @Override
    public Instance selectByUniqueKey(IndexKeyRT key) {
        var instances = memIndex.get(key);
        if (NncUtils.isNotEmpty(instances)) return instances.iterator().next();
        return NncUtils.first(selectByKey(key));
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
