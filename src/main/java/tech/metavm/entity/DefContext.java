package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

public class DefContext extends BaseEntityContext implements DefMap, IEntityContext {
    private final Map<Type, ModelDef<?, ?>> javaType2Def = new HashMap<>();
    private final Map<tech.metavm.object.type.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
    private final IdentitySet<ModelDef<?, ?>> processedDefSet = new IdentitySet<>();
    private final IdentitySet<ClassType> initializedClassTypes = new IdentitySet<>();
    private final ObjectTypeDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final Function<Object, Long> getId;
    private final Set<Object> pendingModels = new IdentitySet<>();
    private final Map<tech.metavm.object.type.Type, tech.metavm.object.type.Type> typeInternMap = new HashMap<>();
    private final Map<Object, Instance> instanceMapping = new IdentityHashMap<>();
    private final IdentityContext identityContext = new IdentityContext(this::isClassTypeInitialized, this::getJavaType);
    private final ColumnStore columnStore;

    public static final Map<Class<?>, Class<?>> BOX_CLASS_MAP = Map.ofEntries(
            Map.entry(Byte.class, Long.class),
            Map.entry(Short.class, Long.class),
            Map.entry(Integer.class, Long.class),
            Map.entry(Float.class, Double.class)
    );

    public DefContext(Function<Object, Long> getId, ColumnStore columnStore) {
        this(getId, null, columnStore);
    }

    public DefContext(Function<Object, Long> getId, IInstanceContext instanceContext, ColumnStore columnStore) {
        super(instanceContext, null);
        this.getId = getId;
        StandardDefBuilder stdBuilder = new StandardDefBuilder(this);
        stdBuilder.initRootTypes();
        objectDef = stdBuilder.getObjectDef();
        enumDef = stdBuilder.getEnumDef();
        this.columnStore = columnStore;
        ColumnKind.columns().forEach(this::writeEntity);
    }

    public void createCompositeTypes(Type javaType, tech.metavm.object.type.Type type) {
        predefineCompositeTypes(javaType, type);
        initCompositeTypes(javaType);
    }

    public void predefineCompositeTypes(Type javaType, tech.metavm.object.type.Type type) {
        preAddDef(new DirectDef<>(BiUnion.createNullableType(javaType), getNullableType(type)));
        preAddDef(new CollectionDef<>(ReadWriteArray.class, ParameterizedTypeImpl.create(ReadWriteArray.class, javaType),
                getArrayType(type, ArrayKind.READ_WRITE), getDef(type), this));
        preAddDef(new CollectionDef<>(ChildArray.class, ParameterizedTypeImpl.create(ChildArray.class, javaType),
                getArrayType(type, ArrayKind.CHILD), getDef(type), this));
        preAddDef(new CollectionDef<>(ReadonlyArray.class, ParameterizedTypeImpl.create(ReadonlyArray.class, javaType),
                getArrayType(type, ArrayKind.READ_ONLY), getDef(type), this));
    }

    public void initCompositeTypes(Type javaType) {
        afterDefInitialized(getDef(BiUnion.createNullableType(javaType)));
        afterDefInitialized(getDef(ParameterizedTypeImpl.create(ReadWriteArray.class, javaType)));
        afterDefInitialized(getDef(ParameterizedTypeImpl.create(ChildArray.class, javaType)));
        afterDefInitialized(getDef(ParameterizedTypeImpl.create(ReadonlyArray.class, javaType)));
    }

    @Override
    public ModelDef<?, ?> getDef(Type javaType) {
        checkJavaType(javaType);
        javaType = ReflectUtils.getBoxedType(javaType);
        if (!(javaType instanceof TypeVariable<?>)) {
            javaType = ReflectUtils.eraseType(javaType);
            if (javaType instanceof Class<?> klass) {
                if (ReflectUtils.isBoxingClass(klass)) {
                    javaType = BOX_CLASS_MAP.getOrDefault(klass, klass);
                } else {
                    javaType = EntityUtils.getRealType(klass);
                }
            }
        }
        ModelDef<?, ?> existing = javaType2Def.get(javaType);
        if (existing != null) {
            return existing;
        }
        ModelDef<?, ?> def = parseType(javaType);
        if (!processedDefSet.contains(def)) {
            addDef(def);
        }
        return def;
    }

