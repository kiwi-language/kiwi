package tech.metavm.entity;

import tech.metavm.dto.InternalErrorCode;
import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.Index;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.entity.EntityUtils.isModelInitialized;
import static tech.metavm.entity.EntityUtils.tryGetId;

public abstract class BaseEntityContext implements CompositeTypeFactory, IEntityContext {

    private final Map<EntityKey, Object> entityMap = new HashMap<>();
    private final @Nullable IInstanceContext instanceContext;
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    private final IdentityHashMap<Instance, Object> instance2model = new IdentityHashMap<>();
    private final IEntityContext parent;

    public BaseEntityContext(@Nullable IInstanceContext instanceContext, IEntityContext parent) {
        this.instanceContext = instanceContext;
        this.parent = parent;
        if(instanceContext != null) {
            instanceContext.addListener(this::onInstanceIdInitialized);
            instanceContext.addRemovalListener(this::onInstanceRemoved);
        }
    }

    @Override
    public <T> List<T> getByType(Class<T> javaType, T startExclusive, long limit) {
        NncUtils.requireNonNull(instanceContext);
        Instance startInstance = NncUtils.get(startExclusive, this::getInstance);
        Type type = getDefContext().getType(javaType);
        List<Instance> instances = instanceContext.getByType(type, startInstance, limit);
        return NncUtils.map(instances, i -> getModel(javaType, i));
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        return getModel(klass, instance, null);
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance, ModelDef<?,?> def) {
        if(!getDefContext().containsTypeDef(instance.getType())) {
            // If there's no java type for the instance type, return the instance
            return klass.cast(instance);
        }
        beforeGetModel(klass, instance);
        if(parent != null && parent.containsInstance(instance)) {
            return parent.getModel(klass, instance);
        }
        beforeGetModel(klass, instance);
        Object model = instance2model.get(instance);
        if(model == null) {
            Class<?> actualClass =
                    getDefContext().getJavaClass(instance.getType());
            java.lang.reflect.Type genericClass =
                    getDefContext().getJavaType(instance.getType());
            model = createModel(actualClass, genericClass, instance, def);
        }
        if(klass.isInstance(model)) {
            return klass.cast(model);
        }
        else {
            throw new InternalException(
                    InternalErrorCode.MODEL_TYPE_MISMATCHED,
                    klass.getName(),
                    EntityUtils.getRealType(model).getName()
            );
        }
    }

    private void onInstanceIdInitialized(Instance instance) {
        Object model = instance2model.get(instance);
        if(model != null) {
            entityMap.put(EntityKey.create(model.getClass(), instance.getId()), model);
            if (model instanceof IdInitializing idInitializing) {
                NncUtils.requireNull(idInitializing.getId());
                idInitializing.initId(instance.getId());
            }
        }
    }

    private void onInstanceRemoved(Instance instance) {
        Object model = instance2model.remove(instance);
        if(model != null) {
            model2instance.remove(model);
            if(instance.getId() != null) {
                entityMap.remove(new EntityKey(EntityUtils.getEntityType(model), instance.getId()));
            }
        }
    }

    protected <T> void beforeGetModel(Class<T> klass, Instance instance) {}

    protected void beforeFinish() {}

    @Override
    public boolean containsInstance(Instance instance) {
        return instance2model.containsKey(instance) || parent != null && parent.containsInstance(instance);
    }

    @Override
    public boolean containsModel(Object model) {
        return model2instance.containsKey(model) || parent != null && parent.containsModel(model);
    }

    public boolean containsKey(EntityKey entityKey) {
        return entityMap.containsKey(entityKey);
    }

    protected <T> T createModel(Class<T> actualClass, java.lang.reflect.Type genericType, Instance instance, ModelDef<?,?> def) {
        if(def == null) {
            def = getDefContext().getDef(genericType);
        }
        T model;
        if(def.isProxySupported()) {
            final ModelDef<?,?> defFinal = def;
            model = EntityProxyFactory.getProxy(
                    actualClass,
                    instance.getId(),
                    k -> actualClass.cast(defFinal.createModelProxyHelper(k)),
                    m -> initializeModel(m, instance, defFinal)
            );
        }
        else {
            model = actualClass.cast(def.createModelHelper(instance, this));
        }
        addMapping(model, instance);
        return model;
    }

    private void initializeModel(Object model, Instance instance, ModelDef<?,?> def) {
        def.initModelHelper(model, instance, this);
    }

