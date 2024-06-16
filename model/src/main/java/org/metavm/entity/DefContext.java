package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Value;
import org.metavm.flow.Flow;
import org.metavm.flow.Function;
import org.metavm.flow.ScopeRT;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Index;
import org.metavm.object.type.*;
import org.metavm.object.view.Mapping;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

import static org.metavm.object.type.ResolutionStage.*;

public class DefContext extends BaseEntityContext implements DefMap, IEntityContext, TypeRegistry {

    public static final Logger logger = LoggerFactory.getLogger(DefContext.class);
    public static final Set<Class<? extends GlobalKey>> BINDING_ALLOWED_CLASSES = Set.of();

    private final Map<Type, ModelDef<?, ?>> javaType2Def = new HashMap<>();
    private final Map<TypeDef, ModelDef<?, ?>> typeDef2Def = new IdentityHashMap<>();
    private final Map<Integer, ModelDef<?, ?>> typeTag2Def = new HashMap<>();
    private final IdentitySet<ModelDef<?, ?>> processedDefSet = new IdentitySet<>();
    private final IdentitySet<Klass> initializedClassTypes = new IdentitySet<>();
    private final ValueDef<Enum<?>> enumDef;
    private final StdIdProvider stdIdProvider;
    private final Set<Object> pendingModels = new IdentitySet<>();
    private final Set<Object> entities = new IdentitySet<>();
    private final Map<org.metavm.object.type.Type, org.metavm.object.type.Type> typeInternMap = new HashMap<>();
    //    private final Map<Object, DurableInstance> instanceMapping = new IdentityHashMap<>();
    private final IdentityContext identityContext;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private final Map<Type, DefParser<?, ?, ?>> parsers = new HashMap<>();
    private final EntityMemoryIndex memoryIndex = new EntityMemoryIndex();
    private final Map<Id, Object> entityMap = new HashMap<>();
    private final Set<java.lang.reflect.Field> fieldBlacklist = new HashSet<>();
    private final Set<ClassType> typeDefTypes = new HashSet<>();
    private final Set<ClassType> mappingTypes = new HashSet<>();
    private final Set<ClassType> functionTypes = new HashSet<>();

    public static final Map<Class<?>, Class<?>> BOX_CLASS_MAP = Map.ofEntries(
            Map.entry(Byte.class, Long.class),
            Map.entry(Short.class, Long.class),
            Map.entry(Integer.class, Long.class),
            Map.entry(Float.class, Double.class)
    );

    public DefContext(StdIdProvider getId, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this(getId, null, columnStore, typeTagStore, new IdentityContext());
    }

    public DefContext(StdIdProvider stdIdProvider, IInstanceContext instanceContext, ColumnStore columnStore, TypeTagStore typeTagStore, IdentityContext identityContext) {
        super(instanceContext, null);
        this.stdIdProvider = stdIdProvider;
        this.identityContext = identityContext;
        StandardDefBuilder stdBuilder = new StandardDefBuilder(this);
        stdBuilder.initRootTypes();
        enumDef = stdBuilder.getEnumDef();
        this.columnStore = columnStore;
        this.typeTagStore = typeTagStore;
        ColumnKind.columns().forEach(this::writeEntity);
    }

    @Override
    public ModelDef<?, ?> getDef(Type javaType) {
        return getDef(javaType, DEFINITION);
    }

    @Override
    public ModelDef<?, ?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public PojoDef<?> getPojoDef(Type javatype, ResolutionStage stage) {
        return (PojoDef<?>) getDef(javatype, stage);
    }

