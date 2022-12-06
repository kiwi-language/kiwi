package tech.metavm.entity;

import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.*;

import java.util.*;

import static tech.metavm.entity.EntityUtils.tryGetId;

public class EntityContext implements CompositeTypeFactory, IEntityContext {

    private final Map<EntityKey, Entity> entityMap = new HashMap<>();
    private final Set<Entity> entities = new LinkedHashSet<>();
    private final Set<Object> values = new HashSet<>();
    private final IInstanceContext instanceContext;
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    private final IdentityHashMap<Instance, Object> instance2model = new IdentityHashMap<>();
    private final IEntityContext parent;
    private final DefContext defContext;

    public EntityContext(IInstanceContext instanceContext, IEntityContext parent) {
        this(instanceContext, parent, ModelDefRegistry.getDefContext());
    }

    public EntityContext(IInstanceContext instanceContext, IEntityContext parent, DefContext defContext) {
        this.instanceContext = instanceContext;
        this.parent = parent;
        this.defContext = defContext;
        instanceContext.addListener(instance -> {
            Object model = instance2model.get(instance);
            if((model instanceof Entity entity) && entity.getId() == null) {
                entity.initId(instance.getId());
            }
        });
    }

    public void preloadEntity(Entity entity) {
        if(containsModel(entity)) {
            return;
        }
        if(entity.getId() == null) {
            bind(entity);
        }
        else {
            Instance instance = instanceContext.get(entity.getId());
            model2instance.put(entity, instance);
            instance2model.put(instance, entity);
            entityMap.put(entity.key(), entity);
        }
        entities.add(entity);
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        if(parent != null && parent.containsInstance(instance)) {
            return parent.getModel(klass, instance);
        }
        Class<? extends T> javaType =
                defContext.getJavaType(instance.getType()).asSubclass(klass);
        return createModel(javaType, instance);
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instance2model.containsKey(instance);
    }

    @Override
    public boolean containsModel(Object model) {
        return model2instance.containsKey(model);
    }

    public boolean containsKey(EntityKey entityKey) {
        return entityMap.containsKey(entityKey);
    }

    private <T> T createModel(Class<T> actualType, Instance instance) {
        ModelDef<T, ?> def = defContext.getDef(actualType);
        T model;
        if(def.isProxySupported()) {
            model = EntityProxyFactory.getProxy(
                    actualType,
                    instance.getId(),
                    m -> def.initModelHelper(m, instance, this),
                    k -> def.createModelProxy(k)
            );
        }
        else {
            model = actualType.cast(def.createModelHelper(instance, this));
        }
        addMapping(model, instance);
        return model;
    }

    public void bind(Object model) {
        if(model instanceof Entity entity) {
            NncUtils.requireTrue(entity.getId() == null, "Can not bind a persisted entity");
            entities.add(entity);
        }
        else {
            values.add(model);
        }
        createInstanceFromModel(model);
    }

    @SuppressWarnings("unused")
    public <T extends Entity> Table<T> getArray(Class<T> elementType, long id) {
        return new Table<>(
                NncUtils.map(
                        ((InstanceArray) instanceContext.get(id)).getElements(),
                        element -> {
                            if(element instanceof Instance instance) {
                                return getModel(elementType, instance);
                            }
                            else {
                                return elementType.cast(element);
                            }
                        }
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

    public <T extends Entity> T getEntity(TypeReference<T> typeReference, long id) {
        return getEntity(typeReference.getType(), id);
    }

    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
        Instance instance = instanceContext.get(id);
        if(parent != null && parent.containsInstance(instance)) {
            return parent.getModel(entityType, instance);
        }
        return getModel(entityType, instance);
    }

    public <T extends Enum<?>> T getEnum(Class<T> klass, long id) {
        return defContext.getEnumConstant(klass, id);
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

    public void finish() {
        entities.forEach(this::writeEntity);
        instanceContext.finish();
    }

    private void writeEntity(Entity entity) {
        Instance instance = model2instance.get(entity);
        if(instance == null) {
            createInstanceFromModel(entity);
        }
        else {
            ModelDef<?,?> def = defContext.getDef(instance.getType());
            def.updateInstanceHelper(entity, instance, this);
        }
    }

    public <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.getFirst(selectByKey(indexDef, values));
    }

    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                     Object...values) {
        IndexKeyPO indexKey = createIndexKey(indexDef, values);
        List<Instance> instances = instanceContext.selectByKey(indexKey);
        return EntityProxyFactory.getProxy(
                new TypeReference<Table<T>>() {},
                null,
                table -> table.initialize(
                        NncUtils.map(
                                instances,
                                inst -> getModel(indexDef.getEntityType(), inst)
                        )
                ),
                this::createTable
        );
    }

    private <T> Table<T> createTable(Class<? extends Table<T>> klass) {
        return ReflectUtils.invokeConstructor(ReflectUtils.getConstructor(klass));
    }

    void onIdInitialized(Instance instance) {
        Object model = instance2model.get(instance);
        if((model instanceof Entity entity) && entity.getId() == null) {
            entity.initId(instance.getId());
        }
    }

    public long getTenantId() {
        return instanceContext.getTenantId();
    }

    public void remove(Object model) {
        if(model instanceof Entity entity) {
            if (entity.getId() != null) {
                entityMap.remove(entity.key());
                instanceContext.remove(instanceContext.get(entity.getId()));
            }
            entities.remove(entity);
        }
        else {
            values.remove(model);
        }
    }

    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    public <T extends Entity> T getByUniqueKey(Class<T> entityType, IndexDef<?> uniqueConstraintDef, Object...fieldValues) {
        IndexKeyPO indexKey = createIndexKey(uniqueConstraintDef, fieldValues);
        Instance instance = instanceContext.selectByUniqueKey(indexKey);
        return getEntity(entityType, instance.getId());
    }

    private IndexKeyPO createIndexKey(IndexDef<?> uniqueConstraintDef, Object...values) {
        UniqueConstraintRT constraint = defContext.getUniqueConstraint(uniqueConstraintDef);
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

    private Instance createInstanceFromModel(Object model) {
        ModelDef<?,?> def = defContext.getDef(model.getClass());
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

    private void addMapping(Object model, Instance instance) {
        instance2model.put(instance, model);
        model2instance.put(model, instance);
        if(!instance.getType().isValue() && !instanceContext.containsInstance(instance)) {
            instanceContext.bind(instance);
        }
    }

}
