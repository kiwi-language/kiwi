package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.RefDTO;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.Flow;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.*;
import tech.metavm.util.*;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.entity.EntityUtils.*;

public abstract class BaseEntityContext implements CompositeTypeFactory, IEntityContext, ContextListener {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntityContext.class);

    private final Map<Long, Object> entityMap = new HashMap<>();
    private final IInstanceContext instanceContext;
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    private final IdentitySet<Object> removedEntities = new IdentitySet<>();
    private final Map<Long, Object> removedEntityMap = new HashMap<>();
    private final @Nullable IEntityContext parent;
    private final GenericContext genericContext;
    private final Map<TypeCategory, CompositeTypeContext<?>> compositeTypeContexts = new IdentityHashMap<>();

    public BaseEntityContext(IInstanceContext instanceContext, @Nullable IEntityContext parent) {
        this.instanceContext = instanceContext;
        this.parent = parent;
        initCompositeTypeContexts();
        genericContext = new GenericContext(this, getTypeFactory(),
                NncUtils.get(parent, IEntityContext::getGenericContext));
        instanceContext.addListener(this);
    }

    protected abstract TypeFactory getTypeFactory();

    private void initCompositeTypeContexts() {
        compositeTypeContexts.put(TypeCategory.READ_WRITE_ARRAY,
                new ArrayTypeContext(this, ArrayKind.READ_WRITE,
                        NncUtils.get(parent, p -> p.getArrayTypeContext(ArrayKind.READ_WRITE))));
        compositeTypeContexts.put(TypeCategory.READ_ONLY_ARRAY,
                new ArrayTypeContext(this, ArrayKind.READ_ONLY,
                        NncUtils.get(parent, p -> p.getArrayTypeContext(ArrayKind.READ_ONLY))));
        compositeTypeContexts.put(TypeCategory.CHILD_ARRAY, new ArrayTypeContext(this, ArrayKind.CHILD,
                NncUtils.get(parent, p -> p.getArrayTypeContext(ArrayKind.CHILD))));
        compositeTypeContexts.put(TypeCategory.UNCERTAIN, new UncertainTypeContext(this,
                NncUtils.get(parent, IEntityContext::getUncertainTypeContext)));
        compositeTypeContexts.put(TypeCategory.FUNCTION, new FunctionTypeContext(this,
                NncUtils.get(parent, IEntityContext::getFunctionTypeContext)));
        compositeTypeContexts.put(TypeCategory.UNION, new UnionTypeContext(this,
                NncUtils.get(parent, IEntityContext::getUnionTypeContext)));
        compositeTypeContexts.put(TypeCategory.INTERSECTION, new IntersectionTypeContext(this,
                NncUtils.get(parent, IEntityContext::getIntersectionTypeContext)));
    }

    @Override
    public <T> List<T> getByType(Class<? extends T> javaType, T startExclusive, long limit) {
        NncUtils.requireNonNull(instanceContext);
        Instance startInstance = NncUtils.get(startExclusive, this::getInstance);
        Type type = getDefContext().getType(javaType);
        List<Instance> instances = instanceContext.getByType(type, startInstance, limit);
        return NncUtils.map(instances, i -> getEntity(javaType, i));
    }

    @Override
    public boolean existsInstances(Class<?> type) {
        return NncUtils.isNotEmpty(getByType(type, null, 1));
    }

    @Override
    public <T> T getEntity(Class<T> klass, Instance instance) {
        return getEntity(klass, instance, null);
    }

    public @Nullable IEntityContext getParent() {
        return parent;
    }

    @Override
    public <T> T getEntity(Class<T> entityClass, Instance instance, @Nullable ModelDef<T, ?> def) {
        if (instance instanceof PrimitiveInstance primitiveInstance)
            return InstanceUtils.deserializePrimitive(primitiveInstance, entityClass);
        var found = instance.getMappedEntity();
        if (found != null)
            return entityClass.cast(found);
        NncUtils.requireNonNull(instance.getContext());
        if (def == null) {
            var resolvedDef = getDefContext().tryGetDef(instance.getType());
            if (resolvedDef == null || resolvedDef instanceof DirectDef<?>)
                return entityClass.cast(instance);
            def = resolvedDef.as(entityClass);
        }
        if (instance.getContext() == instanceContext)
            return createEntity(instance, def);
        if (parent != null)
            return parent.createEntity(instance, def);
        else
            throw new InternalException(String.format("Instance '%s' is not contained in the context", instance));
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
    public void invalidateCache(long id) {
        var entity = get(Object.class, id);
        var instance = getInstance(entity);
        instanceContext.invalidateCache(instance);
    }

    @Override
    public void onInstanceIdInit(Instance instance) {
        Object model = instance.getMappedEntity();
        if (model != null && model2instance.containsKey(model)) {
            entityMap.put(instance.getIdRequired(), model);
            if (model instanceof IdInitializing idInitializing) {
                NncUtils.requireNull(idInitializing.getId());
                idInitializing.initId(instance.getIdRequired());
            }
        }
    }

    public void onInstanceInitialized(Instance instance) {
        Object model = instance.getMappedEntity();
        if (model != null && model2instance.containsKey(model)) {
            var def = getDefContext().getDefByModel(model);
            initializeModel0(model, instance, def);
        }
    }

    @Override
    public void onInstanceRemoved(Instance instance) {
        Object model = instance.getMappedEntity();
        if (model != null && model2instance.containsKey(model)) {
            if (model instanceof Entity entity)
                entity.setRemoved();
            model2instance.remove(model);
            removedEntities.add(model);
            if (instance.getId() != null)
                removedEntityMap.put(instance.getIdRequired(), model);
            if (instance.getId() != null) {
                entityMap.remove(instance.getIdRequired());
            }
        }
    }

    @Override
    public boolean isRemoved(Object entity) {
        return removedEntities.contains(entity);
    }

    @Override
    public <T> T getRemoved(Class<T> entityClass, long id) {
        return entityClass.cast(removedEntityMap.get(id));
    }

    @Override
    public boolean isNewEntity(Object entity) {
        Instance instance = model2instance.get(entity);
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
    public GenericContext getGenericContext() {
        return genericContext;
    }

    @Override
    public FunctionTypeContext getFunctionTypeContext() {
        return (FunctionTypeContext) getCompositeTypeContext(TypeCategory.FUNCTION);
    }

    @Override
    public UncertainTypeContext getUncertainTypeContext() {
        return (UncertainTypeContext) getCompositeTypeContext(TypeCategory.UNCERTAIN);
    }

    @Override
    public ArrayTypeContext getArrayTypeContext(ArrayKind kind) {
        return (ArrayTypeContext) getCompositeTypeContext(kind.category());
    }

    @Override
    public UnionTypeContext getUnionTypeContext() {
        return (UnionTypeContext) getCompositeTypeContext(TypeCategory.UNION);
    }

    @Override
    public IntersectionTypeContext getIntersectionTypeContext() {
        return (IntersectionTypeContext) getCompositeTypeContext(TypeCategory.INTERSECTION);
    }

    @Override
    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments) {
        return genericContext.getParameterizedType(template, typeArguments);
    }

    @Override
    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType) {
        return getFunctionTypeContext().get(parameterTypes, returnType);
    }

    @Override
    public CompositeTypeContext<?> getCompositeTypeContext(TypeCategory category) {
        return NncUtils.requireNonNull(
                compositeTypeContexts.get(category),
                "Can not find composite type context for category '" + category + "'"
        );
    }

    @Override
    public Collection<CompositeTypeContext<?>> getCompositeTypeContexts() {
        return Collections.unmodifiableCollection(compositeTypeContexts.values());
    }

    @Override
    public boolean containsModel(Object model) {
        return model2instance.containsKey(model) || parent != null && parent.containsModel(model);
    }

    public boolean containsKey(EntityKey entityKey) {
        return entityMap.containsKey(entityKey.id());
    }

    @Override
    public <T> T createEntity(Instance instance, ModelDef<T, ?> def) {
        T model;
        if (def.isProxySupported()) {
            final ModelDef<?, ?> defFinal = def;
            model = EntityProxyFactory.getProxy(
                    def.getJavaClass(),
                    instance.getId(),
                    k -> def.getJavaClass().cast(defFinal.createModelProxyHelper(k)),
                    m -> initializeModel(m, instance, defFinal)
            );
        } else
            model = def.getJavaClass().cast(def.createModelHelper(instance, this));
        addMapping(model, instance);
        return model;
    }

    @Override
    public void afterContextIntIds() {
        try (var ignored = getProfiler().enter("BaseEntityContext.afterContextIntIds", true)) {
            for (Object model : new ArrayList<>(model2instance.keySet())) {
                if (isNewEntity(model) && (model instanceof Entity entity)) {
                    if (entity.afterContextInitIds()) {
                        updateInstance(model, getInstance(model));
                    }
                }
            }
        }
    }

    private void initializeModel(Object model, Instance instance, ModelDef<?, ?> def) {
        EntityUtils.ensureProxyInitialized(instance);
        if (!EntityUtils.isModelInitialized(model)) {
            initializeModel0(model, instance, def);
        }
    }

    private void initializeModel0(Object model, Instance instance, ModelDef<?, ?> def) {
        def.initModelHelper(model, instance, this);
        EntityUtils.setProxyState(model, EntityMethodHandler.State.INITIALIZED);
    }

    public <T> T bind(T model) {
        NncUtils.requireTrue(EntityUtils.tryGetId(model) == null, "Can not bind a persisted entity");
        if (containsModel(model)) {
            return model;
        }
        newInstance(model);
        return model;
    }

    public void initIdManually(Object model, long id) {
        Instance instance = getInstance(model);
        if (instance.getId() != null) {
            throw new InternalException("Model " + model + " already its id initialized");
        }
        NncUtils.requireNonNull(instanceContext).initIdManually(instance, id);
    }

    public Flow getFlow(long id) {
        return getEntity(Flow.class, id);
    }

    public ScopeRT getScope(long id) {
        return getEntity(ScopeRT.class, id);
    }

    public Field getField(long id) {
        return getEntity(Field.class, id);
    }

    public NodeRT<?> getNode(long id) {
        return getEntity(NodeRT.class, id);
    }


    @SuppressWarnings("unused")
    public Index getUniqueConstraint(long id) {
        return getEntity(Index.class, id);
    }

    @SuppressWarnings("unused")
    public CheckConstraint getCheckConstraint(long id) {
        return getEntity(CheckConstraint.class, id);
    }

    public <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    @Override
    public boolean containsEntity(Class<?> entityType, long id) {
        return entityMap.containsKey(id);
    }

    @Override
    public <T> T getBufferedEntity(Class<T> entityClass, long id) {
        var found = entityMap.get(id);
        if (found != null)
            return entityClass.cast(found);
        if (parent != null)
            return parent.getBufferedEntity(entityClass, id);
        else
            return null;
    }

    public <T> T getEntity(Class<T> entityType, long id) {
        var found = getBufferedEntity(entityType, id);
        if (found != null) {
            ensureNotRemoved(found);
            return found;
        } else
            return getEntity(entityType, instanceContext.get(id));
    }

    private void ensureNotRemoved(Object entity) {
        if (entity instanceof Entity e)
            e.ensureNotRemoved();
        else {
            if (removedEntities.contains(entity))
                throw new InternalException(String.format("'%s' is already removed", entity));
        }
    }

    @Override
    public <T> T getEntity(Class<T> entityType, RefDTO reference) {
        var instance = Objects.requireNonNull(instanceContext).get(reference);
        return instance != null ? getEntity(entityType, instance) : null;
    }

    public <T> T get(Class<T> klass, long id) {
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
        instanceContext.finish();
    }

    @Override
    public void beforeFinish() {
        try (var ignored1 = getProfiler().enter("flush")) {
            flush();
        }
        updateInstances();
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
        return model2instance.keySet();
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

    private void updateInstances() {
        try (var ignored = getProfiler().enter("updateInstances")) {
            new IdentityHashMap<>(model2instance).forEach(this::updateInstance);
        }
    }

//    validation is migrated to InstanceContext.finishInternal
//    private void validateInstances() {
//        try(var ignored = getProfiler().enter("validateInstances", true)) {
//            for (Object model : new ArrayList<>(model2instance.keySet())) {
//                if (model instanceof Entity entity) {
//                    entity.validate();
//                }
//            }
//        }
//    }

    private void updateInstance(Object model, Instance instance) {
        if (isModelInitialized(model)) {
            ModelDef<?, ?> def = getDefContext().getDef(instance.getType());
            def.updateInstanceHelper(model, instance, this);
        }
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Object... values) {
        try (var ignored = enter("selectByKey")) {
            NncUtils.requireNonNull(instanceContext, "instanceContext required");
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            List<Instance> instances = instanceContext.selectByKey(indexKey);
            return createEntityList(indexDef.getType(), instances);
        }
    }

    @Nullable
    @Override
    public <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object... values) {
        try (var ignored = enter("selectByUniqueKey")) {
            NncUtils.requireNonNull(instanceContext, "instanceContext required");
            IndexKeyRT indexKey = createIndexKey(indexDef, values);
            Instance instance = instanceContext.selectByUniqueKey(indexKey);
            return instance == null ? null : createEntityList(indexDef.getType(), List.of(instance)).get(0);
        }
    }

    @Override
    public long getAppId(Object model) {
        if (model2instance.containsKey(model)) {
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
        List<Instance> instances = instanceContext.query(convertToInstanceIndexQuery(query));
        return createEntityList(javaClass, instances);
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return instanceContext.count(convertToInstanceIndexQuery(query));
    }

    private InstanceIndexQuery convertToInstanceIndexQuery(EntityIndexQuery<?> query) {
        Class<?> javaClass = query.indexDef().getType();
        Index indexConstraint = getDefContext().getIndexConstraint(query.indexDef());
        return new InstanceIndexQuery(
                indexConstraint,
                NncUtils.map(
                        query.items(),
                        item -> createInstanceQueryItem(indexConstraint, javaClass, item)
                ),
                query.desc(),
                query.limit()
        );
    }

    private InstanceIndexQueryItem createInstanceQueryItem(Index indexConstraint,
                                                           Class<?> javaClass,
                                                           EntityIndexQueryItem queryItem) {
        ClassType type = indexConstraint.getDeclaringType();
        Field field = type.getFieldByJavaField(ReflectUtils.getField(javaClass, queryItem.fieldName()));
        return new InstanceIndexQueryItem(
                indexConstraint.getFieldByTypeField(field),
                queryItem.operator(),
                resolveInstance(queryItem.value())
        );
    }

    private Instance resolveInstance(Object value) {
        if (value == null) {
            return InstanceUtils.nullInstance();
        }
        if (containsModel(value)) {
            return getInstance(value);
        }
        return InstanceUtils.serializePrimitive(value, getDefContext()::getType);
    }

    private <T> List<T> createEntityList(Class<T> javaType, List<Instance> instances) {
        return EntityProxyFactory.getProxy(
                new TypeReference<ReadonlyArray<T>>() {
                },
                null,
                table -> table.initialize(
                        NncUtils.map(
                                instances,
                                inst -> getEntity(javaType, inst)
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
        if (parent != null) {
            List<?> parentEntities = NncUtils.filter(entities, parent::containsModel);
            if (!parentEntities.isEmpty()) {
                parent.batchRemove(parentEntities);
                entities = NncUtils.exclude(entities, parent::containsModel);
            }
        }
        Set<Instance> instances = beforeRemove(entities);
//        updateInstances();
        if (NncUtils.isEmpty(instances))
            return;
        if (instanceContext != null) {
            instanceContext.batchRemove(instances);
        } else {
            instances.forEach(this::onInstanceRemoved);
        }
    }

    private Set<Instance> beforeRemove(List<?> entities) {
        Set<Instance> instancesToRemove = new IdentitySet<>();
        for (Object entity : entities) {
            beforeRemove0(entity, instancesToRemove);
        }
        return instancesToRemove;
    }

    private void beforeRemove0(Object model, Set<Instance> instancesToRemove) {
        if (model instanceof Instance instance) {
            instancesToRemove.add(instance);
            return;
        }
        Instance instance = model2instance.get(model);
        if (instance == null) {
            return;
        }
        if (instancesToRemove.contains(instance)) {
            return;
        }
        EntityUtils.ensureProxyInitialized(model);
        instancesToRemove.add(instance);
        Set<Object> cascades = new IdentitySet<>(getChildEntities(model));
        if (model instanceof Entity entity && entity.getParentEntity() != null) {
            var parentInst = getInstance(entity.getParentEntity());
            if (!instancesToRemove.contains(parentInst)) {
                if (entity.getParentEntity() instanceof ChildArray<?> array) {
                    array.remove(entity);
                } else {
                    ReflectUtils.set(
                            entity.getParentEntity(),
                            NncUtils.requireNonNull(entity.getParentEntityField()),
                            null
                    );
                }
                updateInstance(entity.getParentEntity(), parentInst);
            }
        }
        if (model instanceof RemovalAware removalAware) {
            cascades.addAll(removalAware.beforeRemove(this));
        }
        if (NncUtils.isNotEmpty(cascades)) {
            for (Object cascade : cascades) {
                beforeRemove0(cascade, instancesToRemove);
            }
        }
    }

    @Override
    public UncertainType getUncertainType(Type lowerBound, Type upperBound) {
        return getUncertainTypeContext().get(List.of(lowerBound, upperBound));
    }

    public Set<CompositeType> getNewCompositeTypes() {
        Set<CompositeType> newTypes = new HashSet<>();
        for (CompositeTypeContext<?> ctx : compositeTypeContexts.values()) {
            newTypes.addAll(ctx.getNewTypes());
        }
        return newTypes;
    }

    private Set<Object> getChildEntities(Object entity) {
        Set<Object> childModels = new IdentitySet<>();
        Type type = getDefContext().getType(getEntityRuntimeType(entity));
        if (entity instanceof ReadonlyArray<?> table) {
            if (table instanceof ChildArray<?>) {
                childModels.addAll(NncUtils.filter(table, Objects::nonNull));
            }
        } else if (type instanceof ClassType classType) {
            for (Field field : classType.getAllFields()) {
                if (field.isChild()) {
                    Object child = ReflectUtils.get(entity, getDefContext().getJavaField(field));
                    if (child != null) {
                        childModels.add(child);
                    }
                }
            }
        }
        return childModels;
    }

    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    @SuppressWarnings("unused")
    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Object... fieldValues) {
        NncUtils.requireNonNull(instanceContext);
        IndexKeyRT indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        Instance instance = instanceContext.selectByUniqueKey(indexKey);
        return getEntity(entityType, instance.getIdRequired());
    }

    private IndexKeyRT createIndexKey(IndexDef<?> uniqueConstraintDef, Object... values) {
        Index constraint = getDefContext().getIndexConstraint(uniqueConstraintDef);
        NncUtils.requireNonNull(constraint);
        return constraint.createIndexKeyByModels(Arrays.asList(values), this);
    }

    @Override
    public UnionType getUnionType(Set<Type> members) {
        return getUnionTypeContext().get(new ArrayList<>(members));
    }

    @Override
    public IntersectionType getIntersectionType(Set<Type> types) {
        return getIntersectionTypeContext().get(new ArrayList<>(types));
    }

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind) {
        return getArrayTypeContext(kind).get(List.of(elementType));
    }

    @Override
    public Instance getInstance(Object model) {
        Instance primitiveInst = InstanceUtils.trySerializePrimitive(model, getDefContext()::getType);
        if (primitiveInst != null)
            return primitiveInst;
        if (parent != null && parent.containsModel(model)) {
            return parent.getInstance(model);
        }
        Instance instance = model2instance.get(model);
        if (instance == null) {
            instance = newInstance(model);
        }
        return instance;
    }

    public ClassInstance getEntityInstance(Entity entity) {
        return (ClassInstance) getInstance(entity);
    }

    /**
     * Bind a new entity to the context with the mapped instance
     */
    protected final void addBinding(Object model, Instance instance) {
        addMapping(model, instance);
        if (model instanceof BindAware bindAware) {
            bindAware.onBind(this);
        }
    }

    private Instance newInstance(Object model) {
        ModelDef<?, ?> def = getDefContext().getDefByModel(model);
        if (def.isProxySupported()) {
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType(), tryGetId(model));
            addBinding(model, instance);
            def.initInstanceHelper(instance, model, this);
            updateMemIndex(instance);
            return instance;
        } else {
            Instance instance = def.createInstanceHelper(model, this, null);
            addBinding(model, instance);
            updateMemIndex(instance);
            return instance;
        }
    }

    public Collection<Instance> instances() {
        return model2instance.values();
    }

    /*
    Add a mapping between an entity and an instance. Client shouldn't call this method directly if the
    entity is new, use addBinding instead.
     */
    protected void addMapping(Object model, Instance instance) {
        NncUtils.requireNull(instance.getMappedEntity());
        model2instance.put(model, instance);
        instance.setMappedEntity(model);
        if (model instanceof Entity entity && entity.getTmpId() != null)
            instance.setTmpId(entity.getTmpId());
        if (instance.getId() != null)
            entityMap.put(instance.getIdRequired(), model);
        if (!manualInstanceWriting()) {
            if (!instanceContext.containsInstance(instance))
                instanceContext.bind(instance);
        }
    }

    private void updateMemIndex(Instance instance) {
        if (!manualInstanceWriting() && instanceContext != null && instance instanceof ClassInstance clsInst) {
            instanceContext.updateMemoryIndex(clsInst);
        }
    }

    protected boolean manualInstanceWriting() {
        return false;
    }

    @Override
    public Type getType(Class<?> javaType) {
        return getDefContext().getType(javaType);
    }

    @Override
    public UnionType getNullableType(Type type) {
        return getUnionType(Set.of(type, StandardTypes.getNullType()));
    }
}
