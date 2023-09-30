package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.Index;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

public class DefContext extends BaseEntityContext implements DefMap, IEntityContext {
    private final Map<Type, ModelDef<?, ?>> javaType2Def = new HashMap<>();
    private final Map<tech.metavm.object.meta.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
    private final IdentitySet<ModelDef<?, ?>> processedDefSet = new IdentitySet<>();
    private final IdentitySet<ClassType> initializedClassTypes = new IdentitySet<>();
    private final ObjectTypeDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final Function<Object, Long> getId;
    private final Set<Object> pendingModels = new IdentitySet<>();
    private final Map<tech.metavm.object.meta.Type, tech.metavm.object.meta.Type> typeInternMap = new HashMap<>();
    private final Map<Object, Instance> instanceMapping = new IdentityHashMap<>();
    private final IdentityContext identityContext = new IdentityContext(this::isClassTypeInitialized, this::getJavaType);

    public static final Map<Class<?>, Class<?>> BOX_CLASS_MAP = Map.ofEntries(
            Map.entry(Byte.class, Long.class),
            Map.entry(Short.class, Long.class),
            Map.entry(Integer.class, Long.class),
            Map.entry(Float.class, Double.class)
    );

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

    private void checkJavaType(Type javaType) {
        if (javaType instanceof WildcardType && javaType instanceof TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public tech.metavm.object.meta.Type getType(Class<?> javaClass) {
        return getDef(javaClass).getType();
    }

    public ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    @SuppressWarnings("unused")
    public java.lang.reflect.Field getJavaField(Field field) {
        Class<?> javaClass = getJavaClass(field.getDeclaringType());
        return ReflectUtils.getDeclaredFieldByMetaFieldName(javaClass, field.getName());
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return getClassType(javaField.getDeclaringClass()).getFieldByJavaField(javaField);
    }

    @Override
    public tech.metavm.object.meta.Type internType(tech.metavm.object.meta.Type type) {
        return typeInternMap.computeIfAbsent(type, t -> type);
    }

    @SuppressWarnings("unchecked")
    public <T> ModelDef<T, ?> getDef(Class<T> klass) {
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
        return NncUtils.requireNonNull(type2Def.get(type), () -> new InternalException("Can not find def for type " + type));
    }

    private DefParser<?, ?, ?> getParser(Type javaType) {
         if (javaType instanceof TypeVariable<?> typeVariable) {
            return new TypeVariableParser(typeVariable, this);
        }
        javaType = ReflectUtils.eraseType(javaType);
        Class<?> javaClass = ReflectUtils.getRawClass(javaType);
        TypeCategory typeCategory = ValueUtil.getTypeCategory(javaType);
        if (Table.class.isAssignableFrom(javaClass) || List.class.isAssignableFrom(javaClass)) {
            Class<? extends Table<?>> collectionClass = Table.class.asSubclass(
                    new TypeReference<Table<?>>() {
                    }.getType()
            );
            if (javaType instanceof ParameterizedType pType) {
                Type elementJavaType = pType.getActualTypeArguments()[0];
                if ((elementJavaType instanceof Class<?> elementJavaClass) &&
                        Instance.class.isAssignableFrom(elementJavaClass)) {
                    var typeFactory = new DefaultTypeFactory(this::getType);
                    return new InstanceCollectionParser<>(
                            javaType,
                            collectionClass,
                            elementJavaClass,
                            TypeUtil.getArrayType(objectDef.getType(), typeFactory)
                    );
                }
            }
            return new CollectionParser<>(
                    collectionClass,
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
                        this);
                case CLASS -> new EntityParser<>(javaClass.asSubclass(Entity.class), javaType, this);
                case VALUE -> {
                    if (Record.class.isAssignableFrom(javaClass)) {
                        yield new RecordParser<>(
                                javaClass.asSubclass(Record.class), javaType, this
                        );
                    } else {
                        yield new ValueParser<>(
                                javaClass,
                                javaType,
                                this
                        );
                    }
                }
                case INTERFACE -> new InterfaceParser<>(javaClass, javaType, this);
                default -> throw new IllegalStateException("Unexpected value: " + typeCategory);
            };
        }
    }

    private ModelDef<?, ?> parseType(Type genericType) {
        DefParser<?, ?, ?> parser = getParser(genericType);
        for (Type dependencyType : parser.getDependencyTypes()) {
            getDef(dependencyType);
        }
        ModelDef<?, ?> def;
        if ((def = javaType2Def.get(genericType)) != null) {
            return def;
        }
        def = parser.create();
        preAddDef(def);
        parser.initialize();
        afterDefInitialized(def);
        return def;
    }

    @Override
    public void preAddDef(ModelDef<?, ?> def) {
        ModelDef<?, ?> existing = javaType2Def.get(def.getJavaType());
        if (existing != null && existing != def) {
            throw new InternalException("Def for java type " + def.getJavaType() + " already exists");
        }
        javaType2Def.put(def.getJavaType(), def);
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
        Map<Object, ModelIdentity> identityMap = identityContext.getIdentityMap(def.getType());
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

        def.getInstanceMapping().forEach((javaConstruct, instance) -> {
            if (!instance.isValue() && instance.getId() == null) {
                Long id = getId.apply(javaConstruct);
                if (id != null) {
                    instance.initId(id);
                }
            }
            addMapping(javaConstruct, instance);
        });
        instanceMapping.putAll(def.getInstanceMapping());
    }

    @SuppressWarnings("unused")
    public Collection<ModelDef<?, ?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Class<?> getJavaClass(tech.metavm.object.meta.Type type) {
        return getDef(type).getJavaClass();
    }

    public Type getJavaType(tech.metavm.object.meta.Type type) {
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
        if (model instanceof Table<?> table) {
            Type type = table.getGenericType();
            return type.equals(
                    new TypeReference<Table<Flow>>() {
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
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType());
            if (id != null) {
                instance.initId(id);
            }
            addMapping(model, instance);
            def.initInstanceHelper(instance, model, this);
        } else {
            Instance instance = def.createInstanceHelper(model, this);
            if (id != null && instance.getId() == null) {
                instance.initId(id);
            }
            addMapping(model, instance);
        }
    }

    @Override
    protected boolean manualInstanceWriting() {
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
    protected void flush() {
        generateInstances();
    }

    @Override
    protected void writeInstances(IInstanceContext instanceContext) {
        instanceContext.replace(NncUtils.filterNot(instances(), Instance::isValue));
    }

    @SuppressWarnings("unused")
    // DEBUG用，勿删！
    public tech.metavm.object.meta.Type getTypeByTable(Table<?> table) {
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
        throw new UnsupportedOperationException();
    }

}
