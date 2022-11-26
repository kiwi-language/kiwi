package tech.metavm.entity;

import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.infra.IdService;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.ValueUtil;

import java.util.*;
import java.util.function.Supplier;

import static tech.metavm.object.meta.IdConstants.TENANT;

public class EntityContext implements CompositeTypeFactory, ModelMap, InstanceMap {

    private final Map<EntityKey, Entity> entityMap = new HashMap<>();
    private final Set<Entity> entities = new LinkedHashSet<>();
    private final Set<Object> values = new HashSet<>();
    private final IdService idService;
    private final InstanceContext instanceContext;
    private final IdentityHashMap<Object, IInstance> model2instance = new IdentityHashMap<>();
    private final IdentityHashMap<Instance, Object> instance2model = new IdentityHashMap<>();

    public EntityContext(InstanceContext instanceContext) {
        this.instanceContext = instanceContext;
        idService = instanceContext.getIdService();
    }

    @Override
    public <T> T get(Class<T> klass, IInstance instance) {
        return EntityProxyFactory.getProxyInstance(klass, instance, this::getReal);
    }

    Object getReal(Instance instance) {
        return instance2model.computeIfAbsent(instance, this::createModel);
    }

    private Object createModel(Instance instance) {
        return EntityTypeRegistry.createEntity(instance, this);
    }

    public void bind(Object model) {
        if(model instanceof Entity entity) {
            NncUtils.requireTrue(entity.getId() == null, "Can not bind a persisted entity");
            entities.add(entity);
        }
        else {
            values.add(model);
        }
    }

    public Type getTenantType() {
        return getType(TENANT.ID);
    }

    @SuppressWarnings("unused")
    public <T extends Entity> Table<T> getArray(Class<T> elementType, long id) {
        return new Table<>(
                () -> NncUtils.map(
                        ((InstanceArray) instanceContext.get(id)).getElements(),
                        instance -> createEntity(elementType, instance)
                )
        );
    }

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

    public Type getType(long id) {
        return getEntity(Type.class, id);
    }

    public Field getField(long id) {
        return getEntity(Field.class, id);
    }

    public NodeRT<?> getNode(long id) {
        return getEntity(NodeRT.class, id);
    }

    @SuppressWarnings("unused")
    public ConstraintRT<UniqueConstraintParam> getConstraint(long id) {
        return getEntity(ConstraintRT.class, id);
    }

    @SuppressWarnings("unused")
    public UniqueConstraintRT getUniqueConstraint(long id) {
        return getEntity(UniqueConstraintRT.class, id);
    }

