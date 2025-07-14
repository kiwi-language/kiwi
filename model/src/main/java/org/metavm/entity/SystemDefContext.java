package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ValueObject;
import org.metavm.event.EventQueue;
import org.metavm.flow.Function;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class SystemDefContext extends DefContext implements DefMap, IInstanceContext, TypeRegistry {

    public static final Set<Class<? extends GlobalKey>> BINDING_ALLOWED_CLASSES = Set.of();

    private final Map<Type, KlassDef<?>> javaType2Def = new HashMap<>();
    private final Map<TypeDef, KlassDef<?>> typeDef2Def = new IdentityHashMap<>();
    private final StdIdProvider stdIdProvider;
    private final Set<Entity> entities = new IdentitySet<>();
    private final IdentityContext identityContext;
    private final TypeTagStore typeTagStore;
    private final EntityMemoryIndex memoryIndex = new EntityMemoryIndex();
    private final Map<Id, Entity> entityMap = new HashMap<>();
    private final Set<java.lang.reflect.Field> fieldBlacklist = new HashSet<>();
    private final Set<ClassType> typeDefTypes = new HashSet<>();
    private final Set<ClassType> functionTypes = new HashSet<>();
    private final StandardDefBuilder standardDefBuilder;
    private final Profiler profiler = new Profiler();

    public SystemDefContext(StdAllocators stdAllocators, TypeTagStore typeTagStore) {
        this(stdAllocators, typeTagStore, new IdentityContext());
    }

    public SystemDefContext(StdAllocators allocators, TypeTagStore typeTagStore, IdentityContext identityContext) {
        this.stdIdProvider = new StdIdProvider(allocators);
        this.identityContext = identityContext;
        this.typeTagStore = typeTagStore;
        standardDefBuilder = new StandardDefBuilder(this);
        standardDefBuilder.initRootTypes();
    }

    @Override
    public KlassDef<?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public KlassDef<?> getDef(Type javaType) {
        checkJavaType(javaType);
        var existing = javaType2Def.get(javaType);
        if (existing != null && existing.getParser() == null)
            return existing;
        return parseType(javaType);
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

    @Override
    public Id getModelId(Object o) {
        var identity = o instanceof ModelIdentity i ? i : ModelIdentities.getIdentity(o);
        var id = stdIdProvider.getId(identity);
//        if (DebugEnv.flag) {
            if (id == null)
                throw new NullPointerException("Failed to get id for model: " + o);
//        }
        return id;
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

    private @NotNull KlassDef<?> parseType(Type javaType) {
        var javaClass = ReflectionUtils.getRawClass(javaType);
        var parser = new KlassParser<>(javaClass, this, this::getModelId);
        var def = parser.parse();
        afterDefInitialized(def);
        return def;
    }

    @Override
    public boolean containsJavaType(Type javaType) {
        return javaType2Def.containsKey(javaType);
    }

    @Override
    public void preAddDef(KlassDef<?> def) {
        var tracing = DebugEnv.traceClassDefinition;
        if (tracing) log.trace("Adding class def: {}", def.getJavaClass().getName());
        var existing = javaType2Def.get(def.getJavaClass());
        if (existing != null && existing != def)
            throw new InternalException("Def for java type " + def.getJavaClass() + " already exists");
        javaType2Def.put(def.getJavaClass(), def);
        existing = typeDef2Def.get(def.getTypeDef());
        if (existing != null && existing != def)
            throw new InternalException("Def for type " + def.getTypeDef() + " already exists. Def: " + existing);
        typeDef2Def.put(def.getTypeDef(), def);
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
        def.getEntities().forEach(this::writeEntityIfNotPresent);
    }

    private void writeEntityIfNotPresent(Entity entity) {
        if (!entities.contains(entity))
            writeEntity(entity);
    }

    void writeEntity(Entity entity) {
        if (entities.add(entity)) {
            entity.setContext(this);
            if (entity.tryGetId() != null)
                entityMap.put(entity.getId(), entity);
        }
        else
            throw new InternalException("Entity " + entity + " is already written to the context");
    }

    @Override
    public Collection<KlassDef<?>> getAllDefList() {
        return javaType2Def.values();
    }

    public Map<Entity, ModelIdentity> getIdentityMap() {
        return identityContext.getIdentityMap();
    }

    public void flush() {
        try (var ignored = getProfiler().enter("flush")) {
            crawNewEntities();
            recordHotTypes();
            setupIdentityContext();
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

    private void setupIdentityContext() {
        for (Entity entity : entities) {
            if (!(entity instanceof ValueObject || entity.isEphemeral())) {
                identityContext.getModelId(entity);
            }
        }
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
            var visited = new IdentitySet<Instance>();
            for (var entity : entities) {
                entity.visitGraph(i -> {
                    if(i instanceof Entity e && !entities.contains(e)) {
                        newEntities.add(e);
                    }
                    return true;
                }, r -> true, visited);
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
        return memoryIndex.selectFirstByKey(indexDef, List.of(values));
    }

    @Override
    public boolean containsUniqueKey(IndexDef<?> indexDef, Value... values) {
        return memoryIndex.selectFirstByKey(indexDef, List.of(values)) != null;
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
    public IInstanceContext createSame(long appId) {
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
        log.info("Post processing system def context");
        standardDefBuilder.initUserFunctions();
        buildMemoryIndex();
        Klasses.loadKlasses(this);
        freezeKlasses();
        StdKlass.initialize(this, false);
        StdMethod.initialize(this, false);
        StdField.initialize(this, false);
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
                new ArrayList<>(indexKey.getValues()));
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
    public void increaseVersionsForAll() {
        for (Instance entity : entities) {
            entity.incVersion();
        }
    }

    @Override
    public void updateMemoryIndex(ClassInstance instance) {
    }

    @Override
    public long allocateTreeId() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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

    @org.jetbrains.annotations.Nullable
    @Override
    public Reference selectFirstByKey(IndexKeyRT key) {
        return Utils.first(selectByKey(key));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Reference selectLastByKey(IndexKeyRT key) {
        return Utils.last(selectByKey(key));
    }

    @Override
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        return query(new InstanceIndexQuery(
                from.getIndex(),
                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getValues())),
                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getValues())),
                false,
                -1L
        ));
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return count(new InstanceIndexQuery(
                from.getIndex(),
                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getValues())),
                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getValues())),
                false,
                -1L
        ));
    }

    @Override
    public List<Reference> indexSelect(IndexKeyRT key) {
        var entities = memoryIndex.selectByKey(key.getIndex().getIndexDef(), new ArrayList<>(key.getValues()));
        return Utils.map(entities, Instance::getReference);
    }

    @Override
    public Reference createReference(Id id) {
        return new EntityReference(id, () -> get(id));
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
    public void buffer(Id id) {

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