    public org.metavm.object.type.Type getType(Type javaType) {
        var type = StandardTypes.getPrimitiveType(javaType);
        if (type != null)
            return type;
        if (javaType instanceof Class<?> k && Instance.class.isAssignableFrom(k))
            return new AnyType();
        if (BiUnion.isNullable(javaType))
            return StandardTypes.getNullableType(getType(BiUnion.getUnderlyingType(javaType)));
        if (javaType instanceof ParameterizedType pType) {
            var rawClass = (Class<?>) pType.getRawType();
            var typeArgs = pType.getActualTypeArguments();
            if (ReadonlyArray.class.isAssignableFrom(rawClass))
                return new ArrayType(getType(typeArgs[0]), ArrayKind.fromEntityClass(rawClass));
            else if (List.class.isAssignableFrom(rawClass))
                return StandardTypes.getListKlass()
                        .getParameterized(List.of(getType(typeArgs[0]))).getType();
            else if (Set.class.isAssignableFrom(rawClass))
                return StandardTypes.getSetKlass()
                        .getParameterized(List.of(getType(typeArgs[0]))).getType();
            else if (Map.class.isAssignableFrom(rawClass))
                return StandardTypes.getMapKlass()
                        .getParameterized(List.of(getType(typeArgs[0]), getType(typeArgs[1]))).getType();
            else
                return new ClassType(((ClassType) getDef(rawClass).getType()).getKlass(), NncUtils.map(pType.getActualTypeArguments(), this::getType));
        } else
            return getDef(javaType, INIT).getType();
    }

