package org.metavm.entity;

import javassist.util.proxy.ProxyObject;
import org.metavm.event.EventQueue;
import org.metavm.flow.Flow;
import org.metavm.flow.NodeRT;
import org.metavm.flow.ScopeRT;
import org.metavm.object.instance.DefaultObjectInstanceMap;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static org.metavm.entity.EntityUtils.getRuntimeType;
import static org.metavm.entity.EntityUtils.isModelInitialized;

public abstract class BaseEntityContext implements CompositeTypeFactory, IEntityContext, ContextListener {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(BaseEntityContext.class);
    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Map<Id, Object> entityMap = new HashMap<>();

    private final IInstanceContext instanceContext;
    private final IdentitySet<Object> entities = new IdentitySet<>();
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    //    private final IdentitySet<Object> removedEntities = new IdentitySet<>();
//    private final Map<Long, Object> removedEntityMap = new HashMap<>();
    @Nullable
    private final IEntityContext parent;
    private final ObjectInstanceMap objectInstanceMap = new DefaultObjectInstanceMap(this, this::addBinding);

    public BaseEntityContext(IInstanceContext instanceContext, @Nullable IEntityContext parent) {
        this.instanceContext = instanceContext;
        this.parent = parent;
        instanceContext.addListener(this);
    }

    protected abstract TypeFactory getTypeFactory();

    @Override
    public <T> T getEntity(Class<T> klass, Instance instance) {
        return getEntity(klass, instance, null);
    }

    public @Nullable IEntityContext getParent() {
        return parent;
    }

    @Override
    public <T> T getEntity(Class<T> entityClass, Instance instance, @Nullable Mapper<T, ?> mapper) {
        var entity = instance.getMappedEntity();
        if(entity != null)
            return entityClass.cast(entity);
        if(mapper == null)
            mapper = getDefContext().getMapper(instance.getType()).as(entityClass);
        return createEntity(instance, mapper);
    }

    @Override
    public EventQueue getEventQueue() {
        return instanceContext.getEventQueue();
    }