    public void bind(Object model) {
        NncUtils.requireTrue(EntityUtils.tryGetId(model) == null, "Can not bind a persisted entity");
        if(containsModel(model)) {
            return;
        }
        createInstanceFromModel(model);
    }

//    @SuppressWarnings("unused")
//    public <T extends Entity> Table<T> getArray(Class<T> elementType, long id) {
//        return new Table<>(
//                NncUtils.map(
//                        ((InstanceArray) instanceContext.get(id)).getElements(),
//                        element -> {
//                            if(element instanceof Instance instance) {
//                                return getModel(elementType, instance);
//                            }
//                            else {
//                                return elementType.cast(element);
//                            }
//                        }
//                )
//        );
//    }

    @SuppressWarnings("unused")
    public UserRT getUser(long id) {
        return getEntity(UserRT.class, id);
    }

    public RoleRT getRole(long id) {
        return getEntity(RoleRT.class, id);
    }
    public FlowRT getFlow(long id) {
        return getEntity(FlowRT.class, id);
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
    public Constraint<UniqueConstraintParam> getConstraint(long id) {
        return getEntity(new TypeReference<>(){}, id);
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
    public boolean containsEntity(Class<?> entityType,  long id) {
        return entityMap.containsKey(new EntityKey(entityType, id));
    }

    public <T> T getEntity(Class<T> entityType, long id) {
        if (parent != null && parent.containsEntity(entityType, id)) {
            return parent.getEntity(entityType, id);
        }
        Object entity = entityMap.get(new EntityKey(entityType, id));
        if (entity != null) {
            return entityType.cast(entity);
        }
        if (instanceContext == null) {
            throw new InternalException("Can not find model for id " + id);
        }
        return getModel(entityType, instanceContext.get(id));
    }

    public <T extends Enum<?>> T getEnum(Class<T> klass, long id) {
        return getDefContext().getEnumConstant(klass, id);
    }

    public <T> T get(Class<T> klass, long id) {
        return klass.cast(getEntity(klass, id));
//        if(Enum.class.isAssignableFrom(klass)) {
//            return klass.cast(getEnum(klass.asSubclass(Enum.class), id));
//        }
//        else {
//            return klass.cast(getEntity(klass, id));
//        }
//        else {
//            throw new InternalException("Invalid model type: " + klass.getName());
//        }
    }


    public void initIds() {
        NncUtils.requireNonNull(instanceContext);
        flushAndWriteInstances();
        instanceContext.initIds();
    }

    public void finish() {
        NncUtils.requireNonNull(instanceContext);
        flush();
        updateInstances();
        writeInstances(instanceContext);
        instanceContext.finish();
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

    protected void flush() {}

    protected void writeInstances(IInstanceContext instanceContext) {}

    private void updateInstances() {
        new IdentityHashMap<>(model2instance).forEach(this::updateInstance);
    }

    private void updateInstance(Object model, Instance instance) {
        if(isModelInitialized(model)) {
            ModelDef<?, ?> def = getDefContext().getDef(instance.getType());
            def.updateInstanceHelper(model, instance, this);
        }
    }

    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Object...values) {
        NncUtils.requireNonNull(instanceContext, "instanceContext required");
        IndexKeyPO indexKey = createIndexKey(indexDef, values);
        List<Instance> instances = instanceContext.selectByKey(indexKey);
        return createEntityList(indexDef.getType(), instances);
    }

    @Override
    public <T> List<T> query(EntityIndexQuery<T> query) {
        NncUtils.requireNonNull(instanceContext, "instanceContext required");
        Class<T> javaClass = query.indexDef().getType();
        Index indexConstraint = getDefContext().getIndexConstraint(query.indexDef());
        List<Instance> instances = instanceContext.query(new InstanceIndexQuery(
                indexConstraint,
                NncUtils.map(
                        query.items(),
                        item -> createInstanceQueryItem(indexConstraint, javaClass, item)
                ),
                query.lastOperator(),
                query.desc(),
                query.limit()
        ));
        return createEntityList(javaClass, instances);
    }

    private InstanceIndexQueryItem createInstanceQueryItem(Index indexConstraint,
                                                           Class<?> javaClass,
                                                           EntityIndexQueryItem queryItem) {
        ClassType type = indexConstraint.getDeclaringType();
        Field field = type.getFieldByJavaField(ReflectUtils.getField(javaClass, queryItem.fieldName()));
        return new InstanceIndexQueryItem(
                indexConstraint.getFieldByTypeField(field),
                resolveInstance(field.getType(), queryItem.value())
        );
    }

    private Instance resolveInstance(Type type, Object value) {
        if(value == null) {
            return InstanceUtils.nullInstance();
        }
        if(containsModel(value)) {
            return getInstance(value);
        }
        return InstanceUtils.resolveValue(type, value);
    }

    private <T> List<T> createEntityList(Class<T> javaType, List<Instance> instances) {
        return EntityProxyFactory.getProxy(
                new TypeReference<Table<T>>() {},
                null,
                table -> table.initialize(
                        NncUtils.map(
                                instances,
                                inst -> getModel(javaType, inst)
                        )
                ),
                k -> Table.createProxy(k, javaType)
        );
    }

    public long getTenantId() {
        NncUtils.requireNonNull(instanceContext);
        return instanceContext.getTenantId();
    }

    public boolean remove(Object entity) {
        if(parent != null && parent.containsModel(entity)) {
            return parent.remove(entity);
        }
        Set<Instance> instances = beforeRemove(entity);
        updateInstances();
        if(NncUtils.isEmpty(instances)) {
            return false;
        }
        if(instanceContext != null) {
            instanceContext.batchRemove(instances);
        }
        else {
            instances.forEach(this::onInstanceRemoved);
        }
        return true;
    }

    private Set<Instance> beforeRemove(Object model) {
        Set<Instance> instancesToRemove = new IdentitySet<>();
        beforeRemove0(model, instancesToRemove);
        return instancesToRemove;
    }

    private void beforeRemove0(Object model, Set<Instance> instancesToRemove) {
        Instance instance = model2instance.get(model);
        if(instance == null) {
            return;
        }
        if(instancesToRemove.contains(instance)) {
            return;
        }
        instancesToRemove.add(instance);
        if(model instanceof RemovalAware removalAware) {
            List<Object> cascades = removalAware.onRemove();
            if (NncUtils.isNotEmpty(cascades)) {
                for (Object cascade : cascades) {
                    beforeRemove0(cascade, instancesToRemove);
                }
            }
        }
    }

    public @Nullable IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Object...fieldValues) {
        NncUtils.requireNonNull(instanceContext);
        IndexKeyPO indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        Instance instance = instanceContext.selectByUniqueKey(indexKey);
        return getEntity(entityType, instance.getId());
    }

    private IndexKeyPO createIndexKey(IndexDef<?> uniqueConstraintDef, Object...values) {
        Index constraint = getDefContext().getIndexConstraint(uniqueConstraintDef);
        NncUtils.requireNonNull(constraint);
        return constraint.createIndexKeyByModels(Arrays.asList(values), this);
    }

    @Override
    public UnionType getUnionType(Set<Type> typeMembers) {
        boolean allPersisted = NncUtils.allMatch(typeMembers, t -> t.getId() != null);
        if(allPersisted) {
            List<Type> types = new ArrayList<>(typeMembers);
            types.sort(Comparator.comparingLong(Entity::getId));
            UnionType pType = getByUniqueKey(
                    UnionType.class,
                    UnionType.UNIQUE_TYPE_ELEMENTS,
                    NncUtils.join(types, t -> t.getId().toString())
            );
            if(pType != null) {
                return pType;
            }
        }
        return TypeUtil.createUnion(typeMembers);
    }

    @Override
    public Instance getInstance(Object model) {
        if(parent != null && parent.containsModel(model)) {
            return parent.getInstance(model);
        }
        Instance instance = model2instance.get(model);
        if(instance == null) {
            instance = createInstanceFromModel(model);
        }
        return instance;
    }

    public ClassInstance getEntityInstance(Entity entity) {
        return (ClassInstance) getInstance(entity);
    }

    private Instance createInstanceFromModel(Object model) {
        ModelDef<?,?> def = getDefContext().getDefByModel(model);
        if(def.isProxySupported()) {
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType(), tryGetId(model));
            addMapping(model, instance);
            def.initInstanceHelper(instance, model, this);
            return instance;
        }
        else {
            Instance instance = def.createInstanceHelper(model, this);
            addMapping(model, instance);
            return instance;
        }
    }

    protected Collection<Instance> instances() {
        return instance2model.keySet();
    }

    protected void addMapping(Object model, Instance instance) {
        instance2model.put(instance, model);
        model2instance.put(model, instance);
//        if((model instanceof Entity entity) && entity.getId() != null) {
        if(instance.getId() != null) {
            entityMap.put(EntityKey.create(model.getClass(), instance.getId()), model);
        }
        if(!instance.isValue() && !manualInstanceWriting()
                && instanceContext != null && !instanceContext.containsInstance(instance)) {
            instanceContext.bind(instance);
        }
    }

    protected abstract boolean manualInstanceWriting();

    protected abstract DefContext getDefContext();

}
