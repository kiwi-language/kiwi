package tech.metavm.entity;

import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.*;

import java.util.*;

import static tech.metavm.object.meta.IdConstants.TENANT;

public class EntityContext implements CompositeTypeFactory, ModelMap, IEntityContext {

    private final Map<EntityKey, Entity> entityMap = new HashMap<>();
    private final Set<Entity> entities = new LinkedHashSet<>();
    private final Set<Object> values = new HashSet<>();
    private final EntityIdProvider idService;
    private final InstanceContext instanceContext;
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    private final IdentityHashMap<Instance, Object> instance2model = new IdentityHashMap<>();
    private final IEntityContext parent;
    private final DefContext defContext;

    public EntityContext(InstanceContext instanceContext, IEntityContext parent, DefContext defContext) {
        this.instanceContext = instanceContext;
        idService = instanceContext.getIdService();
        this.parent = parent;
        this.defContext = defContext;
    }

    @Override
    public <T> T get(Class<T> klass, Instance instance) {
        if(parent != null && parent.containsInstance(instance)) {
            return parent.get(klass, instance);
        }
        Class<? extends T> javaType =
                EntityTypeRegistry.getJavaType(instance.getType()).asSubclass(klass);
        return getWithActualType(javaType, instance);
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instance2model.containsKey(instance);
    }

    @Override
    public boolean containsKey(EntityKey entityKey) {
        return entityMap.containsKey(entityKey);
    }

    private <T> T getWithActualType(Class<T> actualType, Instance instance) {
        return EntityProxyFactory.getProxyInstance(
                actualType,
                instance.getId(),
                () -> actualType.cast(defContext.createEntity(instance, this))
        );
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
                NncUtils.map(
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
        return getEntity(new TypeReference<>(){}, id);
    }

    @SuppressWarnings("unused")
    public UniqueConstraintRT getUniqueConstraint(long id) {
        return getEntity(UniqueConstraintRT.class, id);
    }

    @SuppressWarnings("unused")
    public CheckConstraintRT getCheckConstraint(long id) {
        return getEntity(CheckConstraintRT.class, id);
    }

    @Override
    public <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    @Override
    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
        EntityKey entityKey = new EntityKey(entityType, id);
        if(parent != null && parent.containsKey(entityKey)) {
            return parent.getEntity(entityType, id);
        }
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
        return EntityProxyFactory.getProxyInstance(entityType, id, () -> getRealEntity(entityType, id));
    }

    public void finish() {
        if(parent != null) {
            parent.finish();
        }
        entities.forEach(this::writeEntity);
        instanceContext.finish();
        instance2model.forEach((instance, model) -> {
            if(model instanceof Entity entity) {
                if(entity.getId() == null) {
                    entity.initId(instance.getId());
                }
            }
        });
    }

    private void writeEntity(Entity entity) {
        if(entity.getId() == null) {
            Instance instance = EntityTypeRegistry.createInstance(entity, this);
            instance2model.put(instance, entity);
            model2instance.put(entity, instance);
            instanceContext.bind(instance);
        }
        else {
            EntityTypeRegistry.updateInstance(
                entity, instanceContext.get(entity.getId()), this
            );
        }
    }

    public <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.getFirst(selectByKey(indexDef, values));
    }

    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                     Object...values) {
        IndexKeyPO indexKey = createIndexKey(indexDef, values);
        List<Instance> instances = instanceContext.selectByKey(indexKey);
        return EntityProxyFactory.getProxyInstance(
                new TypeReference<>() {},
                () -> new Table<>(
                        NncUtils.map(
                                instances,
                                inst -> createEntity(indexDef.getEntityType(), inst)
                        )
                )
        );
    }

    public <T extends Entity> Table<T> selectByKey(Class<T> entityClass, IndexKeyPO indexKeyPO) {
        List<Instance> instances = instanceContext.selectByKey(indexKeyPO);
        return EntityProxyFactory.getProxyInstance(
                new TypeReference<>() {},
                () -> new Table<>(
                        NncUtils.map(
                                instances,
                                inst -> entityClass.cast(EntityTypeRegistry.createEntity(inst, this))
                        )
                )
        );
    }


    private <T extends Entity> T createEntity(Class<T> entityType, Instance instance) {
        return entityType.cast(EntityTypeRegistry.createEntity(instance, this));
    }

    @SuppressWarnings("unused")
    private Object createEntity(Instance instance) {
        return EntityTypeRegistry.createEntity(instance, this);
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
        Instance instance = instanceContext.selectByUniqueKey(indexKey);
        return getEntity(entityType, instance.getId());
    }

    private IndexKeyPO createIndexKey(IndexDef<?> uniqueConstraintDef, Object...values) {
        ConstraintPO constraint = EntityTypeRegistry.getUniqueConstraint(uniqueConstraintDef);
        NncUtils.requireNonNull(constraint);
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
                    null,
                    rawType,
                    typeArguments,
                    null,
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

    public <T extends Entity> T getRealEntity(Class<T> entityType, long id) {
        EntityKey key = new EntityKey(entityType, id);
        if(!entityMap.containsKey(key)) {
            Instance instance = instanceContext.get(key.id());
            Entity entity = EntityTypeRegistry.createEntity(instance, this);
            entityMap.put(key, entity);
        }
        return entityType.cast(entityMap.get(key));
    }

    @Override
    public Instance getInstanceByModel(Object model) {
        Long id;
        if(model instanceof Identifiable identifiable) {
            id = identifiable.getId();
        }
        else {
            id = null;
        }
        return EntityProxyFactory.getProxyInstance(
                model instanceof Collection<?> ? InstanceArray.class : Instance.class,
                id,
                () -> getRealInstanceByModel(model)
        );
    }

    private Instance getRealInstanceByModel(Object model) {
        return model2instance.computeIfAbsent(model, this::createInstanceFromModel);
    }

    private Instance createInstanceFromModel(Object model) {
        Instance instance = EntityTypeRegistry.createInstance(model, this);
        instanceContext.bind(instance);
        return instance;
    }

}
