package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefContext implements DefMap, IEntityContext {
    private final Function<Object, Long> getId;
    private final Map<Type, ModelDef<?,?>> defMap = new HashMap<>();
    private final Map<tech.metavm.object.meta.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
    private final ValueDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final InstanceSink instanceSink;

    private final Map<Object, Instance> model2instance = new IdentityHashMap<>();
    private final Map<Instance, Object> instance2model = new IdentityHashMap<>();
    private final Set<Instance> instances = new IdentitySet<>();
    private final Set<Object> pendingModels = new IdentitySet<>();

    public DefContext(Function<Object, Long> getId) {
        this(getId, null);
    }

    public DefContext(Function<Object, Long> getId, InstanceSink instanceSink) {
        this.getId = getId;
        this.instanceSink = instanceSink;
        StandardDefBuilder stdBuilder = new StandardDefBuilder();
        stdBuilder.initRootTypes(this);
        objectDef = stdBuilder.getObjectDef();
        enumDef = stdBuilder.getEnumDef();
        if(instanceSink != null) {
            instanceSink.addListener(instance -> {
                Object model = instance2model.get(instance);
                if ((model instanceof IdInitializing entity) && entity.getId() == null) {
                    entity.initId(instance.getId());
                }
            });
        }
    }

    @Override
    public ModelDef<?,?> getDef(Type entityType) {
        ModelDef<?,?> existing = defMap.get(entityType);
        if(existing != null) {
            return existing;
        }
        ModelDef<?,?> def = parseType(entityType);
        addDef(def);
        return def;
    }

    public tech.metavm.object.meta.Type getType(Class<?> javaType) {
        return getDef(javaType).getType();
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return getType(javaField.getDeclaringClass()).getFieldByJavaField(javaField);
    }

    @SuppressWarnings("unchecked")
    public <T> ModelDef<T,?> getDef(Class<T> klass) {
        return (ModelDef<T, ?>) getDef((Type) klass);
    }

    public EntityDef<?> getEntityDef(tech.metavm.object.meta.Type type) {
        return (EntityDef<?>) getDef(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> EnumDef<T> getEnumDef(Class<T> enumType) {
        return (EnumDef<T>) getDef(enumType);
    }

    public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
        return NncUtils.requireNonNull(type2Def.get(type), () -> new InternalException("Can not find def for type " + type.getId()));
    }

    private ModelDef<?,?> parseType(Type entityType) {
        TypeCategory typeCategory = ValueUtil.getTypeCategory(entityType);
        if(typeCategory.isArray()) {
            if (entityType instanceof ParameterizedType pType) {
                return new ArrayDef<>(
                        getDef(pType.getActualTypeArguments()[0]),
                        pType,
                        getId.apply(pType)
                );
            }
            else {
                return new ArrayDef<>(
                        objectDef,
                        entityType,
                        getId.apply(entityType)
                );
            }
        }
        else {
            Class<?> rawClass = ReflectUtils.getRawClass(entityType);
            if (typeCategory.isEnum()) {
                Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
                }.getType());
                return EnumParser.parse(
                        enumType,
                        enumDef,
                        getId,
                        this,
                        this
                );
            }
            if (typeCategory.isEntity()) {
                return EntityParser.parse(
                        rawClass.asSubclass(Entity.class),
                        getId,
                        this,
                        this
                );
            }
            if (typeCategory.isValue()) {
                if(Record.class.isAssignableFrom(rawClass)) {
                    return RecordParser.parse(
                            rawClass.asSubclass(Record.class), getId, this, this
                    );
                }
                else {
                    return ValueParser.parse(
                            rawClass,
                            getId,
                            this,
                            this
                    );
                }
            }
        }
        throw new InternalException("Can not parse definition for type: " + entityType);
    }

    @Override
    public void addDef(ModelDef<?, ?> def) {
        defMap.put(def.getGenericType(), def);
        type2Def.put(def.getType(), def);
        pendingModels.addAll(def.getEntityMapping().values());
        def.getInstanceMapping().forEach(this::addMapping);
    }

    public Collection<ModelDef<?, ?>> getAllDefList() {
        return defMap.values();
    }

    public void setModelFields(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {
        getDef(instance.getType()).initModelHelper(model, instance, modelInstanceMap);
    }

    public Class<?> getJavaType(tech.metavm.object.meta.Type type) {
        return getDef(type).getModelType();
    }

    public <T extends Enum<?>> T getEnumConstant(Class<T> klass, long id) {
        return getEnumDef(klass).getEnumConstantDef(id).getValue();
    }

    @Override
    public Instance getInstance(Object model) {
        if(pendingModels.contains(model)) {
            generateInstance(model);
        }
        Instance existing = model2instance.get(model);
        if(existing != null) {
            return existing;
        }
        ModelDef<?,?> def = getDef(model.getClass());
        Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType());
        addMapping(model, instance);
        def.initInstanceHelper(instance, model, this);
        return instance;
    }

    public void generateInstances() {
        while (!pendingModels.isEmpty()) {
            new IdentitySet<>(pendingModels).forEach(this::generateInstance);
        }
    }

    private void generateInstance(Object model) {
        pendingModels.remove(model);
        if(model2instance.containsKey(model)) {
            return;
        }
        ModelDef<?,?> def = getDef(model.getClass());
        Long id = EntityUtils.tryGetId(model);
        if(def.isProxySupported()) {
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType());
             if(id != null) {
                 instance.initId(id);
             }
            addMapping(model, instance);
            def.initInstanceHelper(instance, model, this);
        }
        else {
            Instance instance = def.createInstanceHelper(model, this);
            if(id != null) {
                instance.initId(id);
            }
            addMapping(model, instance);
        }
    }

    private void addMapping(Object model, Instance instance) {
        if(StandardTypes.BLACK_MODELS.contains(model)) {
            StandardTypes.BLACK_INSTANCES.add(instance);
        }
        model2instance.put(model, instance);
        instance2model.put(instance, model);
        instances.add(instance);
    }

    public <T> T getModel(Class<T> klass, Instance instance) {
        generateInstances();
        return klass.cast(instance2model.get(instance));
    }

    @SuppressWarnings("unchecked")
    private <T> T createRefHelper(Instance instance, Class<T> entityType) {
        ModelDef<T, ?> def = (ModelDef<T, ?>) getDef(instance.getType());
        return EntityProxyFactory.getProxy(
                entityType,
                instance.getId(),
                model -> initModel(model, instance),
                k -> def.createModelProxy(k)
        );
    }

    private void initModel(Object model, Instance instance) {
        getDef(instance.getType()).initModelHelper(model, instance, this);
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instances.contains(instance);
    }

    @Override
    public boolean containsModel(Object model) {
        return model2instance.containsKey(model) || pendingModels.contains(model);
    }

    @Override
    public <T extends Entity> T getEntity(Class<T> entityType, long id) {
        return NncUtils.get(
                findModel(model -> Objects.equals(EntityUtils.tryGetId(model), id)),
                entityType::cast
        );
    }

    private Object findModel(Predicate<Object> filter) {
        Object result = NncUtils.find(pendingModels, filter);
        if(result != null) {
            return result;
        }
        return NncUtils.find(model2instance.keySet(), filter);
    }

    @Override
    public void initIds() {
        generateInstances();
        if(instanceSink != null) {
            instanceSink.replace(NncUtils.filterNot(instances, Instance::isValue));
            instanceSink.initIds();
        }
    }

    public void finish() {
        generateInstances();
        if(instanceSink != null) {
            instanceSink.replace(NncUtils.filterNot(instances, Instance::isValue));
            instanceSink.finish();
        }
    }

    public tech.metavm.object.meta.Type getTypeByTable(Table<?> table) {
        for (Object model : model2instance.keySet()) {
            if(model instanceof tech.metavm.object.meta.Type type) {
                if(type.getDeclaredConstraints() == table
                        || type.getDeclaredFields() == table) {
                    return type;
                }
            }
        }
        return null;
    }

    @Override
    public IInstanceContext getInstanceContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object...refValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Object model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(Object model) {
        throw new UnsupportedOperationException();
    }

    public UniqueConstraintRT getUniqueConstraint(IndexDef<?> indexDef) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(indexDef.getEntityType());
        return entityDef.getUniqueConstraintDef(indexDef).getUniqueConstraint();
    }
}