    public void ensureStage(org.metavm.object.type.Type type, ResolutionStage stage) {
        type.accept(new StructuralTypeVisitor() {
            @Override
            public Void visitClassType(ClassType type, Void unused) {
                var def = Objects.requireNonNull(typeDef2Def.get(type.getKlass()));
                getDef(def.getEntityType(), stage);
                return super.visitClassType(type, unused);
            }
        }, null);
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

    public int getTypeTag(Class<?> javaClass) {
        return typeTagStore.getTypeTag(javaClass.getName());
    }

//    @Override
//    public <T> @Nullable T getEntity(Class<T> entityType, Id id) {
//        if (id.id() != null)
//            return getEntity(entityType, id.id());
//        else
//            return null;
//    }

    @Override
    public TypeRegistry getTypeRegistry() {
        return this;
    }

    @Override
    public <T> T getEntity(Class<T> entityType, Id id) {
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
    public boolean containsDef(TypeDef typeDef) {
        return typeDef2Def.containsKey(typeDef);
    }

    private void checkJavaType(Type javaType) {
        if (javaType instanceof WildcardType && javaType instanceof TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public Klass getKlass(Class<?> javaClass) {
        return (Klass) getDef(javaClass).getTypeDef();
    }

    public org.metavm.object.type.Type getType(Class<?> javaClass) {
        return getType((Type) javaClass);
    }

    public ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    @SuppressWarnings("unused")
    public java.lang.reflect.Field getJavaField(Field field) {
        Class<?> javaClass = getJavaClass(field.getDeclaringType().getType());
        return ReflectionUtils.getDeclaredFieldByName(javaClass, field.getCode());
    }

    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectionUtils.getField(javaType, javaFieldName));
    }

    public Field getField(java.lang.reflect.Field javaField) {
        return ((PojoDef<?>) getDef(javaField.getDeclaringClass(), DECLARATION)).getKlass().getFieldByJavaField(javaField);
    }

    @Override
    public org.metavm.object.type.Type internType(org.metavm.object.type.Type type) {
        return typeInternMap.computeIfAbsent(type, t -> type);
    }

    private Id getEntityId(Object entity) {
        if (entity instanceof Value || EntityUtils.isEphemeral(entity))
            return null;
        //        var type = getType(EntityUtils.getRealType(entity.getClass()));
        return stdIdProvider.getId(identityContext.getModelId(entity));
    }

    @SuppressWarnings("unchecked")
    public <T> ModelDef<T, ?> getDef(Class<T> klass) {
        return (ModelDef<T, ?>) getDef((Type) klass);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> EnumDef<T> getEnumDef(Class<T> enumType) {
        return (EnumDef<T>) getDef(enumType);
    }

    @Override
    public Mapper<?, ?> getMapper(org.metavm.object.type.Type type) {
        return Objects.requireNonNull(tryGetMapper(type), () -> "Can not find mapper for type: " + type.getTypeDesc());
    }

    @Override
    public Mapper<?, ?> getMapper(Type javaType, ResolutionStage stage) {
        var arrayType = tryParseArrayType(javaType);
        if (arrayType != null) {
            var pType = (ParameterizedType) javaType;
            var arrayClass = getArrayClass(arrayType.getKind());
            var elementType = pType.getActualTypeArguments()[0];
            if (elementType instanceof Class<?> klass && Instance.class.isAssignableFrom(klass))
                //noinspection rawtypes,unchecked
                return new InstanceArrayMapper(arrayClass, pType, Instance.class, arrayType);
            else
                //noinspection rawtypes,unchecked
                return new ArrayMapper(arrayClass, this);
        } else if (javaType instanceof Class<?> klass && DurableInstance.class.isAssignableFrom(klass))
            //noinspection rawtypes,unchecked
            return new InstanceMapper(DurableInstance.class.asSubclass(klass));
        else
            return getDef(javaType, stage);
    }

    private @Nullable ArrayType tryParseArrayType(Type javaType) {
        if (javaType instanceof ParameterizedType pType) {
            var rawClass = (Class<?>) pType.getRawType();
            if (ReadonlyArray.class.isAssignableFrom(rawClass)) {
                var elementType = getType(pType.getActualTypeArguments()[0]);
                ArrayKind arrayKind = getArrayKind(rawClass);
                return new ArrayType(elementType, arrayKind);
            }
        }
        return null;
    }

    @NotNull
    private static ArrayKind getArrayKind(Class<?> rawClass) {
        if (rawClass == ReadWriteArray.class)
            return ArrayKind.READ_WRITE;
        if (rawClass == ChildArray.class)
            return ArrayKind.CHILD;
        if (rawClass == ValueArray.class)
            return ArrayKind.VALUE;
        if (rawClass == ReadonlyArray.class)
            return ArrayKind.READ_ONLY;
        throw new InternalException("Unrecognized array class " + rawClass.getName());
    }

    private Class<?> getArrayClass(ArrayKind arrayKind) {
        return switch (arrayKind) {
            case CHILD -> ChildArray.class;
            case READ_WRITE -> ReadWriteArray.class;
            case READ_ONLY -> ReadonlyArray.class;
            case VALUE -> ValueArray.class;
        };
    }

    public @Nullable Mapper<?, ?> tryGetMapper(org.metavm.object.type.Type type) {
        if (type instanceof ArrayType arrayType) {
            var javaClass = switch (arrayType.getKind()) {
                case CHILD -> ChildArray.class;
                case READ_WRITE -> ReadWriteArray.class;
                case READ_ONLY -> ReadonlyArray.class;
                case VALUE -> ValueArray.class;
            };
            //noinspection rawtypes,unchecked
            return new ArrayMapper<>(javaClass, this);
        } else if (type instanceof ClassType classType)
            return tryGetDef(classType.resolve());
        else
            throw new InternalException("Can not get entity mapper for type: " + type.getTypeDesc());
    }

    public Mapper<?, ?> getMapper(int typeTag) {
        return Objects.requireNonNull(tryGetMapper(typeTag), () -> "Can not get mapper for type tag " + typeTag);
    }

    public @Nullable Mapper<?, ?> tryGetMapper(int typeTag) {
        if (typeTag == TypeTags.DEFAULT)
            throw new IllegalArgumentException("Can not get mapper for default type tag");
        if (typeTag <= TypeTags.CHILD_ARRAY) {
            var javaClass = switch (typeTag) {
                case TypeTags.READONLY_ARRAY -> ReadonlyArray.class;
                case TypeTags.READ_WRITE_ARRAY -> ReadWriteArray.class;
                case TypeTags.CHILD_ARRAY -> ChildArray.class;
                default -> throw new IllegalStateException("Should not reach here");
            };
            return new ArrayMapper<>(javaClass, this);
        } else
            return typeTag2Def.get(typeTag);
    }

    public @Nullable ModelDef<?, ?> tryGetDef(TypeDef typeDef) {
        return typeDef2Def.get(typeDef);
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
            throw new InternalException("Can not create parser for an array type: " + javaType.getTypeName());
        } else if (Instance.class.isAssignableFrom(javaClass)) {
            throw new InternalException("Instance def should be predefined by StandardDefBuilder");
        } else {
            NncUtils.requireTrue(javaType == javaClass,
                    "Generic type not supported: " + javaType);
            TypeCategory typeCategory = ValueUtils.getTypeCategory(javaType);
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

    public Map<String, Id> getStdIdMap() {
        var stdIds = new HashMap<String, Id>();
        for (Object entity : entities) {
            var instance = getInstance(entity);
            var id = instance.tryGetId();
            if (id instanceof PhysicalId) {
                var modeId = identityContext.getModelId(entity);
                stdIds.put(modeId.qualifiedName(), id);
            }
        }
        return stdIds;
    }

    @Override
    public void preAddDef(ModelDef<?, ?> def) {
        ModelDef<?, ?> existing = javaType2Def.get(def.getEntityType());
        if (existing != null && existing != def)
            throw new InternalException("Def for java type " + def.getEntityType() + " already exists");
        javaType2Def.put(def.getEntityType(), def);
        existing = typeDef2Def.get(def.getTypeDef());
        if (existing != null && existing != def)
            throw new InternalException("Def for type " + def.getTypeDef() + " already exists. Def: " + existing);
        typeDef2Def.put(def.getTypeDef(), def);
        typeTag2Def.put(def.getType().getTypeTag(), def);
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

    public final Map<org.metavm.flow.Function, ScopeRT> originalScopes = new IdentityHashMap<>();

    private void writeEntityIfNotPresent(Object entity) {
        if (!entities.contains(entity))
            writeEntity(entity);
    }

    private void tryInitDefEntityIds(ModelDef<?, ?> def) {
        def.getEntities().forEach(entity -> EntityUtils.forEachDescendant(entity, this::tryInitEntityId));
    }

    private void tryInitEntityId(Object entity) {
        if (EntityUtils.isDurable(entity)) {
            if ((entity instanceof IdInitializing idInitializing) && idInitializing.tryGetId() == null) {
                var id = getEntityId(entity);
                if (id != null) {
                    idInitializing.initId(id);
                    entityMap.put(id, idInitializing);
                }
            }
        }
    }

    void writeEntity(Object entity) {
        if (entities.add(entity)) {
            pendingModels.add(entity);
            if (!(entity instanceof Value)) {
                tryInitEntityId(entity);
                memoryIndex.save(entity);
            }
        } else
            throw new InternalException("Entity " + entity + " is already written to the context");
    }

    @SuppressWarnings("unused")
    public Collection<ModelDef<?, ?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Class<?> getJavaClass(org.metavm.object.type.Type type) {
        return getMapper(type).getEntityClass();
    }

    public Type getJavaType(org.metavm.object.type.Type type) {
        var javaType = StandardTypes.getPrimitiveJavaType(type);
        if (javaType != null)
            return javaType;
        if (type.isBinaryNullable())
            return BiUnion.createNullableType(getJavaType(type.getUnderlyingType())); // TODO maybe we should not use BiUnion here
        if (type instanceof ArrayType arrayType)
            return ParameterizedTypeImpl.create(arrayType.getKind().getEntityClass(), getJavaType(arrayType.getElementType()));
        else if (type instanceof ClassType classType)
            return getDef(classType.resolve()).getEntityType();
        else
            throw new IllegalArgumentException("Can not get java type for type " + type);
    }

    public boolean isClassTypeInitialized(Klass classType) {
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
        try (var ignored = getProfiler().enter("DefContext.generateInstances")) {
            while (!pendingModels.isEmpty()) {
                new IdentitySet<>(pendingModels).forEach(this::generateInstance);
            }
        }
    }

    private void generateInstance(Object model) {
        getInstance(model, null);
    }

    private void generateInstance(Object model, Mapper<?, ?> mapper) {
        if (EntityUtils.isOrphaned(model))
            logger.error("Encounter orphaned entity: {}", EntityUtils.getEntityPath(model));
        pendingModels.remove(model);
        if (isInstanceGenerated(model))
            return;
        if (mapper == null)
            mapper = getMapperByEntity(model);
        var id = getEntityId(model);
        if (id == null) {
            if (mapper.isProxySupported()) {
                var instance = InstanceFactory.allocate(mapper.getInstanceClass(), null,
                        EntityUtils.isEphemeral(model));
                addToContext(model, instance);
                mapper.initInstanceHelper(instance, model, getObjectInstanceMap());
            } else {
                var instance = mapper.createInstanceHelper(model, getObjectInstanceMap(), null);
                addToContext(model, instance);
            }
        } else {
            var instance = getInstanceContext().get(id);
            addToContext(model, instance);
            mapper.updateInstanceHelper(model, instance, getObjectInstanceMap());
        }
    }

    private void addToContext(Object model, DurableInstance instance) {
        if (instance.tryGetTreeId() == null)
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
    public boolean containsEntity(Object entity) {
        return entities.contains(entity);
//        return super.containsModel(model) || pendingModels.contains(model);
    }

    private boolean isInstanceGenerated(Object entity) {
        return super.containsEntity(entity);
    }

    public boolean containsNonDirectDef(org.metavm.object.type.Type type) {
        var def = typeDef2Def.get(type);
        return def != null && !(def instanceof DirectDef<?>);
    }

    private void ensureAllDefsDefined() {
        parsers.values().forEach(p -> ensureStage(p.get().getType(), DEFINITION));
    }

    @Override
    protected void flush() {
        try (var ignored = getProfiler().enter("flush")) {
            ensureAllDefsDefined();
            int numPending = pendingModels.size();
            crawNewEntities();
            long delta = pendingModels.size() - numPending;
            logger.info("{} new entities generated during flush", delta);
            generateInstances();
            for (Object entity : entities) {
                if (entity instanceof Entity e && e.tryGetPhysicalId() != null && e.afterContextInitIds()) {
                    update(e);
                }
            }
            recordHotTypes();
        }
    }

    @Override
    public boolean isTypeDefType(ClassType type) {
        return typeDefTypes.contains(type);
    }

    @Override
    public boolean isMappingType(ClassType type) {
        return mappingTypes.contains(type);
    }

    @Override
    public boolean isFunctionType(ClassType type) {
        return functionTypes.contains(type);
    }

    private void recordHotTypes() {
        typeDefTypes.clear();
        mappingTypes.clear();
        functionTypes.clear();
        var typeDefKlass = getKlass(TypeDef.class);
        var mappingKlass = getKlass(Mapping.class);
        var functionKlass = getKlass(Function.class);
        typeDef2Def.keySet().forEach(typeDef -> {
            if (typeDef instanceof Klass klass) {
                if (typeDefKlass.isAssignableFrom(klass))
                    typeDefTypes.add(klass.getType());
                else if (mappingKlass.isAssignableFrom(klass))
                    mappingTypes.add(klass.getType());
                else if (functionKlass.isAssignableFrom(klass))
                    functionTypes.add(klass.getType());
            }
        });
    }

    private void crawNewEntities() {
        try (var entry = getProfiler().enter("crawNewEntities")) {
            entry.addMessage("numSeedEntities", entities.size());
            List<Object> newEntities = new ArrayList<>();
            EntityUtils.visitGraph(entities, e -> {
                if (!(e instanceof Instance) && !entities.contains(e)/* TODO handle instance */) {
                    newEntities.add(e);
                }
            });
            try (var ignored = getProfiler().enter("crawNewEntities")) {
                newEntities.forEach(this::writeEntity);
            }
        }
    }

    @Override
    protected void writeInstances(IInstanceContext instanceContext) {
        try (var ignored = getProfiler().enter("writeInstances ")) {
            instanceContext.batchBind(NncUtils.exclude(instances(), instanceContext::containsInstance));
        }
    }

    @SuppressWarnings("unused")
    // For debugging, DON'T REMOVE!!!
    public org.metavm.object.type.Type getTypeByTable(ReadonlyArray<?> table) {
        for (Object model : models()) {
            if (model instanceof ClassType type) {
                var klass = type.resolve();
                if (klass.getDeclaredConstraints() == table
                        || klass.getDeclaredFields() == table
                        || klass.getDeclaredMethods() == table
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
    public <T> T bind(T entiy) {
        if (entiy instanceof Entity entity && entity.isEphemeralEntity())
            throw new IllegalArgumentException("Can not bind an ephemeral entity");
        if (BINDING_ALLOWED_CLASSES.contains(EntityUtils.getRealType(entiy.getClass()))) {
            writeEntity(entiy);
            return entiy;
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

    public IdentityContext getIdentityContext() {
        return identityContext;
    }

    public Collection<Object> getEntities() {
        return Collections.unmodifiableSet(entities);
    }
}
