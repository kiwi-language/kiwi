package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

import static tech.metavm.object.type.ResolutionStage.*;

public class DefContext extends BaseEntityContext implements DefMap, IEntityContext, TypeRegistry {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefContext.class);
    public static final Set<Class<? extends GlobalKey>> BINDING_ALLOWED_CLASSES = Set.of();

    private final Map<Type, ModelDef<?, ?>> javaType2Def = new HashMap<>();
    private final Map<tech.metavm.object.type.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
    private final IdentitySet<ModelDef<?, ?>> processedDefSet = new IdentitySet<>();
    private final IdentitySet<ClassType> initializedClassTypes = new IdentitySet<>();
    private final DirectDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final Function<Object, Long> getId;
    private final Set<Object> pendingModels = new IdentitySet<>();
    private final Set<Object> entities = new IdentitySet<>();
    private final Map<tech.metavm.object.type.Type, tech.metavm.object.type.Type> typeInternMap = new HashMap<>();
//    private final Map<Object, DurableInstance> instanceMapping = new IdentityHashMap<>();
    private final IdentityContext identityContext = new IdentityContext();
    private final ColumnStore columnStore;
    private final Map<Type, DefParser<?, ?, ?>> parsers = new HashMap<>();
    private final EntityMemoryIndex memoryIndex = new EntityMemoryIndex();
    private final Map<Long, Object> entityMap = new HashMap<>();
    private final Set<java.lang.reflect.Field> fieldBlacklist = new HashSet<>();

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

    public void createCompositeTypes(tech.metavm.object.type.Type type) {
        predefineCompositeTypes(type);
    }

    public void predefineCompositeTypes(tech.metavm.object.type.Type type) {
        getNullableType(type);
        getArrayType(type, ArrayKind.READ_WRITE);
        getArrayType(type, ArrayKind.CHILD);
        getArrayType(type, ArrayKind.READ_ONLY);
    }

    @Override
    public ModelDef<?, ?> getDef(Type javaType) {
        return getDef(javaType, DEFINITION);
    }

    public PojoDef<?> getPojoDef(Type javatype, ResolutionStage stage) {
        return (PojoDef<?>) getDef(javatype, stage);
    }

    public tech.metavm.object.type.Type getType(Type javaType, ResolutionStage stage) {
        if (BiUnion.isNullable(javaType))
            return getNullableType(getType(BiUnion.getUnderlyingType(javaType), stage));
        else
            return getDef(javaType, stage).getType();
    }

    public void ensureStage(tech.metavm.object.type.Type type, ResolutionStage stage) {
        var def = Objects.requireNonNull(type2Def.get(type));
        getDef(def.getJavaType(), stage);
    }

    public ModelDef<?, ?> getDef(Type javaType, ResolutionStage stage) {
        checkJavaType(javaType);
        javaType = ReflectionUtils.getBoxedType(javaType);
        if (!(javaType instanceof TypeVariable<?>)) {
            javaType = EntityUtils.getEntityType(javaType);
            if (javaType instanceof Class<?> klass) {
                if (ReflectionUtils.isBoxingClass(klass))
                    javaType = BOX_CLASS_MAP.getOrDefault(klass, klass);
                else
                    javaType = EntityUtils.getRealType(klass);
            }
        }
        ModelDef<?, ?> existing = javaType2Def.get(javaType);
        if (existing != null && existing.getParser() == null)
            return existing;
        return parseType(javaType, stage);
    }

    public boolean isFieldBlacklisted(java.lang.reflect.Field field) {
        return fieldBlacklist.contains(field);
    }

    public void setFieldBlacklist(Set<java.lang.reflect.Field> fieldBlacklist) {
        this.fieldBlacklist.addAll(fieldBlacklist);
    }

    @Override
    public <T> @Nullable T getEntity(Class<T> entityType, RefDTO ref) {
        if (ref.id() != null)
            return getEntity(entityType, ref.id());
        else
            return null;
    }

    @Override
    public <T> T getEntity(Class<T> entityType, long id) {
        return entityType.cast(entityMap.get(id));
    }

    @Override
    public void onInstanceIdInit(DurableInstance instance) {
        super.onInstanceIdInit(instance);
        var entity = instance.getMappedEntity();
        if (entity instanceof IdInitializing idInitializing)
            entityMap.put(idInitializing.getId(), idInitializing);
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
        return ReflectionUtils.getDeclaredFieldByName(javaClass, field.getCode());
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectionUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return getClassType(javaField.getDeclaringClass()).getFieldByJavaField(javaField);
    }

    @Override
    public tech.metavm.object.type.Type internType(tech.metavm.object.type.Type type) {
        return typeInternMap.computeIfAbsent(type, t -> type);
    }

    private Long getEntityId(Object entity) {
        return EntityUtils.isEphemeral(entity) ? null : getId.apply(identityContext.getModelId(entity));
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

    public @Nullable ModelDef<?, ?> tryGetDef(tech.metavm.object.type.Type type) {
        return type2Def.get(type);
    }

    private DefParser<?, ?, ?> getParser(Type javaType) {
        javaType = EntityUtils.getEntityType(javaType);
        var parser = parsers.get(javaType);
        if (parser != null)
            return parser;
        parser = createParser(javaType);
        parsers.put(javaType, parser);
        return parser;
    }

    private DefParser<?, ?, ?> createParser(Type javaType) {
        Class<?> javaClass = ReflectionUtils.getRawClass(javaType);
        if (ReadonlyArray.class.isAssignableFrom(javaClass)) {
            Class<? extends ReadonlyArray<?>> arrayClass = javaClass.asSubclass(
                    new TypeReference<ReadonlyArray<?>>() {
                    }.getType()
            );
            if (javaType instanceof ParameterizedType pType) {
                Type elementJavaType = pType.getActualTypeArguments()[0];
                if ((elementJavaType instanceof Class<?> elementJavaClass)
                        && Instance.class.isAssignableFrom(elementJavaClass)) {
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
            NncUtils.requireTrue(javaType == javaClass,
                    "Generic type not supported: " + javaType);
            TypeCategory typeCategory = ValueUtil.getTypeCategory(javaType);
            return switch (typeCategory) {
                case ENUM -> new EnumParser<>(
                        javaClass.asSubclass(new TypeReference<Enum<?>>() {
                        }.getType()), enumDef,
                        this, this::getEntityId);
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

    private ModelDef<?, ?> parseType(Type javaType, ResolutionStage stage) {
        var parser = parsers.get(javaType);
        ModelDef<?, ?> def;
        if (parser == null) {
            NncUtils.requireNull(javaType2Def.get(javaType));
            parser = getParser(javaType);
            def = parser.create();
            //noinspection unchecked,rawtypes
            def.setParser((DefParser) parser);
            preAddDef(def);
        } else
            def = parser.get();
        var curStage = parser.setStage(stage);
        if (curStage.isBefore(SIGNATURE) && stage.isAfterOrAt(SIGNATURE)) {
            parser.generateSignature();
            tryInitDefEntityIds(def);
        }
        if (curStage.isBefore(DECLARATION) && stage.isAfterOrAt(DECLARATION)) {
            parser.generateDeclaration();
            tryInitDefEntityIds(def);
        }
        if (curStage.isBefore(DEFINITION) && stage.isAfterOrAt(DEFINITION)) {
            parser.generateDefinition();
            afterDefInitialized(def);
        }
        return def;
    }

    @Override
    public boolean containsJavaType(Type javaType) {
        return javaType2Def.containsKey(javaType);
    }

    @Override
    public void preAddDef(ModelDef<?, ?> def) {
        ModelDef<?, ?> existing = javaType2Def.get(def.getJavaType());
        if (existing != null && existing != def)
            throw new InternalException("Def for java type " + def.getJavaType() + " already exists");
        javaType2Def.put(def.getJavaType(), def);
        if (def.getType() instanceof ArrayType arrayType)
            getArrayTypeContext(arrayType.getKind()).addNewType(arrayType);
        else if (def.getType() instanceof UnionType unionType)
            getUnionTypeContext().addNewType(unionType);
        if (!(def instanceof InstanceDef) && !(def instanceof InstanceCollectionDef<?, ?>)) {
            existing = type2Def.get(def.getType());
            if (existing != null && existing != def)
                throw new InternalException("Def for type " + def.getType() + " already exists. Def: " + existing);
            type2Def.put(def.getType(), def);
        }
        tryInitDefEntityIds(def);
    }

    @Override
    public void addDef(ModelDef<?, ?> def) {
        preAddDef(def);
        afterDefInitialized(def);
    }

    @Override
    public void onInstanceInitialized(DurableInstance instance) {
    }

    @Override
    public void afterDefInitialized(ModelDef<?, ?> def) {
        if (processedDefSet.contains(def))
            return;
//        identityContext.unmarkPending(def.getType());
        tryInitDefEntityIds(def);
        processedDefSet.add(def);
        def.getEntities().forEach(this::writeEntityIfNotPresent);
//        if (def.getType() instanceof ClassType classType)
//            initializedClassTypes.add(classType);
//        writeEntity(def.getType());
//        def.getInstanceMapping().forEach((javaConstruct, instance) -> {
//            if (!instance.isValue() && instance.getId() == null) {
//                Long id = getId.apply(identityContext.getModelId(javaConstruct));
//                if (id != null)
//                    instance.initId(id);
//            }
//            addToContext(javaConstruct, instance);
//        });
//        instanceMapping.putAll(def.getInstanceMapping());
    }

    public final Map<tech.metavm.flow.Function, ScopeRT> originalScopes = new IdentityHashMap<>();

    private void writeEntityIfNotPresent(Object entity) {
        if (!entities.contains(entity))
            writeEntity(entity);
    }

    private void tryInitDefEntityIds(ModelDef<?,?> def) {
        def.getEntities().forEach(entity -> EntityUtils.forEachDescendant(entity, this::tryInitEntityId));
    }

    private void tryInitEntityId(Object entity) {
        if (EntityUtils.isDurable(entity)) {
            if ((entity instanceof IdInitializing idInitializing) && idInitializing.tryGetId() == null) {
                Long id = getEntityId(entity);
                if (id != null) {
                    idInitializing.initId(id);
                    entityMap.put(id, idInitializing);
                }
            }
        }
    }

    void writeEntity(Object entity) {
        if (entities.add(entity)) {
            tryInitEntityId(entity);
//            if (!containsModel(entity)) {
            pendingModels.add(entity);
            memoryIndex.save(entity);
//            }
//            var identityMap = identityContext.getIdentityMap(entity);
//            identityMap.forEach((object, modelId) -> {
//                if (EntityUtils.isMapper(object)) {
//                    var func = (tech.metavm.flow.Function) object;
//                    if (func.isRootScopePresent())
//                        originalScopes.put(func, func.getRootScope());
//                    else
//                        System.out.println("Caught mapper with null root scope");
//                }
//                if ((object instanceof IdInitializing idInitializing) && idInitializing.getId() == null) {
//                    Long id = getId.apply(modelId);
//                    if (id != null) {
//                        idInitializing.initId(id);
//                        entityMap.put(id, idInitializing);
//                    }
//                }
//                if (!containsModel(object)) {
//                    pendingModels.add(object);
//                    memoryIndex.save(object);
//                }
//            });
        } else
            throw new InternalException("Entity " + entity + " is already written to the context");
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

//    public Map<Object, DurableInstance> getInstanceMapping() {
//        return instanceMapping;
//    }

    @Override
    public DurableInstance getInstance(Object model) {
        return getInstance(model, null);
    }

    public DurableInstance getInstance(Object model, ModelDef<?, ?> def) {
        if (model instanceof DurableInstance d)
            return d;
        if (pendingModels.contains(model))
            generateInstance(model, def);
//        assert isInstanceGenerated(model);
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
        try(var ignored = getProfiler().enter("generateInstances")) {
            while (!pendingModels.isEmpty()) {
                new IdentitySet<>(pendingModels).forEach(this::generateInstance);
            }
        }
    }

    private void generateInstance(Object model) {
        getInstance(model, null);
    }

    private void generateInstance(Object model, ModelDef<?, ?> def) {
        pendingModels.remove(model);
        if (isInstanceGenerated(model))
            return;
        if (def == null)
            def = getDefByModel(model);
        Long id = getEntityId(model);
        if (id == null) {
            if (def.isProxySupported()) {
                var instance = InstanceFactory.allocate(def.getInstanceType(), def.getType(), NncUtils.get(id, PhysicalId::new),
                        EntityUtils.isEphemeral(model));
                addToContext(model, instance);
                def.initInstanceHelper(instance, model, getObjectInstanceMap());
            } else {
                var instance = def.createInstanceHelper(model, getObjectInstanceMap(), id);
                addToContext(model, instance);
            }
        } else {
            var instance = getInstanceContext().get(id);
            addToContext(model, instance);
            def.updateInstanceHelper(model, instance, getObjectInstanceMap());
        }
    }

    private void addToContext(Object model, DurableInstance instance) {
        if (instance.tryGetPhysicalId() == null)
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
    protected void updateMemIndex(Object object) {
        super.updateMemIndex(object);
        memoryIndex.save(object);
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
    protected <T> void beforeGetModel(Class<T> klass, DurableInstance instance) {
        generateInstances();
    }

    @Override
    public boolean containsModel(Object model) {
        return entities.contains(model);
//        return super.containsModel(model) || pendingModels.contains(model);
    }

    private boolean isInstanceGenerated(Object entity) {
        return super.containsModel(entity);
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
        try(var ignored = getProfiler().enter("flush")) {
            parsers.values().forEach(p -> ensureStage(p.get().getType(), DEFINITION));
            int numPending = pendingModels.size();
            for (ClassType newType : getGenericContext().getNewTypes()) {
                writeEntityIfNotPresent(newType);
            }
            for (CompositeType newCompositeType : getNewCompositeTypes()) {
                writeEntityIfNotPresent(newCompositeType);
            }
            crawNewEntities();
            long delta = pendingModels.size() - numPending;
            LOGGER.info("{} new entities generated during flush", delta);
            generateInstances();
        }
    }

    private void crawNewEntities() {
        try(var entry = getProfiler().enter("crawNewEntities")) {
            entry.addMessage("numSeedEntities", entities.size());
            List<Object> newEntities = new ArrayList<>();
            EntityUtils.visitGraph(entities, e -> {
                if (!(e instanceof Instance) && !entities.contains(e)/* TODO handle instance */)
                    newEntities.add(e);
            });
            try(var ignored = getProfiler().enter("crawNewEntities")) {
                newEntities.forEach(this::writeEntity);
            }
        }
    }

    @Override
    protected void writeInstances(IInstanceContext instanceContext) {
        try(var ignored = getProfiler().enter("writeInstances ")) {
            instanceContext.batchBind(NncUtils.exclude(instances(), instanceContext::containsInstance));
        }
    }

    @SuppressWarnings("unused")
    // DEBUG用，勿删！
    public tech.metavm.object.type.Type getTypeByTable(ReadonlyArray<?> table) {
        for (Object model : models()) {
            if (model instanceof ClassType type) {
                if (type.getDeclaredConstraints() == table
                        || type.getDeclaredFields() == table
                        || type.getDeclaredMethods() == table
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
    public <T> List<T> getByType(Class<? extends T> javaType, @Nullable T startExclusive, long limit) {
        return memoryIndex.selectByType(javaType, startExclusive, limit);
    }

    @Override
    public <T> List<T> query(EntityIndexQuery<T> query) {
        return memoryIndex.query(query);
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values) {
        return memoryIndex.selectByKey(indexDef, List.of(values));
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
        return memoryIndex.selectByUniqueKey(indexDef, List.of(values));
    }

    @Override
    public boolean remove(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T bind(T model) {
        if (model instanceof Entity entity && entity.isEphemeralEntity())
            throw new IllegalArgumentException("Can not bind an ephemeral entity");
        if (BINDING_ALLOWED_CLASSES.contains(EntityUtils.getRealType(model.getClass()))) {
            writeEntity(model);
            return model;
        } else
            // Entities enter the DefContext through models def.
            throw new UnsupportedOperationException("Binding not supported.");
    }

    @Override
    public boolean isBindSupported() {
        return false;
    }

    @Override
    public IEntityContext createSame(long appId) {
        throw new UnsupportedOperationException();
    }

}