    @SuppressWarnings("unused")
    public CheckConstraintRT getCheckConstraint(long id) {
        return getEntity(CheckConstraintRT.class, id);
    }

    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
        EntityKey entityKey = new EntityKey(entityType, id);
        if(entityMap.containsKey(entityKey)) {
            return entityType.cast(entityMap.get(entityKey));
        }
        instanceContext.load(id, LoadingOption.none());
        return getRef(entityType, id);
    }

    public <T extends Enum<?>> T getEnum(Class<T> klass, long id) {
        return EntityTypeRegistry.getEnumConstant(klass, id);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> klass, long id) {
        if(Entity.class.isAssignableFrom(klass)) {
            return (T) getEntity(klass.asSubclass(Entity.class), id);
        }
        else if(Enum.class.isAssignableFrom(klass)) {
            return (T) getEnum(klass.asSubclass(Enum.class), id);
        }
        else {
            throw new InternalException("Invalid model type: " + klass.getName());
        }
    }

    public <T extends Entity> T getRef(Class<T> entityType, long id) {
        return EntityProxyFactory.getProxyInstance(entityType, instanceContext.get(id), this::getReal);
    }

    public void finish() {
        entities.forEach(this::writeEntity);
        instanceContext.finish();
    }

    private void writeEntity(Entity entity) {
        if(entity.getId() == null) {
            instanceContext.bind(
                    EntityTypeRegistry.createInstance(entity, this)
            );
        }
        else {
            EntityTypeRegistry.updateInstance(
                entity, instanceContext.get(entity.getId()), this
            );
        }
    }

    @SuppressWarnings("unused")
    public Table<EnumConstantRT> getEnumConstants(Type declaringType) {
        return createLoadingList(EnumConstantRT.class, instanceContext.getByType(declaringType));
    }

    public Table<Field> getFields(Type type) {
        return new Table<>(selectByKey(FieldPO.INDEX_DECLARING_TYPE_ID, type));
    }

    @SuppressWarnings("unused")
    public Table<ConstraintRT<?>> getConstraints(Type type) {
        return selectByKey(ConstraintPO.INDEX_DECLARING_TYPE_ID, type);
    }

    public <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return selectByKey(indexDef, values).getFirst();
    }

    public <T extends Entity> Table<T> selectByKey(IndexDef<T> indexDef,
                                                     Object...values) {
        IndexKeyPO indexKey = createIndexKey(indexDef, values);
        List<IInstance> instances = instanceContext.selectByKey(indexKey);
        return new Table<>(
                () -> NncUtils.map(
                        instances,
                        inst -> createEntity(indexDef.getEntityType(), inst)
                )
        );
    }

    private <T extends Entity> Table<T> createLoadingList(Class<T> entityType, List<IInstance> instances) {
        return new Table<>(
                () -> NncUtils.map(
                        instances,
                        inst -> entityType.cast(EntityTypeRegistry.createEntity(getRealInstance(inst), this))
                )
        );
    }

    public static Instance getRealInstance(IInstance instance) {
        if(instance instanceof Instance realInstance) {
            return realInstance;
        }
        if(instance instanceof InstanceRef instanceRef) {
            return instanceRef.getRealInstance();
        }
        if(instance instanceof InstanceArrayRef instanceArrayRef) {
            return instanceArrayRef.getRealInstanceArray();
        }
        throw new InternalException("can not get real instance from: " + instance);
    }

    public <T extends Entity> Table<T> selectByKey(Class<T> entityClass, IndexKeyPO indexKeyPO) {
        List<IInstance> instances = instanceContext.selectByKey(indexKeyPO);
        return new Table<>(getEntityLoader(entityClass, instances));
    }

    private <T extends Entity> Supplier<Collection<T>> getEntityLoader(
            Class<T> entityClass,
            Collection<IInstance> instances) {
        return () -> NncUtils.map(
                        instances,
                        inst -> entityClass.cast(EntityTypeRegistry.createEntity(getRealInstance(inst), this))
                );
    }

    private <T extends Entity> T createEntity(Class<T> entityType, IInstance instance) {
        return entityType.cast(EntityTypeRegistry.createEntity(getRealInstance(instance), this));
    }

    @SuppressWarnings("unused")
    private Object createEntity(IInstance instance) {
        return EntityTypeRegistry.createEntity(getRealInstance(instance), this);
    }

    public long getTenantId() {
        return instanceContext.getTenantId();
    }

    public void remove(Entity entity) {
        if(entity.getId() != null) {
            entityMap.remove(entity.key());
            instanceContext.remove(instanceContext.get(entity.getId()));
        }
        entities.remove(entity);
    }

    public InstanceContext getInstanceContext() {
        return instanceContext;
    }

    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Object...fieldValues) {
        IndexKeyPO indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        IInstance instance = instanceContext.selectByUniqueKey(indexKey);
        return getEntity(entityType, instance.getId());
    }

    private IndexKeyPO createIndexKey(IndexDef<?> uniqueConstraintDef, Object...values) {
        ConstraintPO constraint = EntityTypeRegistry.getUniqueConstraint(uniqueConstraintDef);
        return IndexKeyPO.create(constraint.getId(), Arrays.asList(values));
    }

    public Type getParameterizedType(Type rawType, List<Type> typeArguments) {
        Type pType = getByUniqueKey(
                Type.class,
                TypePO.UNIQUE_RAW_TYPE_AND_TYPE_ARGS,
                rawType.getId(),
                NncUtils.join(typeArguments, t -> t.getId().toString())
        );
        if(pType == null) {
            pType = new Type(
                    rawType.getName() + "<" + NncUtils.join(typeArguments, Type::getName) + ">",
                    StandardTypes.OBJECT,
                    rawType.equals(StandardTypes.ARRAY) ? TypeCategory.ARRAY : TypeCategory.CLASS,
                    false,
                    false,
                    rawType,
                    typeArguments,
                    null,
                    null
            );
        }
        return pType;
    }

    @Override
    public Type getUnionType(Set<Type> typeMembers) {
        boolean allPersisted = NncUtils.allMatch(typeMembers, t -> t.getId() != null);
        if(allPersisted) {
            List<Type> types = new ArrayList<>(typeMembers);
            types.sort(Comparator.comparingLong(Entity::getId));
            Type pType = getByUniqueKey(
                    Type.class,
                    TypePO.UNIQUE_TYPE_ELEMENTS,
                    NncUtils.join(types, t -> t.getId().toString())
            );
            if(pType != null) {
                return pType;
            }
        }
        return new Type(
                NncUtils.join(typeMembers, Type::getName, "|"),
                ValueUtil.getCommonSuperType(typeMembers),
                TypeCategory.UNION
        );
    }

    public void initIds() {
        instanceContext.initIds();
        List<Entity> entitiesToInitId = NncUtils.filter(entities, entity -> entity.getId() == null);
        Map<Type, Integer> countMap = NncUtils.mapAndCount(
                entitiesToInitId,
                EntityTypeRegistry::getType
        );
        Map<Type, List<Entity>> type2entities = NncUtils.toMultiMap(
                entitiesToInitId,
                EntityTypeRegistry::getType
        );

        Map<Type, List<Long>> idMap = idService.allocate(getTenantId(), countMap);
        idMap.forEach((type, ids) -> NncUtils.biForEach(
                type2entities.get(type),
                ids,
                Entity::initId
        ));
    }

    @SuppressWarnings("unused")
    public Entity getRealEntity(EntityKey key) {
        if(!entityMap.containsKey(key)) {
            IInstance instance = instanceContext.get(key.id());
            Entity entity = EntityTypeRegistry.createEntity(getRealInstance(instance), this);
            entityMap.put(key, entity);
        }
        return entityMap.get(key);
    }

    @Override
    public IInstance getByModel(Object model) {
        return model2instance.computeIfAbsent(model, this::createInstanceFromModel);
    }

    private IInstance createInstanceFromModel(Object model) {
        Instance instance = EntityTypeRegistry.createInstance(model, this);
        instanceContext.bind(instance);
        return instance;
    }

}
