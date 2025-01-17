package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ValueObject;
import org.metavm.entity.natives.StandardStaticMethods;
import org.metavm.event.EventQueue;
import org.metavm.flow.Function;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;

import static org.metavm.object.type.ResolutionStage.*;

@Slf4j
public class SystemDefContext extends DefContext implements DefMap, IEntityContext, TypeRegistry {

    public static final Set<Class<? extends GlobalKey>> BINDING_ALLOWED_CLASSES = Set.of();

    private final Map<Type, KlassDef<?>> javaType2Def = new HashMap<>();
    private final Map<TypeDef, KlassDef<?>> typeDef2Def = new IdentityHashMap<>();
    private final StdIdProvider stdIdProvider;
    private final Set<Entity> entities = new IdentitySet<>();
    private final IdentityContext identityContext;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private final Map<Type, KlassParser<?>> parsers = new HashMap<>();
    private final EntityMemoryIndex memoryIndex = new EntityMemoryIndex();
    private final Map<Id, Entity> entityMap = new HashMap<>();
    private final Set<java.lang.reflect.Field> fieldBlacklist = new HashSet<>();
    private final Set<ClassType> typeDefTypes = new HashSet<>();
    private final Set<ClassType> functionTypes = new HashSet<>();
    private final StandardDefBuilder standardDefBuilder;
    private final IdInitializer idInitializer;
    private final Profiler profiler = new Profiler();

    public SystemDefContext(StdIdProvider getId, ColumnStore columnStore, TypeTagStore typeTagStore, IdInitializer idInitializer) {
        this(getId, columnStore, typeTagStore, new IdentityContext(), idInitializer);
    }

    public SystemDefContext(StdIdProvider stdIdProvider, ColumnStore columnStore, TypeTagStore typeTagStore, IdentityContext identityContext,
                            IdInitializer idInitializer) {
        this.stdIdProvider = stdIdProvider;
        this.identityContext = identityContext;
        this.typeTagStore = typeTagStore;
        standardDefBuilder = new StandardDefBuilder(this);
        standardDefBuilder.initRootTypes();
        this.columnStore = columnStore;
        this.idInitializer = idInitializer;
    }

    @Override
    public KlassDef<?> getDef(Type javaType) {
        return getDef(javaType, DEFINITION);
    }

    @Override
    public KlassDef<?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public void ensureStage(org.metavm.object.type.Type type, ResolutionStage stage) {
        type.accept(new StructuralTypeVisitor() {
            @Override
            public Void visitKlassType(KlassType type, Void unused) {
                var def = Objects.requireNonNull(typeDef2Def.get(type.getKlass()));
                getDef(def.getJavaClass(), stage);
                return super.visitKlassType(type, unused);
            }
        }, null);
    }

    public KlassDef<?> getDef(Type javaType, ResolutionStage stage) {
        checkJavaType(javaType);
        javaType = ReflectionUtils.getBoxedType(javaType);
        if (!(javaType instanceof TypeVariable<?>)) {
            javaType = EntityUtils.getEntityType(javaType);
            if (javaType instanceof Class<?> klass) {
                if (ReflectionUtils.isPrimitiveWrapper(klass))
                    javaType = BOX_CLASS_MAP.getOrDefault(klass, klass);
                else
                    javaType = EntityUtils.getRealType(klass);
            }
        }
        var existing = javaType2Def.get(javaType);
        if (existing != null && existing.getParser() == null)
            return existing;
        return parseType(javaType, stage);
    }

    @SuppressWarnings("unused")
    public boolean isFieldBlacklisted(java.lang.reflect.Field field) {
        return fieldBlacklist.contains(field);
    }

    public void setFieldBlacklist(Set<java.lang.reflect.Field> fieldBlacklist) {
        this.fieldBlacklist.addAll(fieldBlacklist);
    }

    public int getTypeTag(Class<?> javaClass) {
        return typeTagStore.getTypeTag(javaClass.getName());
    }

