package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

public class DefContext extends BaseEntityContext implements DefMap, IEntityContext {
    private final Function<Object, Long> getId;
    private final Map<Type, ModelDef<?,?>> javaType2Def = new HashMap<>();
    private final Map<tech.metavm.object.meta.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
    private final AnyTypeDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final Set<Object> pendingModels = new IdentitySet<>();

    public DefContext(Function<Object, Long> getId) {
        this(getId, null);
    }

    public DefContext(Function<Object, Long> getId, IInstanceContext instanceContext) {
        super(instanceContext, null);
        this.getId = getId;
        StandardDefBuilder stdBuilder = new StandardDefBuilder();
        stdBuilder.initRootTypes(this);
        objectDef = stdBuilder.getObjectDef();
        enumDef = stdBuilder.getEnumDef();
    }

    @Override
    public ModelDef<?,?> getDef(Type javaType) {
        checkJavaType(javaType);
        javaType = ReflectUtils.getBoxedType(javaType);
        TypeCategory typeCategory = ValueUtil.getTypeCategory(javaType);
        if(!typeCategory.isArray()) {
            javaType = ReflectUtils.getRawClass(javaType);
        }
        ModelDef<?,?> existing = javaType2Def.get(javaType);
        if(existing != null) {
            return existing;
        }
        ModelDef<?,?> def = parseType(javaType);
        addDef(def);
        return def;
    }

    private void checkJavaType(Type javaType) {
        if(javaType instanceof WildcardType && javaType instanceof TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public tech.metavm.object.meta.Type getType(Class<?> javaType) {
        return getDef(javaType).getType();
    }

    public ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return getClassType(javaField.getDeclaringClass()).getFieldByJavaField(javaField);
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

    private ModelDef<?,?> parseType(Type genericType) {
        genericType = ReflectUtils.eraseType(genericType);
        Class<?> rawClass = ReflectUtils.getRawClass(genericType);
        TypeCategory typeCategory = ValueUtil.getTypeCategory(genericType);
//        if(rawClass == Table.class) {
//            if (genericType instanceof ParameterizedType pType) {
//                ModelDef<?,?> elementDef = getDef(pType.getActualTypeArguments()[0]);
//                return new TableDef<>(
//                        elementDef,
//                        pType,
//                        TypeUtil.getArrayType(elementDef.getType())
//                );
//            }
//            else {
//                throw new InternalException("Raw TableDef should have been defined by StandardDefBuilder");
//            }
//        }
        if(Collection.class.isAssignableFrom(rawClass)) {
            Class<? extends Collection<?>> collectionClass = rawClass.asSubclass(
                    new TypeReference<Collection<?>>(){}.getType()
            );
            if (genericType instanceof ParameterizedType pType) {
                ModelDef<?,?> elementDef = getDef(pType.getActualTypeArguments()[0]);
                return CollectionDef.createHelper(
                        collectionClass,
                        genericType,
                        elementDef,
                        TypeUtil.getArrayType(elementDef.getType())
                );
            }
            else {
                return CollectionDef.createHelper(
                        collectionClass,
                        collectionClass,
                        objectDef,
                        TypeUtil.getArrayType(objectDef.getType())
                );
            }
        }
        else {
            if (typeCategory.isEnum()) {
                Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
                }.getType());
                return EnumParser.parse(
                        enumType,
                        enumDef,
                        this
                );
            }
            if (typeCategory.isEntity()) {
                return EntityParser.parse(
                        rawClass.asSubclass(Entity.class),
                        genericType,
                        this
                );
            }
            if (typeCategory.isValue()) {
                if(Record.class.isAssignableFrom(rawClass)) {
                    return RecordParser.parse(
                            rawClass.asSubclass(Record.class), genericType, this
                    );
                }
                else {
                    return ValueParser.parse(
                            rawClass,
                            genericType,
                            this
                    );
                }
            }
        }
        throw new InternalException("Can not parse definition for type: " + genericType);
    }

    @Override
    public void addDef(ModelDef<?, ?> def) {
        javaType2Def.put(def.getJavaType(), def);
        type2Def.put(def.getType(), def);
        def.getEntityMapping().forEach((javaConstruct, model) -> {
            if((model instanceof IdInitializing idInitializing) && idInitializing.getId() == null) {
                Long id = getId.apply(javaConstruct);
                if(id != null) {
                    idInitializing.initId(id);
                }
            }
            pendingModels.add(model);
        });

        def.getInstanceMapping().forEach((javaConstruct, instance) -> {
            if(!instance.isValue() && instance.getId() == null) {
                Long id = getId.apply(javaConstruct);
                if(id != null) {
                    instance.initId(id);
                }
            }
            addMapping(javaConstruct, instance);
        });
    }

    public Collection<ModelDef<?, ?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Class<?> getJavaClass(tech.metavm.object.meta.Type type) {
        return getDef(type).getJavaClass();
    }

    public Type getJavaType(tech.metavm.object.meta.Type type) {
        return getDef(type).getJavaType();
    }

    public <T extends Enum<?>> T getEnumConstant(Class<T> klass, long id) {
        return getEnumDef(klass).getEnumConstantDef(id).getValue();
    }

    public UniqueConstraintRT getUniqueConstraint(IndexDef<?> indexDef) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(indexDef.getType());
        return entityDef.getUniqueConstraintDef(indexDef).getUniqueConstraint();
    }

    @Override
    public Instance getInstance(Object model) {
        if(pendingModels.contains(model)) {
            generateInstance(model);
        }
        return super.getInstance(model);
    }

    public void generateInstances() {
        while (!pendingModels.isEmpty()) {
            new IdentitySet<>(pendingModels).forEach(this::generateInstance);
        }
    }

    private void generateInstance(Object model) {
        pendingModels.remove(model);
        if(containsModel(model)) {
            return;
        }
        ModelDef<?,?> def = getDefByModel(model);
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

    @Override
    protected boolean lateBinding() {
        return true;
    }

    @Override
    protected DefContext getDefContext() {
        return this;
    }

    @Override
    protected <T> void beforeGetModel(Class<T> klass, Instance instance) {
        generateInstances();
    }

    @Override
    public boolean containsModel(Object model) {
        return super.containsModel(model) || pendingModels.contains(model);
    }

    public boolean containsTypeDef(tech.metavm.object.meta.Type type) {
        return type2Def.containsKey(type);
    }

    @Override
    protected void flush(IInstanceContext instanceContext) {
        generateInstances();
        instanceContext.replace(NncUtils.filterNot(instances(), Instance::isValue));
    }

    @SuppressWarnings("unused")
    // DEBUG用，勿删！
    public tech.metavm.object.meta.Type getTypeByTable(Table<?> table) {
        for (Object model : models()) {
            if(model instanceof ClassType type) {
                if(type.getDeclaredConstraints() == table
                        || type.getDeclaredFields() == table
                        || type.getDeclaredFlows() == table
                ) {
                    return type;
                }
            }
            if(model instanceof UnionType unionType) {
                if(unionType.getDeclaredTypeMembers() == table) {
                    return unionType;
                }
            }
            if(model instanceof EnumType enumType) {
                if(enumType.getDeclaredEnumConstants() == table) {
                    return enumType;
                }
            }
        }
        return null;
    }

    @Override
    public boolean remove(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(Object model) {
        throw new UnsupportedOperationException();
    }

}