    @Override
    public boolean containsDef(Type javaType) {
        return javaType2Def.containsKey(javaType);
    }

    @Override
    public boolean containsDef(tech.metavm.object.type.Type type) {
        return type2Def.containsKey(type);
    }

    private void checkJavaType(Type javaType) {
        if (javaType instanceof WildcardType && javaType instanceof TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public tech.metavm.object.type.Type getType(Class<?> javaClass) {
        return getDef(javaClass).getType();
    }

    public ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    @SuppressWarnings("unused")
    public java.lang.reflect.Field getJavaField(Field field) {
        Class<?> javaClass = getJavaClass(field.getDeclaringType());
        return ReflectUtils.getDeclaredFieldByName(javaClass, field.getCode());
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return getClassType(javaField.getDeclaringClass()).getFieldByJavaField(javaField);
    }

    @Override
    public tech.metavm.object.type.Type internType(tech.metavm.object.type.Type type) {
        return typeInternMap.computeIfAbsent(type, t -> type);
    }

    @SuppressWarnings("unchecked")
    public <T> ModelDef<T, ?> getDef(Class<T> klass) {
        return (ModelDef<T, ?>) getDef((Type) klass);
    }

    public EntityDef<?> getEntityDef(tech.metavm.object.type.Type type) {
        return (EntityDef<?>) getDef(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> EnumDef<T> getEnumDef(Class<T> enumType) {
        return (EnumDef<T>) getDef(enumType);
    }

    public ModelDef<?, ?> getDef(tech.metavm.object.type.Type type) {
        return NncUtils.requireNonNull(tryGetDef(type), () -> new InternalException("Can not find def for type " + type));
    }

    public @Nullable ModelDef<?,?> tryGetDef(tech.metavm.object.type.Type type) {
        return type2Def.get(type);
    }

    private DefParser<?, ?, ?> getParser(Type javaType) {
         if (javaType instanceof TypeVariable<?> typeVariable) {
            return new TypeVariableParser(typeVariable, this);
        }
        javaType = ReflectUtils.eraseType(javaType);
        Class<?> javaClass = ReflectUtils.getRawClass(javaType);
        TypeCategory typeCategory = ValueUtil.getTypeCategory(javaType);
        if (ReadonlyArray.class.isAssignableFrom(javaClass)) {
            Class<? extends ReadonlyArray<?>> arrayClass = ReadonlyArray.class.asSubclass(
                    new TypeReference<ReadonlyArray<?>>() {
                    }.getType()
            );
            if (javaType instanceof ParameterizedType pType) {
                Type elementJavaType = pType.getActualTypeArguments()[0];
                if ((elementJavaType instanceof Class<?> elementJavaClass) &&
                        Instance.class.isAssignableFrom(elementJavaClass)) {
                    return new InstanceCollectionParser<>(
                            javaType,
                            arrayClass,
                            elementJavaClass,
                            getArrayType(objectDef.getType(), ArrayKind.getByEntityClass(javaClass))
                    );
                }
            }
            return new ArrayParser<>(
                    arrayClass,
                    javaType,
                    this
            );
        } else if (Instance.class.isAssignableFrom(javaClass)) {
            throw new InternalException("Instance def should be predefined by StandardDefBuilder");
        } else {
            return switch (typeCategory) {
                case ENUM -> new EnumParser<>(
                        javaClass.asSubclass(new TypeReference<Enum<?>>() {
                        }.getType()), enumDef,
                        this, getId);
                case CLASS -> new EntityParser<>(javaClass.asSubclass(Entity.class), javaType, this, columnStore);
                case VALUE -> {
                    if (Record.class.isAssignableFrom(javaClass)) {
                        yield new RecordParser<>(
                                javaClass.asSubclass(Record.class), javaType, this, columnStore
                        );
                    } else {
                        yield new ValueParser<>(
                                javaClass,
                                javaType,
                                this,
                                columnStore
                        );
                    }
                }
                case INTERFACE -> new InterfaceParser<>(javaClass, javaType, this, columnStore);
                default -> throw new IllegalStateException("Unexpected value: " + typeCategory);
            };
        }
    }

    private ModelDef<?, ?> parseType(Type javaType) {
        DefParser<?, ?, ?> parser = getParser(javaType);
        for (Type dependencyType : parser.getDependencyTypes()) {
            getDef(dependencyType);
        }
        ModelDef<?, ?> def;
        if ((def = javaType2Def.get(javaType)) != null) {
            return def;
        }
        def = parser.create();
        preAddDef(def);
        parser.initialize();
        afterDefInitialized(def);
        return def;
    }

    @Override
    public boolean containsJavaType(Type javaType) {
        return javaType2Def.containsKey(javaType);
    }

    @Override
    public void preAddDef(ModelDef<?, ?> def) {
        ModelDef<?, ?> existing = javaType2Def.get(def.getJavaType());
        if (existing != null && existing != def) {
            throw new InternalException("Def for java type " + def.getJavaType() + " already exists");
        }
        javaType2Def.put(def.getJavaType(), def);
        if(def.getType() instanceof ArrayType arrayType) {
            getArrayTypeContext(arrayType.getKind()).addNewType(arrayType);
        }
        else if(def.getType() instanceof UnionType unionType) {
            getUnionTypeContext().addNewType(unionType);
        }
        if (!(def instanceof InstanceDef) && !(def instanceof InstanceCollectionDef<?, ?>)) {
            existing = type2Def.get(def.getType());
            if (existing != null && existing != def) {
                throw new InternalException("Def for type " + def.getType() + " already exists");
            }
            type2Def.put(def.getType(), def);
        }
    }

    @Override
    public void addDef(ModelDef<?, ?> def) {
        preAddDef(def);
        afterDefInitialized(def);
    }

    @Override
    public void afterDefInitialized(ModelDef<?, ?> def) {
        if (processedDefSet.contains(def)) {
            return;
        }
        processedDefSet.add(def);
        if (def.getType() instanceof ClassType classType) {
            initializedClassTypes.add(classType);
        }
        writeEntity(def.getType());
        def.getInstanceMapping().forEach((javaConstruct, instance) -> {
            if (!instance.isValue() && instance.getId() == null) {
                Long id = getId.apply(javaConstruct);
                if (id != null) {
                    instance.initId(id);
                }
            }
            addToContext(javaConstruct, instance);
        });
        instanceMapping.putAll(def.getInstanceMapping());
    }

    private void writeEntity(Object entity) {
        Map<Object, ModelIdentity> identityMap = identityContext.getIdentityMap(entity);
        identityMap.forEach((model, modelId) -> {
            if ((model instanceof IdInitializing idInitializing) && idInitializing.getId() == null) {
                Long id = getId.apply(modelId);
                if (id != null) {
                    idInitializing.initId(id);
                }
            }
            if (!containsModel(model)) {
                pendingModels.add(model);
            }
        });
    }

    @SuppressWarnings("unused")
    public Collection<ModelDef<?, ?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Class<?> getJavaClass(tech.metavm.object.type.Type type) {
        return getDef(type).getJavaClass();
    }

    public Type getJavaType(tech.metavm.object.type.Type type) {
        return getDef(type).getJavaType();
    }

    public boolean isClassTypeInitialized(ClassType classType) {
        return initializedClassTypes.contains(classType);
    }

    public <T extends Enum<?>> T getEnumConstant(Class<T> klass, long id) {
        return getEnumDef(klass).getEnumConstantDef(id).getValue();
    }

    public Index getIndexConstraint(IndexDef<?> indexDef) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(indexDef.getType());
        return entityDef.getIndexConstraintDef(indexDef).getIndexConstraint();
    }

    public Map<Object, ModelIdentity> getIdentityMap() {
        return identityContext.getIdentityMap();
    }

    public Map<Object, Instance> getInstanceMapping() {
        return instanceMapping;
    }

    @Override
    public Instance getInstance(Object model) {
        Instance primitiveInst = InstanceUtils.trySerializePrimitive(model, this::getType);
        if(primitiveInst != null)
            return primitiveInst;
        return getInstance(model, null);
    }

    public Instance getInstance(Object model, ModelDef<?, ?> def) {
        if (model instanceof Instance instance) {
            return instance;
        }
        if (pendingModels.contains(model)) {
            generateInstance(model, def);
        }
        return super.getInstance(model);
    }

    @SuppressWarnings("unused")
    private boolean isDebugTarget(Object model) {
        if (model instanceof ReadonlyArray<?> table) {
            Type type = table.getGenericType();
            return type.equals(
                    new TypeReference<ReadonlyArray<Flow>>() {
                    }.getGenericType()
            );
        }
        return false;
    }

    public void generateInstances() {
        while (!pendingModels.isEmpty()) {
            new IdentitySet<>(pendingModels).forEach(this::generateInstance);
        }
    }

    private void generateInstance(Object model) {
        getInstance(model, null);
    }

    private void generateInstance(Object model, ModelDef<?, ?> def) {
        pendingModels.remove(model);
        if (containsModel(model)) {
            return;
        }
        if (def == null) {
            def = getDefByModel(model);
        }
        ModelIdentity identity = identityContext.getIdentity(model);
        Long id = identity != null ? getId.apply(identity) : null;
        if (def.isProxySupported()) {
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType(), id);
            addToContext(model, instance);
            def.initInstanceHelper(instance, model, this);
        } else {
            Instance instance = def.createInstanceHelper(model, this, id);
            addToContext(model, instance);
        }
    }

    private void addToContext(Object model, Instance instance) {
        if(instance.getId() == null)
            // onBind will get invoked
            addBinding(model, instance);
        else
            // add to context without calling onBind
            addMapping(model, instance);
    }

    @Override
    protected boolean manualInstanceWriting() {
        return true;
    }

    @Override
    public DefContext getDefContext() {
        return this;
    }

    @Override
    protected TypeFactory getTypeFactory() {
        return new DefaultTypeFactory(this::getType);
    }

    @Override
    protected <T> void beforeGetModel(Class<T> klass, Instance instance) {
        generateInstances();
    }

    @Override
    public boolean containsModel(Object model) {
        return super.containsModel(model) || pendingModels.contains(model);
    }

    @Override
    public UnionType getNullableType(tech.metavm.object.type.Type type) {
        return getUnionType(Set.of(type, getType(Null.class)));
    }

    public boolean containsNonDirectDef(tech.metavm.object.type.Type type) {
        var def = type2Def.get(type);
        return def != null && !(def instanceof DirectDef<?>);
    }

    @Override
    protected void flush() {
        generateInstances();
    }

    @Override
    protected void writeInstances(IInstanceContext instanceContext) {
        instanceContext.replace(NncUtils.exclude(instances(), Instance::isValue));
    }

    @SuppressWarnings("unused")
    // DEBUG用，勿删！
    public tech.metavm.object.type.Type getTypeByTable(ReadonlyArray<?> table) {
        for (Object model : models()) {
            if (model instanceof ClassType type) {
                if (type.getDeclaredConstraints() == table
                        || type.getDeclaredFields() == table
                        || type.getDeclaredFlows() == table
                ) {
                    return type;
                }
            }
            if (model instanceof UnionType unionType) {
                if (unionType.getDeclaredMembers() == table) {
                    return unionType;
                }
            }
        }
        return null;
    }

    @Override
    public boolean remove(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(Object model) {
        // Entities enter the DefContext through models def.
        throw new UnsupportedOperationException("Binding not supported.");
    }

    @Override
    public boolean isBindSupported() {
        return false;
    }
}
