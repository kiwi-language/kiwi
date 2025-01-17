package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.event.EventQueue;
import org.metavm.flow.Code;
import org.metavm.flow.Flow;
import org.metavm.flow.Node;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class BaseEntityContext implements CompositeTypeFactory, IEntityContext, ContextListener {

    private final IInstanceContext instanceContext;
    private ParameterizedMap parameterizedMap;
    private final transient WeakReference<IEntityContext> enclosingEntityContext;

    public BaseEntityContext(IInstanceContext instanceContext) {
        this.instanceContext = instanceContext;
        instanceContext.addListener(this);
        enclosingEntityContext = new WeakReference<>(ContextUtil.getEntityContext());
        ContextUtil.setEntityContext(this);
    }

    @Override
    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
        return Utils.filterByType(instanceContext, entityClass);
    }

    public void onInstanceInitialized(Instance instance) {
        if(instance instanceof Entity) {
            var loadAware = new ArrayList<LoadAware>();
            instance.accept(new StructuralInstanceVisitor() {

                @Override
                public Void visitInstance(Instance instance) {
                    super.visitInstance(instance);
                    var entity = onInstanceInitialized0(instance);
                    if (entity instanceof LoadAware l)
                        loadAware.add(l);
                    return null;
                }
            });
            loadAware.forEach(LoadAware::onLoadPrepare);
            loadAware.forEach(LoadAware::onLoad);
        }
    }

    private @Nullable Object onInstanceInitialized0(Instance instance) {
        return instance;
    }

    @Override
    public <T> T getRemoved(Class<T> entityClass, Id id) {
        return entityClass.cast(getEntity(entityClass, instanceContext.getRemoved(id)));
    }

    @Override
    public boolean onChange(Instance instance) {
        if (instance instanceof ChangeAware changeAware && changeAware.isChangeAware()) {
            changeAware.onChange(this);
            return true;
        }
        return false;
    }

    @Override
    public List<Instance> beforeRemove(Instance instance, Predicate<Instance> contains) {
        List<Instance> cascade;
        if (instance instanceof RemovalAware removalAware)
            cascade =  removalAware.beforeRemove(this);
        else
            cascade = List.of();
        return cascade;
    }

    @Override
    public boolean onRemove(Instance instance) {
        if (instance instanceof PostRemovalAware removalAware) {
            removalAware.postRemove(this);
            return true;
        }
        return false;
    }

    @Override
    public void afterContextIntIds() {
        try (var ignored = getProfiler().enter("BaseEntityContext.afterContextIntIds", true)) {
            instanceContext.forEach(instance -> {
                if (instance instanceof Entity entity && instance.isNew() && instance.setAfterContextInitIdsNotified())
                    entity.afterContextInitIds();
            });
        }
    }

    public <T extends Instance> T bind(T entity) {
//        if(model instanceof Entity entity && entity.isEphemeralEntity())
//            throw new IllegalArgumentException("Can not bind an ephemeral entity");
        Utils.require(EntityUtils.tryGetPhysicalId(entity) == null, "Can not bind a persisted entity");
        if (this.containsInstance(entity))
            return entity;
        instanceContext.bind(entity);
        instanceContext.updateMemoryIndex((ClassInstance) entity);
        return entity;
    }

    public Flow getFlow(Id id) {
        return getEntity(Flow.class, id);
    }

    public Code getCode(Id id) {
        return getEntity(Code.class, id);
    }

    public Field getField(Id id) {
        return getEntity(Field.class, id);
    }

    public Node getNode(Id id) {
        return getEntity(Node.class, id);
    }


    @SuppressWarnings("unused")
    public Index getUniqueConstraint(Id id) {
        return getEntity(Index.class, id);
    }

    @SuppressWarnings("unused")
    public CheckConstraint getCheckConstraint(Id id) {
        return getEntity(CheckConstraint.class, id);
    }

    public <T extends Entity> T getEntity(TypeReference<T> typeReference, Id id) {
        return getEntity(typeReference.getType(), id);
    }

    @Override
    public <T> T getBufferedEntity(Class<T> entityClass, Id id) {
        return entityClass.cast(instanceContext.getBuffered(id));
    }

    @Override
    public <T> @Nullable T getEntity(Class<T> entityType, Id id) {
        if (id.isTemporary())
            return entityType.cast(instanceContext.getBuffered(id));
        else
            return entityType.cast(instanceContext.get(id));
    }

    public <T> T get(Class<T> klass, Id id) {
        return klass.cast(getEntity(klass, id));
    }


    private Profiler.Entry enter(String name) {
        return getProfiler().enter(name);
    }

    public void initIds() {
        try (var ignored = enter("initIds")) {
            Objects.requireNonNull(instanceContext);
            this.flush();
            instanceContext.initIds();
        }
    }

    public void finish() {
        for (var o : instanceContext) {
            if (o instanceof ContextFinishWare c)
                c.onContextFinish(this);
        }
        instanceContext.finish();
    }

    @Override
    public void beforeFinish() {
        try (var ignored1 = getProfiler().enter("flush")) {
            flush();
        }
    }

    public void close() {
        if (instanceContext != null) {
            instanceContext.close();
        }
        ContextUtil.setEntityContext(enclosingEntityContext.get());
    }

    @Override
    public boolean isFinished() {
        return instanceContext != null && instanceContext.isFinished();
    }

    @Override
    public void flush() {
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Value... values) {
        try (var ignored = enter("selectByKey")) {
            Objects.requireNonNull(instanceContext, "instanceContext required");
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            var instances = instanceContext.selectByKey(indexKey);
            return createEntityList(indexDef.getType(), instances);
        }
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        var instance = instanceContext.selectFirstByKey(createIndexKey(indexDef, values));
        return instance == null ? null : createEntityList(indexDef.getType(), List.of(instance)).getFirst();
    }

    @Override
    public boolean containsUniqueKey(IndexDef<?> indexDef, Value... values) {
        for (var value : values) {
            if(value instanceof Reference ref && !containsId(ref.getId()))
                return false ;
        }
        return instanceContext.containsUniqueKey(createIndexKey(indexDef, values));
    }

    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        Class<T> javaClass = query.indexDef().getType();
        var instances = instanceContext.query(convertToInstanceIndexQuery(query));
        return createEntityList(javaClass, instances);
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return instanceContext.count(convertToInstanceIndexQuery(query));
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

    private <T> List<T> createEntityList(Class<T> javaType, List<? extends Reference> references) {
        var list = new ArrayList<T>();
        references.forEach(ref -> list.add(javaType.cast(ref.get())));
        return list;
    }

    @Override
    public long getAppId() {
        Objects.requireNonNull(instanceContext);
        return instanceContext.getAppId();
    }

    private Set<Instance> beforeRemove(List<?> entities) {
        var instancesToRemove = new IdentitySet<Instance>();
        for (Object entity : entities) {
            beforeRemove0(entity, instancesToRemove);
        }
        return instancesToRemove;
    }

    private void beforeRemove0(Object object, Set<Instance> instancesToRemove) {
        if (object instanceof MvInstance instance) {
            instancesToRemove.add(instance);
            return;
        }
        var instance = (Instance) object;
        if (instance == null || instancesToRemove.contains(instance))
            return;
        EntityUtils.ensureProxyInitialized(object);
        instancesToRemove.add(instance);
        var cascades = new IdentitySet<>(getNonEphemeralChildren(object));
        if (object instanceof RemovalAware removalAware)
            cascades.addAll(removalAware.beforeRemove(this));
        if (Utils.isNotEmpty(cascades)) {
            for (Object cascade : cascades) {
                beforeRemove0(cascade, instancesToRemove);
            }
        }
    }

    private Set<Object> getNonEphemeralChildren(Object object) {
        if (object instanceof Entity entity) {
            Set<Object> children = new IdentitySet<>();
            entity.forEachChild(children::add);
            return children;
        }
        else
            return Set.of();
    }

    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    @SuppressWarnings("unused")
    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Value... fieldValues) {
        Objects.requireNonNull(instanceContext);
        IndexKeyRT indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        var instance = instanceContext.selectFirstByKey(indexKey);
        return Utils.safeCall(instance, i -> getEntity(entityType, requireNonNull(i.tryGetId())));
    }

    private IndexKeyRT createIndexKey(IndexDef<?> uniqueConstraintDef, Value... values) {
        var constraint = uniqueConstraintDef.getIndex();
        Objects.requireNonNull(constraint);
        return constraint.createIndexKey(List.of(values));
    }

    @Override
    public Type getType(Class<?> javaType) {
        return getDefContext().getType(javaType);
    }

    @Override
    public ParameterizedMap getParameterizedMap() {
        return parameterizedMap;
    }

    @Override
    public void setParameterizedMap(ParameterizedMap parameterizedMap) {
        this.parameterizedMap = parameterizedMap;
    }


    @Override
    public void setLockMode(LockMode mode) {
        instanceContext.setLockMode(mode);
    }

    @Override
    public LockMode getLockMode() {
        return instanceContext.getLockMode();
    }

    @Override
    public List<Id> filterAlive(List<Id> ids) {
        return instanceContext.filterAlive(ids);
    }

    @Override
    public boolean isAlive(Id id) {
        return instanceContext.isAlive(id);
    }

    @Override
    public Instance get(Id id) {
        return instanceContext.get(id);
    }

    @Override
    public Reference createReference(Id id) {
        return instanceContext.createReference(id);
    }

    @Override
    public List<Instance> batchGetRoots(List<Long> treeIds) {
        return instanceContext.batchGetRoots(treeIds);
    }

    @Override
    public Instance getRoot(long treeId) {
        return instanceContext.getRoot(treeId);
    }

    @Nullable
    @Override
    public Instance getBuffered(Id id) {
        return instanceContext.getBuffered(id);
    }

    @Override
    public String getClientId() {
        return instanceContext.getClientId();
    }

    @Override
    public Instance internalGet(Id id) {
        return instanceContext.internalGet(id);
    }

    @Override
    public boolean contains(Id id) {
        return instanceContext.contains(id);
    }

    @Override
    public Profiler getProfiler() {
        return instanceContext.getProfiler();
    }

    @Override
    public List<Instance> batchGet(Collection<Id> ids) {
        return instanceContext.batchGet(ids);
    }

    @Override
    public ScanResult scan(long start, long limit) {
        return instanceContext.scan(start, limit);
    }

    @Override
    public InstanceRepository instanceRepository() {
        return instanceContext.instanceRepository();
    }

    @Override
    public void loadTree(long id) {
        instanceContext.loadTree(id);
    }

    @Override
    public boolean isReferenced(Instance instance) {
        return instanceContext.isReferenced(instance);
    }

    @Override
    public TypeDefProvider getTypeDefProvider() {
        return instanceContext.getTypeDefProvider();
    }

    @Override
    public RedirectStatusProvider getRedirectStatusProvider() {
        return instanceContext.getRedirectStatusProvider();
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instanceContext.containsInstance(instance);
    }

    @Override
    public boolean containsId(Id id) {
        return instanceContext.containsId(id);
    }

    @Override
    public List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
        return instanceContext.getByReferenceTargetId(targetId, startExclusive, limit);
    }

    @Override
    public List<Instance> getRelocated() {
        return instanceContext.getRelocated();
    }

    @Override
    public void buffer(Id id) {
        instanceContext.buffer(id);
    }

    @Override
    public void removeForwardingPointer(MvInstance instance, boolean clearingOldId) {
        instanceContext.removeForwardingPointer(instance, clearingOldId);
    }

    @Override
    public void buffer(Collection<? extends Id> ids) {
        instanceContext.buffer(ids);
    }

    @Override
    public boolean containsIdSelf(Id id) {
        return instanceContext.containsIdSelf(id);
    }

    @Override
    public void batchRemove(Collection<Instance> instances) {
        instanceContext.batchRemove(instances);
    }

    @Override
    public boolean remove(Instance instance) {
        return instanceContext.remove(instance);
    }

    @Override
    public IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider) {
        return instanceContext.createSame(appId, typeDefProvider);
    }

    @Override
    public List<Reference> selectByKey(IndexKeyRT indexKey) {
        return instanceContext.selectByKey(indexKey);
    }

    @Override
    public List<Reference> query(InstanceIndexQuery query) {
        return instanceContext.query(query);
    }

    @Override
    public long count(InstanceIndexQuery query) {
        return instanceContext.count(query);
    }

    @Override
    public boolean containsUniqueKey(IndexKeyRT key) {
        return instanceContext.containsUniqueKey(key);
    }

    @Override
    public Reference selectFirstByKey(IndexKeyRT key) {
        return instanceContext.selectFirstByKey(key);
    }

    @Override
    public void batchBind(Collection<Instance> instances) {
        instanceContext.batchBind(instances);
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        instanceContext.registerCommitCallback(action);
    }

    @Override
    public <E> E getAttribute(ContextAttributeKey<E> key) {
        return instanceContext.getAttribute(key);
    }

    @Override
    public void initIdManually(Instance instance, Id id) {
        instanceContext.initIdManually(instance, id);
    }

    @Override
    public void increaseVersionsForAll() {
        instanceContext.increaseVersionsForAll();
    }

    @Override
    public void updateMemoryIndex(ClassInstance instance) {
        instanceContext.updateMemoryIndex(instance);
    }

    @Nullable
    @Override
    public Consumer<Object> getBindHook() {
        return instanceContext.getBindHook();
    }

    @Override
    public Instance getRemoved(Id id) {
        return instanceContext.getRemoved(id);
    }

    @Override
    @Nullable
    public EventQueue getEventQueue() {
        return instanceContext.getEventQueue();
    }

    @Override
    public InstanceInput createInstanceInput(InputStream stream) {
        return instanceContext.createInstanceInput(stream);
    }

    @Override
    public long getTimeout() {
        return instanceContext.getTimeout();
    }

    @Override
    public void setTimeout(long timeout) {
        instanceContext.setTimeout(timeout);
    }

    @Override
    public String getDescription() {
        return instanceContext.getDescription();
    }

    @Override
    public void setDescription(String description) {
        instanceContext.setDescription(description);
    }

    @Override
    public void forceReindex(ClassInstance instance) {
        instanceContext.forceReindex(instance);
    }

    @Override
    public Set<ClassInstance> getReindexSet() {
        return instanceContext.getReindexSet();
    }

    @Override
    public void forceSearchReindex(ClassInstance instance) {
        instanceContext.forceSearchReindex(instance);
    }

    @Override
    public Set<ClassInstance> getSearchReindexSet() {
        return instanceContext.getSearchReindexSet();
    }

    @Override
    public void addListener(ContextListener listener) {
        instanceContext.addListener(listener);
    }

    @Override
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        return instanceContext.indexScan(from, to);
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return instanceContext.indexCount(from, to);
    }

    @Override
    public List<Reference> indexSelect(IndexKeyRT key) {
        return instanceContext.indexSelect(key);
    }

    @Override
    public @NotNull Iterator<Instance> iterator() {
        return instanceContext.iterator();
    }

    @Override
    public void forEach(Consumer<? super Instance> action) {
        instanceContext.forEach(action);
    }

    @Override
    public Spliterator<Instance> spliterator() {
        return instanceContext.spliterator();
    }

    @Override
    public long getAppId(Instance instance) {
        return instanceContext.getAppId(instance);
    }
}
