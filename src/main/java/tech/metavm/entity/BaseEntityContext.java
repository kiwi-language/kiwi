package tech.metavm.entity;

import tech.metavm.dto.InternalErrorCode;
import tech.metavm.flow.FlowRT;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.entity.EntityUtils.tryGetId;

public abstract class BaseEntityContext implements CompositeTypeFactory, IEntityContext {

    private final Map<EntityKey, Entity> entityMap = new HashMap<>();
    private final @Nullable IInstanceContext instanceContext;
    private final IdentityHashMap<Object, Instance> model2instance = new IdentityHashMap<>();
    private final IdentityHashMap<Instance, Object> instance2model = new IdentityHashMap<>();
    private final IEntityContext parent;

    public BaseEntityContext(@Nullable IInstanceContext instanceContext, IEntityContext parent) {
        this.instanceContext = instanceContext;
        this.parent = parent;
        if(instanceContext != null) {
            instanceContext.addListener(instance -> {
                Object model = instance2model.get(instance);
                if (model instanceof IdInitializing idInitializing) {
                    NncUtils.requireNull(idInitializing.getId());
                    idInitializing.initId(instance.getId());
                    if(idInitializing instanceof Entity entity) {
                        entityMap.put(entity.key(), entity);
                    }
                }
            });
        }
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
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
            model = createModel(actualClass, genericClass, instance);
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

    protected <T> void beforeGetModel(Class<T> klass, Instance instance) {}

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

    protected <T> T createModel(Class<T> actualClass, java.lang.reflect.Type genericType, Instance instance) {
        ModelDef<?, ?> def = getDefContext().getDef(genericType);
        T model;
        if(def.isProxySupported()) {
            model = EntityProxyFactory.getProxy(
                    actualClass,
                    instance.getId(),
                    m -> def.initModelHelper(m, instance, this),
                    k -> actualClass.cast(def.createModelProxyHelper(k))
            );
        }
        else {
            model = actualClass.cast(def.createModelHelper(instance, this));
        }
        addMapping(model, instance);
        return model;
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

    @Override
    public boolean containsEntity(Class<? extends Entity> entityType,  long id) {
        return entityMap.containsKey(new EntityKey(entityType, id));
    }

    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
        if (parent != null && parent.containsEntity(entityType, id)) {
            return parent.getEntity(entityType, id);
        }
        Entity entity = entityMap.get(new EntityKey(entityType, id));
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
        if(Entity.class.isAssignableFrom(klass)) {
            return klass.cast(getEntity(klass.asSubclass(Entity.class), id));
        }
        else if(Enum.class.isAssignableFrom(klass)) {
            return klass.cast(getEnum(klass.asSubclass(Enum.class), id));
        }
        else {
            throw new InternalException("Invalid model type: " + klass.getName());
        }
    }

    public void finish() {
        NncUtils.requireNonNull(instanceContext);
        flush(instanceContext);
        models().forEach(this::writeModel);
        instanceContext.finish();
    }

    @Override
    public boolean isFinished() {
        return instanceContext != null && instanceContext.isFinished();
    }

    protected Collection<Object> models() {
        return model2instance.keySet();
    }

    public void flush() {
        NncUtils.requireNonNull(instanceContext, "Instance context required");
        flush(instanceContext);
    }

    protected void flush(IInstanceContext instanceContext) {}

    private void writeModel(Object model) {
        Instance instance = model2instance.get(model);
        if(instance == null) {
            createInstanceFromModel(model);
        }
        else {
            ModelDef<?,?> def = getDefContext().getDef(instance.getType());
            def.updateInstanceHelper(model, instance, this);
        }
    }

    public <T extends Entity> T selectByUniqueKey(IndexDef<T> indexDef, Object...values) {
        return NncUtils.getFirst(selectByKey(indexDef, values));
    }

    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                                  Object...values) {
        NncUtils.requireNonNull(instanceContext, "instanceContext required");
        IndexKeyPO indexKey = createIndexKey(indexDef, values);
        List<Instance> instances = instanceContext.selectByKey(indexKey);
        return EntityProxyFactory.getProxy(
                new TypeReference<Table<T>>() {},
                null,
                table -> table.initialize(
                        NncUtils.map(
                                instances,
                                inst -> getModel(indexDef.getType(), inst)
                        )
                ),
                k -> Table.createProxy(k, indexDef.getType())
        );
    }

    public long getTenantId() {
        NncUtils.requireNonNull(instanceContext);
        return instanceContext.getTenantId();
    }

    public boolean remove(Entity entity) {
        Instance instance = model2instance.remove(entity);
        if(instance == null) {
            return false;
        }
        instance2model.remove(instance);
        if(instanceContext != null) {
            instanceContext.remove(instance);
        }
        if (entity.getId() != null) {
            entityMap.remove(entity.key());
        }
        return true;
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
        UniqueConstraintRT constraint = getDefContext().getUniqueConstraint(uniqueConstraintDef);
        NncUtils.requireNonNull(constraint);
        return IndexKeyPO.create(constraint.getId(), Arrays.asList(values));
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

    public void initIds() {
        NncUtils.requireNonNull(instanceContext);
        flush(instanceContext);
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
        if((model instanceof Entity entity) && entity.getId() != null) {
            entityMap.put(entity.key(), entity);
        }
        if(!instance.isValue() && !lateBinding()
                && instanceContext != null && !instanceContext.containsInstance(instance)) {
            instanceContext.bind(instance);
        }
    }

    protected abstract boolean lateBinding();

    protected abstract DefContext getDefContext();

}