    @Override
    public <T> T getEntity(Class<T> entityType, Id id) {
        return entityType.cast(entityMap.get(id));
    }

    @Override
    public boolean containsDef(Type javaType) {
        return javaType2Def.containsKey(javaType);
    }

    public Klass getKlass(Class<?> javaClass) {
        return getDef(javaClass).getTypeDef();
    }

    private Id getEntityId(Entity entity) {
        if (entity instanceof ValueObject || entity.isEphemeral())
            return null;
        return stdIdProvider.getId(identityContext.getModelId(entity));
    }

    @SuppressWarnings("unchecked")
    public <T> KlassDef<T> getDef(Class<T> klass) {
        return (KlassDef<T>) getDef((Type) klass);
    }

    @Nullable
    @Override
    public KlassDef<?> getDefIfPresent(Type javaType) {
        return javaType2Def.get(javaType);
    }

    @Override
    public boolean containsDef(TypeDef typeDef) {
        return typeDef2Def.containsKey(typeDef);
    }

    public @Nullable KlassDef<?> tryGetDef(TypeDef typeDef) {
        return typeDef2Def.get(typeDef);
    }

    private KlassParser<?> getParser(Type javaType) {
        javaType = EntityUtils.getEntityType(javaType);
        var parser = parsers.get(javaType);
        if (parser != null)
            return parser;
        parser = createParser(javaType);
        parsers.put(javaType, parser);
        return parser;
    }

    private KlassParser<?> createParser(Type javaType) {
        Class<?> javaClass = ReflectionUtils.getRawClass(javaType);
        if (Value.class == javaClass || Reference.class == javaClass) {
            throw new InternalException("Instance def should be predefined by StandardDefBuilder");
        } else {
            Utils.require(javaType == javaClass,
                    "Generic type not supported: " + javaType);
            return new KlassParser<>(javaClass, javaType, this, columnStore);
        }
    }