    @Override
    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
        return NncUtils.filterByType(model2instance.keySet(), entityClass);
    }

    @Override
    public Profiler getProfiler() {
        return instanceContext.getProfiler();
    }

    @Override
    public void invalidateCache(Id id) {
        var entity = get(Object.class, id);
        var instance = getInstance(entity);
        instanceContext.invalidateCache(instance.getReference());
    }

    @Override
    public void onInstanceIdInit(Instance instance) {
        Object model = instance.getMappedEntity();
        if (model != null && model2instance.containsKey(model)) {
            entityMap.put(instance.getId(), model);
            if (model instanceof IdInitializing idInitializing) {
                NncUtils.requireNull(idInitializing.tryGetPhysicalId(), () -> "ID is already set for entity " + idInitializing);
                idInitializing.initId(instance.tryGetId());
            }
        }
    }

    public void onInstanceInitialized(Instance instance) {
        if(!TypeTags.isSystemTypeTag(instance.getType().getTypeTag()))
            return;
        var loadAware = new ArrayList<LoadAware>();
        instance.accept(new StructuralInstanceVisitor() {

            @Override
            public void visitInstance(Instance instance) {
                super.visitInstance(instance);
                var entity = onInstanceInitialized0(instance);
                if(entity instanceof LoadAware l)
                    loadAware.add(l);
            }
        });
        loadAware.forEach(LoadAware::onLoadPrepare);
        loadAware.forEach(LoadAware::onLoad);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable Object onInstanceInitialized0(Instance instance) {
        if(instance.getMappedEntity() != null)
            return instance.getMappedEntity();
        var typeTag = instance.getType().getTypeTag();
        if (typeTag == 0 || typeTag >= 1000000)
            return null;
        var mapper = (Mapper) getDefContext().getMapper(typeTag);
        return createEntity(instance, mapper);
//        if (!mapper.isProxySupported()) {
//            mapper.createEntity(instance, getObjectInstanceMap());
//        } else {
//            var entity = EntityProxyFactory.getProxy(
//                    mapper.getEntityClass(),
//                    instance.getId(),
//                    k -> mapper.getEntityClass().cast(mapper.createModelProxyHelper(k)),
//                    m -> instanceContext.get(instance.getId())
//            );
//            mapper.initEntityHelper(entity, instance, getObjectInstanceMap());
//        }
//        Object model = instance.getMappedEntity();
//        if (model != null && model2instance.containsKey(model)) {
//            var mapper = getDefContext().getMapperByEntity(model);
//            initializeModel0(model, instance, mapper);
//        }
    }

    @Override
    public void onInstanceRemoved(Instance instance) {
        Object model = instance.getMappedEntity();
        if (model != null && model2instance.containsKey(model)) {
            if (model instanceof Entity entity)
                entity.setRemoved();
//            model2instance.remove(model);
//            removedEntities.add(model);
//            if (instance.getId() != null)
//                removedEntityMap.put(instance.getIdRequired(), model);
//            if (instance.getId() != null) {
//                entityMap.remove(instance.getIdRequired());
//            }
        }
    }

    @Override
    public boolean isRemoved(Object entity) {
        var instance = getInstance(entity);
        return instance.isRemoved();
    }

    @Override
    public <T> T getRemoved(Class<T> entityClass, Id id) {
        return entityClass.cast(getEntity(entityClass, instanceContext.getRemoved(id)));
    }

    @Override
    public boolean isNewEntity(Object entity) {
        var instance = model2instance.get(entity);
        if (instance != null)
            return instance.isNew();
        else
            return parent == null || parent.isNewEntity(entity);
    }

    public boolean isPersisted(Object entity) {
        return !isNewEntity(entity);
    }

    protected <T> void beforeGetModel(Class<T> klass, Instance instance) {
    }

    @Override
    public boolean containsEntity(Object entity) {
        return entities.contains(entity) || parent != null && parent.containsEntity(entity);
    }

    public boolean containsKey(EntityKey entityKey) {
        return entityMap.containsKey(entityKey.id());
    }

    @Override
    public <T> T createEntity(Instance instance, Mapper<T, ?> mapper) {
        NncUtils.requireNull(instance.getMappedEntity(), "Entity was already created");
        var entity = instance.tryGetId() != null ? entityMap.get(instance.getId()) : null;
        if(entity == null && mapper.isProxySupported()) {
            entity = mapper.createModelProxyHelper(mapper.getEntityClass());
            if(entity instanceof IdInitializing idInitializing && instance.tryGetId() != null)
                idInitializing.initId(instance.getId());
    }
        if (entity != null) {
            addMapping(entity, instance);
            mapper.initEntityHelper(entity, instance, objectInstanceMap);
            if(entity instanceof ProxyObject)
                EntityUtils.setProxyState(entity, EntityMethodHandler.State.INITIALIZED);
        } else {
            entity = mapper.getEntityClass().cast(mapper.createEntityHelper(instance, objectInstanceMap));
            addMapping(entity, instance);
        }
//        if (entity instanceof LoadAware loadAware)
//            loadAware.onLoad();
        return mapper.getEntityClass().cast(entity);
    }

    @Override
    public void onPatchBuild() {
        updateInstances();
    }

    @Override
    public boolean onChange(Instance instance) {
        if (instance instanceof ClassInstance classInstance && classInstance.getType().getTypeTag() > 0) {
            var entity = getEntity(Object.class, classInstance);
            if (entity instanceof ChangeAware changeAware && changeAware.isChangeAware()) {
                changeAware.onChange(classInstance, this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onRemove(Instance instance) {
        var entity = instance.getMappedEntity();
        if (entity instanceof PostRemovalAware removalAware) {
            removalAware.postRemove(this);
            return true;
        }
        return false;
    }

    @Override
    public void afterContextIntIds() {
        try (var ignored = getProfiler().enter("BaseEntityContext.afterContextIntIds", true)) {
            forEachEntityInstancePair((object, instance) -> {
                if (isNewEntity(object) && (object instanceof Entity entity) && instance.setAfterContextInitIdsNotified()) {
                    if (entity.afterContextInitIds())
                        updateInstance(object, getInstance(object));
                }
            });
        }
    }

    private void initializeModel(Object model, Instance instance, Mapper<?, ?> mapper) {
//        EntityUtils.ensureProxyInitialized(instance);
        if (!EntityUtils.isModelInitialized(model)) {
            initializeModel0(model, instance, mapper);
        }
    }

    private void initializeModel0(Object model, Instance instance, Mapper<?, ?> def) {
        instance.setMappedEntity(model);
        model2instance.put(model, instance);
        def.initEntityHelper(model, instance, objectInstanceMap);
        if (model instanceof LoadAware loadAware)
            loadAware.onLoad();
        EntityUtils.setProxyState(model, EntityMethodHandler.State.INITIALIZED);
    }

    public <T> T bind(T entity) {
//        if(model instanceof Entity entity && entity.isEphemeralEntity())
//            throw new IllegalArgumentException("Can not bind an ephemeral entity");
        NncUtils.requireTrue(EntityUtils.tryGetPhysicalId(entity) == null, "Can not bind a persisted entity");
        if (this.containsEntity(entity))
            return entity;
        newInstance(entity);
        return entity;
    }

    public void initIdManually(Object model, Id id) {
        var instance = getInstance(model);
        if (instance.tryGetTreeId() != null) {
            throw new InternalException("Model " + model + " already its id initialized");
        }
        NncUtils.requireNonNull(instanceContext).initIdManually(instance, id);
    }

    public Flow getFlow(Id id) {
        return getEntity(Flow.class, id);
    }

    public ScopeRT getScope(Id id) {
        return getEntity(ScopeRT.class, id);
    }

    public Field getField(Id id) {
        return getEntity(Field.class, id);
    }

    public NodeRT getNode(Id id) {
        return getEntity(NodeRT.class, id);
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
    public boolean containsEntity(Class<?> entityType, Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public <T> T getBufferedEntity(Class<T> entityClass, Id id) {
        var found = entityMap.get(id);
        if (found != null)
            return entityClass.cast(found);
        if (parent != null)
            return parent.getBufferedEntity(entityClass, id);
        else
            return null;
    }

//    public <T> T getEntity(Class<T> entityType, Id id) {
//        return getEntity(entityType, instanceContext.get(id));
//    }

//    private void ensureNotRemoved(Object entity) {
//        if (entity instanceof Entity e)
//            e.ensureNotRemoved();
//        else {
//            if (removedEntities.contains(entity))
//                throw new InternalException(String.format("'%s' is already removed", entity));
//        }
//    }

    @Override
    public <T> @Nullable T getEntity(Class<T> entityType, Id id) {
        var existing = entityMap.get(id);
        if(existing != null)
            return entityType.cast(existing);
        if(parent != null && parent.containsEntity(entityType, id))
            return parent.getEntity(entityType, id);
        if(id.isTemporary())
            return null;
        var typeTag = id.getTypeTag(this, this);
        assert TypeTags.isSystemTypeTag(typeTag);
        var mapper = getDefContext().tryGetMapper(typeTag);
        if (mapper == null || mapper instanceof DirectDef<?>)
            return entityType.cast(instanceContext.createReference(id));
        var castedMapper = mapper.as(entityType);
        if (castedMapper.isProxySupported()) {
            var proxy = EntityProxyFactory.getProxy(
                    castedMapper.getEntityClass(),
                    id,
                    k -> castedMapper.getEntityClass().cast(mapper.createModelProxyHelper(k)),
                    // Instance load will trigger entity initialization, see onInstanceInitialized
                    m -> instanceContext.get(id)
            );
            entityMap.put(id, proxy);
            entities.add(proxy);
            return proxy;
        } else
            return getEntity(entityType, instanceContext.get(id));
//        if (id.tryGetTreeId() == null && !instanceContext.contains(id))
//            return null;
//        if(instanceContext.containsId(id)) {
//            var instance = instanceContext.get(id);
//            return getEntity(entityType, instance);
//        }
//        var resolvedMapper = getDefContext().tryGetMapper(id.getTypeTag(this, this));
//        if (resolvedMapper == null || resolvedMapper instanceof DirectDef<?>)
//            return entityType.cast(instanceContext.createReference(id));
//        var mapper = resolvedMapper.as(entityType);
//        if (!mapper.isProxySupported())
//            return getEntity(entityType, instanceContext.get(id));
//        return EntityProxyFactory.getProxy(
//                mapper.getEntityClass(),
//                id,
//                k -> mapper.getEntityClass().cast(mapper.createModelProxyHelper(k)),
//                m -> instanceContext.get(id)
//        );
//        if (id.isEmpty())
//            return null;
//        var id = id.toId();
//        if (id.tryGetTreeId() == null && !instanceContext.contains(id))
//            return null;
//        var existing = entityMap.get(id);
//        if (existing != null)
//            return entityType.cast(existing);
//        if (parent != null && parent.containsEntity(entityType, id))
//            return parent.getEntity(entityType, id);
//        var resolvedMapper = getDefContext().tryGetMapper(id.getTypeTag(this, this));
//        if (resolvedMapper == null || resolvedMapper instanceof DirectDef<?>)
//            return entityType.cast(instanceContext.createReference(id));
//        var mapper = resolvedMapper.as(entityType);
//        T entity;
//        if (mapper.isProxySupported()) {
//            final Mapper<?, ?> defFinal = mapper;
//            entity = EntityProxyFactory.getProxy(
//                    mapper.getEntityClass(),
//                    id,
//                    k -> mapper.getEntityClass().cast(defFinal.createModelProxyHelper(k)),
//                    m -> initializeModel(m, instanceContext.get(id), defFinal)
//            );
//        } else {
//            var instance = instanceContext.get(id);
//            entity = mapper.getEntityClass().cast(mapper.createEntityHelper(instance, objectInstanceMap));
//            instance.setMappedEntity(entity);
//            model2instance.put(entity, instance);
//            if (entity instanceof LoadAware loadAware)
//                loadAware.onLoad();
//        }
//        entityMap.put(id, entity);
//        return entity;
    }

    public <T> T get(Class<T> klass, Id id) {
        return klass.cast(getEntity(klass, id));
    }


    private Profiler.Entry enter(String name) {
        return getProfiler().enter(name);
    }

    public void initIds() {
        try (var ignored = enter("initIds")) {
            NncUtils.requireNonNull(instanceContext);
            flushAndWriteInstances();
            instanceContext.initIds();
        }
    }

    public void finish() {
        for (Object o : model2instance.keySet()) {
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
//        updateInstances();
//            validateInstances();
        try (var ignored1 = getProfiler().enter("writeInstances")) {
            writeInstances(instanceContext);
        }
    }

    public void close() {
        if (instanceContext != null) {
            instanceContext.close();
        }
    }

    @Override
    public boolean isFinished() {
        return instanceContext != null && instanceContext.isFinished();
    }

    protected Collection<Object> models() {
        return Collections.unmodifiableCollection(entities);
    }

    public void flushAndWriteInstances() {
        NncUtils.requireNonNull(instanceContext);
        flush();
        writeInstances(instanceContext);
    }

    protected void flush() {
    }

    protected void writeInstances(IInstanceContext instanceContext) {
    }

    @Override
    public void updateInstances() {
        try (var profilerEntry = getProfiler().enter("updateInstances")) {
            profilerEntry.addMessage("numInstances", model2instance.size());
//            var list = new ArrayList<>(model2instance.entrySet());
            forEachEntityInstancePair(this::updateInstance);
//            for (var entry : list) {
//                updateInstance(entry.getKey(), entry.getValue());
//            }
//            new IdentityHashMap<>(model2instance).forEach(this::updateInstance);
        }
    }

    private void forEachEntityInstancePair(BiConsumer<Object, Instance> action) {
        instanceContext.forEach(instance -> {
            var entity = instance.getMappedEntity();
            if (entity != null)
                action.accept(entity, instance);
        });
    }

    @Override
    public void update(Object object) {
        updateInstance(object, getInstance(object));
    }

    private void updateInstance(Object object, Instance instance) {
//        try(var ignored = getProfiler().enter("updateInstance")) {
        if (isModelInitialized(object) && !instance.isRemoved()) {
            var mapper = getDefContext().getMapper(instance.getType());
            mapper.updateInstanceHelper(object, instance, objectInstanceMap);
            updateMemIndex(object);
        }
//        }
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Object... values) {
        try (var ignored = enter("selectByKey")) {
            NncUtils.requireNonNull(instanceContext, "instanceContext required");
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            var instances = instanceContext.selectByKey(indexKey);
            return createEntityList(indexDef.getType(), instances);
        }
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
//        NncUtils.requireTrue(indexDef.isUnique());
        try (var ignored = enter("selectByUniqueKey")) {
            NncUtils.requireNonNull(instanceContext, "instanceContext required");
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            var instance = instanceContext.selectFirstByKey(indexKey);
            return instance == null ? null : createEntityList(indexDef.getType(), List.of(instance)).get(0);
        }
    }

    @Override
    public long getAppId(Object model) {
        if (entities.contains(model)) {
            return getAppId();
        } else if (parent != null) {
            return parent.getAppId(model);
        } else {
            throw new InternalException("Model " + model + " is not contained in the context");
        }
    }

    @Override
    public <T> List<T> query(EntityIndexQuery<T> query) {
        Class<T> javaClass = query.indexDef().getType();
        var instances = instanceContext.query(convertToInstanceIndexQuery(query));
        return createEntityList(javaClass, instances);
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return instanceContext.count(convertToInstanceIndexQuery(query));
    }

    private InstanceIndexQuery convertToInstanceIndexQuery(EntityIndexQuery<?> query) {
        var index = getDefContext().getIndexConstraint(query.indexDef());
        return new InstanceIndexQuery(
                index,
                query.from() != null ? new InstanceIndexKey(index, NncUtils.map(query.from().values(), this::resolveInstance)) : null,
                query.to() != null ? new InstanceIndexKey(index, NncUtils.map(query.to().values(), this::resolveInstance)) : null,
                query.desc(),
                query.limit()
        );
    }

    private InstanceIndexQueryItem createInstanceQueryItem(Index indexConstraint,
                                                           Class<?> javaClass,
                                                           EntityIndexQueryItem queryItem) {
        Klass type = indexConstraint.getDeclaringType();
        Field field = type.getFieldByJavaField(ReflectionUtils.getField(javaClass, queryItem.fieldName()));
        return new InstanceIndexQueryItem(
                indexConstraint.getFieldByTypeField(field),
                queryItem.operator(),
                resolveInstance(queryItem.value())
        );
    }

    @Override
    public Value resolveInstance(Object value) {
        if (value == null) {
            return Instances.nullInstance();
        }
        if (this.containsEntity(value)) {
            if (!EntityUtils.isModelInitialized(value))
                return instanceContext.createReference(((Entity) value).getId());
            else
                return getInstance(value).getReference();
        }
        return Instances.serializePrimitive(value, getDefContext()::getType);
    }

    private <T> List<T> createEntityList(Class<T> javaType, List<? extends Reference> instances) {
        return EntityProxyFactory.getProxy(
                new TypeReference<ReadonlyArray<T>>() {
                },
                null,
                table -> table.initialize(
                        ParameterizedTypeImpl.create(table.getRawClass(), Object.class),
                        NncUtils.map(
                                instances,
                                inst -> inst.tryGetId() != null ?
                                        getEntity(javaType, inst.getId()) : getEntity(javaType, inst.resolve())
                        )
                ),
                k -> ReadonlyArray.createProxy(k, javaType)
        ).toList();
    }

    @Override
    public long getAppId() {
        NncUtils.requireNonNull(instanceContext);
        return instanceContext.getAppId();
    }

    public boolean remove(Object entity) {
        batchRemove(List.of(entity));
        return true;
    }

    public void batchRemove(List<?> entities) {
        entities.forEach(EntityUtils::ensureTreeInitialized);
        if (parent != null) {
            List<?> parentEntities = NncUtils.filter(entities, parent::containsEntity);
            if (!parentEntities.isEmpty()) {
                parent.batchRemove(parentEntities);
                entities = NncUtils.exclude(entities, parent::containsEntity);
            }
        }
        var instances = beforeRemove(entities);
//        updateInstances();
        if (NncUtils.isEmpty(instances))
            return;
        if (instanceContext != null)
            instanceContext.batchRemove(instances);
        else
            instances.forEach(this::onInstanceRemoved);
    }

    private Set<Instance> beforeRemove(List<?> entities) {
        var instancesToRemove = new IdentitySet<Instance>();
        for (Object entity : entities) {
            beforeRemove0(entity, instancesToRemove);
        }
        return instancesToRemove;
    }

    private void beforeRemove0(Object object, Set<Instance> instancesToRemove) {
        if (object instanceof Instance instance) {
            instancesToRemove.add(instance);
            return;
        }
        var instance = model2instance.get(object);
        if (instance == null || instancesToRemove.contains(instance))
            return;
        EntityUtils.ensureProxyInitialized(object);
        instancesToRemove.add(instance);
        var cascades = new IdentitySet<>(getNonEphemeralChildren(object));
        if (object instanceof Entity entity && entity.getParentEntity() != null) {
            var parentInst = getInstance(entity.getParentEntity());
            if (!instancesToRemove.contains(parentInst)) {
                if (entity.getParentEntity() instanceof ChildArray<?> array)
                    array.remove(entity);
                else {
                    ReflectionUtils.set(
                            entity.getParentEntity(),
                            requireNonNull(entity.getParentEntityField()),
                            null
                    );
                }
                updateInstance(entity.getParentEntity(), parentInst);
            }
        }
        if (object instanceof RemovalAware removalAware)
            cascades.addAll(removalAware.beforeRemove(this));
        if (NncUtils.isNotEmpty(cascades)) {
            for (Object cascade : cascades) {
                beforeRemove0(cascade, instancesToRemove);
            }
        }
    }

    private Set<Object> getNonEphemeralChildren(Object object) {
        Set<Object> children = new IdentitySet<>();
        Type type = getDefContext().getType(getRuntimeType(object));
        if (object instanceof ReadonlyArray<?> array) {
            if (array instanceof ChildArray<?>)
                children.addAll(NncUtils.filter(array, Objects::nonNull));
        } else if (type instanceof ClassType classType) {
            var klass = classType.resolve();
            for (Field field : klass.getAllFields()) {
                if (field.isChild()) {
                    Object child = ReflectionUtils.get(object, getDefContext().getJavaField(field));
                    if (child != null && !EntityUtils.isEphemeral(child))
                        children.add(child);
                }
            }
        }
        return children;
    }

    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    @SuppressWarnings("unused")
    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Object... fieldValues) {
        NncUtils.requireNonNull(instanceContext);
        IndexKeyRT indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        var instance = instanceContext.selectFirstByKey(indexKey);
        return NncUtils.get(instance, i -> getEntity(entityType, requireNonNull(i.tryGetId())));
    }

    private IndexKeyRT createIndexKey(IndexDef<?> uniqueConstraintDef, Object... values) {
        var constraint = getDefContext().getIndexConstraint(uniqueConstraintDef);
        NncUtils.requireNonNull(constraint);
        return constraint.createIndexKeyByModels(Arrays.asList(values), this);
    }

    @Override
    public Instance getInstance(Object entity) {
        EntityUtils.ensureProxyInitialized(entity);
        if (parent != null && parent.containsEntity(entity)) {
            return parent.getInstance(entity);
        }
        var instance = model2instance.get(entity);
        if (instance == null) {
            instance = newInstance(entity);
        }
        return instance;
//        return Objects.requireNonNull(model2instance.get(entity), () -> "Failed to get instance for entity " + entity + "@" + System.identityHashCode(entity));
    }

    public ClassInstance getEntityInstance(Entity entity) {
        return (ClassInstance) getInstance(entity);
    }

    /**
     * Bind a new entity to the context with the mapped instance
     */
    protected final void addBinding(Object model, Instance instance) {
        addMapping(model, instance);
        if (model instanceof BindAware bindAware)
            bindAware.onBind(this);
    }

    private Instance newInstance(Object object) {
        Mapper<?, ?> mapper = getDefContext().getMapperByEntity(object);
//        if (mapper.isProxySupported()) {
//            var instance = mapper.allocateInstanceHelper(object, objectInstanceMap, EntityUtils.tryGetId(object));
//            addBinding(object, instance);
//            mapper.initInstanceHelper(instance, object, objectInstanceMap);
//            updateMemIndex(object);
//            return instance;
//        } else {
            var instance = mapper.createInstanceHelper(object, objectInstanceMap, null);
//            addBinding(object, instance);
            updateMemIndex(object);
            return instance;
//        }
    }

    public Collection<Instance> instances() {
        return model2instance.values();
    }

    /*
    Add a mapping between an entity and an instance. Client shouldn't call this method directly if the
    entity is new, use addBinding instead.
     */
    protected void addMapping(Object model, Instance instance) {
        if (instance.getMappedEntity() != null)
            throw new IllegalStateException("Entity " + model + " is already mapped");
        model2instance.put(model, instance);
        instance.setMappedEntity(model);
        entities.add(model);
        if (model instanceof Entity entity) {
            var id = entity.tryGetId();
            if(id != null) {
                if (id instanceof TmpId)
                    instance.initId(id);
                entityMap.put(id, entity);
            }
        }
        if (!manualInstanceWriting()) {
            if (!instanceContext.containsInstance(instance))
                instanceContext.bind(instance);
        }
    }

    protected void updateMemIndex(Object object) {
//        try(var ignored = getProfiler().enter("updateMemIndex")) {
        var instance = getInstance(object);
        if (!manualInstanceWriting() && instanceContext != null && instance instanceof ClassInstance clsInst) {
            instanceContext.updateMemoryIndex(clsInst);
        }
//        }
    }

    @Override
    public ObjectInstanceMap getObjectInstanceMap() {
        return objectInstanceMap;
    }

    protected boolean manualInstanceWriting() {
        return false;
    }

    @Override
    public Type getType(Class<?> javaType) {
        return getDefContext().getType(javaType);
    }

    @Override
    public List<Object> scan(long start, long limit) {
        return NncUtils.map(
                instanceContext.scan(start, limit).instances(),
                inst -> getEntity(Object.class, inst.getId())
        );
    }
}