    private @NotNull KlassDef<?> parseType(Type javaType, ResolutionStage stage) {
        if(javaType instanceof Class<?> javaClass && javaClass.getName().startsWith("java.")) {
            var def = new KlassDef<>(
                    javaClass,
                    standardDefBuilder.parseKlass(javaClass)
            );
            preAddDef(def);
            afterDefInitialized(def);
            return def;
        }
        var parser = parsers.get(javaType);
        KlassDef<?> def;
        if (parser == null) {
            Utils.require(javaType2Def.get(javaType) == null);
            parser = getParser(javaType);
            def = parser.create();
            //noinspection unchecked,rawtypes
            def.setParser((KlassParser) parser);
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
        for (var entity : entities) {
            var id = entity.tryGetId();
            if (id instanceof PhysicalId) {
                var modeId = identityContext.getModelId(entity);
                stdIds.put(modeId.qualifiedName(), id);
            }
        }
        return stdIds;
    }

    @Override
    public void preAddDef(KlassDef<?> def) {
        var existing = javaType2Def.get(def.getJavaClass());
        if (existing != null && existing != def)
            throw new InternalException("Def for java type " + def.getJavaClass() + " already exists");
        javaType2Def.put(def.getJavaClass(), def);
        existing = typeDef2Def.get(def.getTypeDef());
        if (existing != null && existing != def)
            throw new InternalException("Def for type " + def.getTypeDef() + " already exists. Def: " + existing);
        typeDef2Def.put(def.getTypeDef(), def);
        tryInitDefEntityIds(def);
    }

    @Override
    public void addDef(KlassDef<?> def) {
        preAddDef(def);
        afterDefInitialized(def);
    }

    @Override
    public void afterDefInitialized(KlassDef<?> def) {
        if (def.isInitialized())
            return;
        def.setInitialized(true);
        tryInitDefEntityIds(def);
        def.getEntities().forEach(this::writeEntityIfNotPresent);
    }

    private void writeEntityIfNotPresent(Entity entity) {
        if (!entities.contains(entity))
            writeEntity(entity);
    }

    private void tryInitDefEntityIds(KlassDef<?> def) {
        def.getEntities().forEach(entity -> entity.forEachDescendant(i -> tryInitEntityId((Entity) i)));
    }

    private void tryInitEntityId(Entity entity) {
        if (entity.isDurable()) {
            var id = getEntityId(entity);
            if(id != null) {
                entityMap.put(id, entity);
                if ((entity instanceof IdInitializing idInitializing) && idInitializing.tryGetId() == null) {
                    idInitializing.initId(id);
                }
            }
        }
    }

    void writeEntity(Entity entity) {
        if (entities.add(entity)) {
            if (!(entity instanceof ValueObject)) tryInitEntityId(entity);
        } else
            throw new InternalException("Entity " + entity + " is already written to the context");
    }

    @Override
    public Collection<KlassDef<?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Map<Entity, ModelIdentity> getIdentityMap() {
        return identityContext.getIdentityMap();
    }

    @Override
    public DefContext getDefContext() {
        return this;
    }

    private void ensureAllDefsDefined() {
        new ArrayList<>(parsers.values()).forEach(p -> ensureStage(p.get().getType(), DEFINITION));
    }

    public void flush() {
        try (var ignored = getProfiler().enter("flush")) {
            ensureAllDefsDefined();
            crawNewEntities();
            recordHotTypes();
        }
    }

    private void freezeKlasses() {
        typeDef2Def.keySet().forEach(t -> {
            if(t instanceof Klass k)
                k.freeze();
        });
    }

    @Override
    public boolean isTypeDefType(ClassType type) {
        return typeDefTypes.contains(type);
    }

    @Override
    public boolean isFunctionType(ClassType type) {
        return functionTypes.contains(type);
    }

    private void recordHotTypes() {
        typeDefTypes.clear();
        functionTypes.clear();
        var typeDefKlass = getKlass(TypeDef.class);
        var functionKlass = getKlass(Function.class);
        typeDef2Def.keySet().forEach(typeDef -> {
            if (typeDef instanceof Klass klass) {
                if (typeDefKlass.isAssignableFrom(klass))
                    typeDefTypes.add(klass.getType());
                else if (functionKlass.isAssignableFrom(klass))
                    functionTypes.add(klass.getType());
            }
        });
    }

    private void crawNewEntities() {
        try (var entry = getProfiler().enter("crawNewEntities")) {
            entry.addMessage("numSeedEntities", entities.size());
            var newEntities = new HashSet<Entity>();
            for (var entity : entities) {
                entity.visitGraph(i -> {
                    if(i instanceof Entity e && !entities.contains(e)) newEntities.add(e);
                    return true;
                }, r -> true);
            }
            try (var ignored = getProfiler().enter("crawNewEntities")) {
                newEntities.forEach(this::writeEntity);
            }
        }
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Value... values) {
        return memoryIndex.selectByKey(indexDef, List.of(values));
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        return memoryIndex.selectByUniqueKey(indexDef, List.of(values));
    }

    @Override
    public boolean containsUniqueKey(IndexDef<?> indexDef, Value... values) {
        return memoryIndex.selectByUniqueKey(indexDef, List.of(values)) != null;
    }

    @Override
    public <T extends Instance> T bind(T object) {
        if (object.isEphemeral())
            throw new IllegalArgumentException("Can not bind an ephemeral entity");
        if (object instanceof  Entity entity && BINDING_ALLOWED_CLASSES.contains(EntityUtils.getRealType(object.getClass()))) {
            writeEntity(entity);
            return object;
        } else
            // Entities enter the DefContext through models def.
            throw new UnsupportedOperationException("Binding not supported.");
    }

    @Override
    public Profiler getProfiler() {
        return profiler;
    }

    @Override
    public IEntityContext createSame(long appId) {
        throw new UnsupportedOperationException();
    }

    public IdentityContext getIdentityContext() {
        return identityContext;
    }

    public Collection<Entity> entities() {
        return Collections.unmodifiableSet(entities);
    }

    public void buildMemoryIndex() {
        for (Entity entity : entities) memoryIndex.save(entity);
    }

    public void postProcess() {
        buildMemoryIndex();
        standardDefBuilder.postProcess();
        freezeKlasses();
        PrimitiveKind.initialize(this);
        StdKlass.initialize(this, false);
        StdMethod.initialize(this, false);
        StdField.initialize(this, false);
        StandardStaticMethods.initialize(this, false);
        standardDefBuilder.initUserFunctions();
        for (TypeDef typeDef : typeDef2Def.keySet()) {
            if(typeDef instanceof Klass klass)
                klass.rebuildMethodTable();
        }
    }


    // Instance context implementation

    @Override
    public List<Instance> batchGet(Collection<Id> ids) {
        return Utils.map(ids, this::get);
    }

    @Override
    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
        return Utils.filterByType(entities, entityClass);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean containsIdSelf(Id id) {
        return containsId(id);
    }

    @Override
    public <T> T getBufferedEntity(Class<T> entityType, Id id) {
        return entityType.cast(getBuffered(id));
    }

    @Override
    public <T> T getRemoved(Class<T> entityClass, Id id) {
        return null;
    }

    @Nullable
    @Override
    public EventQueue getEventQueue() {
        return null;
    }

    @Override
    public InstanceInput createInstanceInput(InputStream stream) {
        return null;
    }

    @Override
    public long getTimeout() {
        return 0;
    }

    @Override
    public void setTimeout(long timeout) {

    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void forceReindex(ClassInstance instance) {

    }

    @Override
    public Set<ClassInstance> getReindexSet() {
        return Set.of();
    }

    @Override
    public void forceSearchReindex(ClassInstance instance) {

    }

    @Override
    public Set<ClassInstance> getSearchReindexSet() {
        return Set.of();
    }

    @Override
    public long getAppId(Instance model) {
        return 1;
    }

    @Override
    public long getAppId() {
        return 1;
    }

    @Override
    public void batchRemove(Collection<Instance> instances) {
        for (var inst : instances) {
            if (inst instanceof Entity e) {
                if (entities.remove(e) && e.tryGetId() != null)
                    entityMap.remove(e.getId());
            }
        }
    }

    @Override
    public boolean remove(Instance instance) {
        batchRemove(List.of(instance));
        return true;
    }

    @Override
    public IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider) {
        return null;
    }

    @Override
    public List<Reference> selectByKey(IndexKeyRT indexKey) {
        var entities = memoryIndex.selectByKey(indexKey.getIndex().getIndexDef(),
                new ArrayList<>(indexKey.getFields().values()));
        return Utils.map(entities, Instance::getReference);
    }

    @Override
    public List<Reference> query(InstanceIndexQuery query) {
        var entities = memoryIndex.query(new EntityIndexQuery<>(
                query.index().getIndexDef(),
                Utils.safeCall(query.from(), this::convertToEntityIndexKey),
                Utils.safeCall(query.to(), this::convertToEntityIndexKey),
                query.desc(),
                query.limit() != null ? query.limit() : -1
        ));
        return Utils.map(entities, Instance::getReference);
    }

    private EntityIndexKey convertToEntityIndexKey(InstanceIndexKey key) {
        return new EntityIndexKey(new ArrayList<>(key.values()));
    }

    @Override
    public long count(InstanceIndexQuery query) {
        return memoryIndex.query(new EntityIndexQuery<>(
                query.index().getIndexDef(),
                Utils.safeCall(query.from(), this::convertToEntityIndexKey),
                Utils.safeCall(query.to(), this::convertToEntityIndexKey),
                query.desc(),
                query.limit() != null ? query.limit() : -1
        )).size();
    }

    @Override
    public boolean containsUniqueKey(IndexKeyRT key) {
        return selectFirstByKey(key) != null;
    }

    @Override
    public void batchBind(Collection<Instance> instances) {
        instances.forEach(this::bind);
    }

    @Override
    public void registerCommitCallback(Runnable action) {

    }

    @Override
    public <E> E getAttribute(ContextAttributeKey<E> key) {
        return null;
    }

    @Override
    public void initIdManually(Instance instance, Id id) {
        instance.initId(id);
    }

    @Override
    public void increaseVersionsForAll() {
        for (Instance entity : entities) {
            entity.incVersion();
        }
    }

    @Override
    public void updateMemoryIndex(ClassInstance instance) {
    }

    @Nullable
    @Override
    public Consumer<Object> getBindHook() {
        return null;
    }

    @Override
    public Instance getRemoved(Id id) {
        return null;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void finish() {
        flush();
        var set = Utils.filter(entities, e -> !e.isValue() && !e.isEphemeral() && e.tryGetId() == null);
        idInitializer.initializeIds(1L, set);
        set.forEach(i -> entityMap.put(i.getId(), i));
    }

    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        return List.of();
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return 0;
    }

    @Override
    public void initIds() {
    }

    @Override
    public void addListener(ContextListener listener) {

    }

    @Override
    public void setLockMode(LockMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockMode getLockMode() {
        return LockMode.NONE;
    }

    @Override
    public List<Id> filterAlive(List<Id> ids) {
        return Utils.filter(ids, this::isAlive);
    }

    @Override
    public boolean isAlive(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public Instance get(Id id) {
        return entityMap.get(id);
    }

    @Override
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        return query(new InstanceIndexQuery(
                from.getIndex(),
                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getFields().values())),
                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getFields().values())),
                false,
                -1L
        ));
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return count(new InstanceIndexQuery(
                from.getIndex(),
                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getFields().values())),
                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getFields().values())),
                false,
                -1L
        ));
    }

    @Override
    public List<Reference> indexSelect(IndexKeyRT key) {
        var entities = memoryIndex.selectByKey(key.getIndex().getIndexDef(), new ArrayList<>(key.getFields().values()));
        return Utils.map(entities, Instance::getReference);
    }

    @Override
    public Reference createReference(Id id) {
        return new Reference(id, () -> get(id));
    }

    @Override
    public List<Instance> batchGetRoots(List<Long> treeIds) {
        List<Id> ids = Utils.map(treeIds, treeId -> new PhysicalId(treeId, 0L));
        return batchGet(ids);
    }

    @Nullable
    @Override
    public Instance getBuffered(Id id) {
        return entityMap.get(id);
    }

    @Override
    public String getClientId() {
        return "";
    }

    @Override
    public Instance internalGet(Id id) {
        return entityMap.get(id);
    }

    @Override
    public boolean contains(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public ScanResult scan(long start, long limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadTree(long id) {

    }

    @Override
    public TypeDefProvider getTypeDefProvider() {
        return this;
    }

    @Override
    public RedirectStatusProvider getRedirectStatusProvider() {
        return this;
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instance instanceof Entity e && entities.contains(e);
    }

    @Override
    public boolean containsId(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
        return List.of();
    }

    @Override
    public List<Instance> getRelocated() {
        return List.of();
    }

    @Override
    public void buffer(Id id) {

    }

    @Override
    public void removeForwardingPointer(MvInstance instance, boolean clearingOldId) {

    }

    @Override
    public ParameterizedMap getParameterizedMap() {
        return null;
    }

    @Override
    public void setParameterizedMap(ParameterizedMap parameterizedMap) {

    }

    @Override
    public @NotNull Iterator<Instance> iterator() {
        var it = entities.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Instance next() {
                return it.next();
            }
        };
    }


}

