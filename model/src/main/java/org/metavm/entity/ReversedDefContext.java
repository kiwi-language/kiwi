//package org.metavm.entity;
//
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.metavm.entity.natives.StandardStaticMethods;
//import org.metavm.entity.natives.StdFunction;
//import org.metavm.flow.Function;
//import org.metavm.object.instance.ColumnKind;
//import org.metavm.object.instance.IndexKeyRT;
//import org.metavm.object.instance.core.*;
//import org.metavm.object.type.*;
//import org.metavm.util.*;
//
//import javax.annotation.Nullable;
//import java.io.InputStream;
//import java.lang.reflect.Type;
//import java.lang.reflect.TypeVariable;
//import java.util.*;
//import java.util.function.Consumer;
//
//@Slf4j
//public class ReversedDefContext extends DefContext {
//
//    private DefContext defContext;
//    private final Map<TypeDef, KlassDef<?>> typeDef2Def = new HashMap<>();
//    private final Map<java.lang.reflect.Type, KlassDef<?>> javaType2Def = new HashMap<>();
//    private final Map<Integer, KlassDef<?>> typeTag2Def = new HashMap<>();
//    private State state = State.PRE_INITIALIZING;
//    private final List<Klass> extraKlasses = new ArrayList<>();
//    private final List<Column> columns = new ArrayList<>();
//    private final List<Function> stdFunctions = new ArrayList<>();
//    private final Map<Object, Id> entity2Id = new IdentityHashMap<>();
//    private WAL wal;
//
//    private enum State {
//        PRE_INITIALIZING,
//        INITIALIZING,
//        POST_INITIALIZING
//    }
//
//    public ReversedDefContext(WAL wal, DefContext defContext) {
//        this.defContext = defContext;
//    }
//
//    @Override
//    public DefContext getDefContext() {
//        return defContext;
//    }
//
//    @Override
//    public KlassDef<?> tryGetDef(TypeDef typeDef) {
//        return typeDef2Def.get(typeDef);
//    }
//
//    private void loadExtraKlasses(List<String> ids) {
//        for (String id : ids) {
//            var klass = getKlass(id);
//            EntityUtils.ensureTreeInitialized(klass);
//            extraKlasses.add(klass);
//        }
//    }
//
//    @Override
//    public KlassDef<?> getDef(Type javaType, ResolutionStage stage) {
//        checkJavaType(javaType);
//        javaType = ReflectionUtils.getBoxedType(javaType);
//        if (!(javaType instanceof TypeVariable<?>)) {
//            javaType = EntityUtils.getEntityType(javaType);
//            if (javaType instanceof Class<?> klass) {
//                if (ReflectionUtils.isPrimitiveWrapper(klass))
//                    javaType = BOX_CLASS_MAP.getOrDefault(klass, klass);
//                else
//                    javaType = EntityUtils.getRealType(klass);
//            }
//        }
//        var javaTypeF = javaType;
//        return Objects.requireNonNull(javaType2Def.get(javaType), () -> "Cannot find def for type: " + javaTypeF);
//    }
//
//    @Override
//    public Collection<KlassDef<?>> getAllDefList() {
//        return typeDef2Def.values();
//    }
//
//    @Override
//    public boolean containsDef(TypeDef typeDef) {
//        return typeDef2Def.containsKey(typeDef);
//    }
//
//    @Override
//    public IEntityContext createSame(long appId) {
//        return new ReversedDefContext(defContext);
//    }
//
//    public void initializeFrom(DefContext sysDefContext, List<String> extraKlassIds) {
//        state = State.INITIALIZING;
//        loadStdFunctions();
//        loadExtraKlasses(extraKlassIds);
//        sysDefContext.getAllDefList().forEach(this::initDef);
//        defContext = this;
//        state = State.POST_INITIALIZING;
//        recordEntityIds();
//        postInitialization();
//        postProcess();
//    }
//
//    public void initEnv() {
//        StdFunction.initializeFromDefContext(this, true);
//        StdKlass.initialize(this, true);
//        StdMethod.initialize(this, true);
//        StdField.initialize(this, true);
//        StandardStaticMethods.initialize(this, true);
//    }
//
//    private void freezeKlasses() {
//        typeDef2Def.keySet().forEach(t -> {
//            if(t instanceof Klass k)
//                k.freeze();
//        });
//    }
//
//    private void postProcess() {
//        freezeKlasses();
//        initEnv();
//        for (TypeDef typeDef : typeDef2Def.keySet()) {
//            if(typeDef instanceof Klass klass)
//                klass.rebuildMethodTable();
//        }
//    }
//
//    private void loadStdFunctions() {
//        for (StdFunction stdFunc : StdFunction.values()) {
//            var func = getFunction(stdFunc.get().getId());
//            EntityUtils.ensureTreeInitialized(func);
//            stdFunctions.add(func);
//        }
//    }
//
//    private void recordEntityIds() {
//        for (Object entity : entities()) {
//            var inst = entity;
//            if(inst.tryGetId() != null)
//                entity2Id.put(entity, inst.getId());
//        }
//    }
//
//    public void postInitialization() {
//        replaceInstanceContext();
//        addMappings();
//    }
//
//    private void replaceInstanceContext() {
//        var instCtx = getInstanceContext().createSame(getAppId(), this);
//        setInstanceContext(instCtx);
//        instCtx.addListener(this);
//    }
//
//    private void addMappings() {
//        var instCtx = getInstanceContext();
//        var entities = new ArrayList<>();
//        forEachDef(def -> {
//            if((def.getTypeDef() instanceof Klass k && (k.isInner() || k.isLocal())))
//                return;
//            entities.add(def.getTypeDef());
//        });
//        entities.addAll(stdFunctions);
//        entities.addAll(extraKlasses);
//        entities.addAll(columns);
//        for (var entity : entities) {
//            EntityUtils.forEachDescendant(entity, e -> {
//                var id = entity2Id.get(e);
//                if(id != null) {
//                    var inst = instCtx.get(id);
//                    addMapping(e, inst);
//                }
//            });
//        }
//    }
//
//    private void forEachDef(Consumer<KlassDef<?>> action) {
//        javaType2Def.values().forEach(action);
//    }
//
//    private void initDef(KlassDef<?> prototype) {
//        var typeDef = (TypeDef) getTypeDef(prototype.getTypeDef().getId());
//        EntityUtils.ensureTreeInitialized(typeDef);
//        var def = createDef(prototype);
//        javaType2Def.put(prototype.getJavaClass(), def);
//        typeDef2Def.put(typeDef, def);
//        if(typeDef instanceof Klass klass && TypeTags.isSystemTypeTag((int) klass.getTag()))
//            typeTag2Def.put((int) klass.getTag(), def);
//    }
//
//    private KlassDef<?> createDef(KlassDef<?> prototype) {
//        var existing = javaType2Def.get(prototype.getJavaClass());
//        if(existing != null)
//            return existing;
//        return createKlassDef(prototype);
//   }
//
//    private <T> KlassDef<T> createKlassDef(KlassDef<T> prototype) {
//        var klass = getKlass(prototype.getKlass().getId());
//       var def = new KlassDef<>(prototype.getJavaClass(), klass);
//       initPojoDef(def);
//       return def;
//    }
//
//    private void initPojoDef(KlassDef<?> klassDef) {
//        javaType2Def.put(klassDef.getJavaClass(), klassDef);
//    }
//
//    @Nullable
//    @Override
//    public KlassDef<?> getDefIfPresent(Type javaType) {
//        return javaType2Def.get(javaType);
//    }
//
//    @Override
//    public void close() {
//        super.close();
//        SystemConfig.clearLocal();
//    }
//
//    public WAL getWal() {
//        return wal;
//    }
//
//
//    // Instance context implementation
//
//    @Override
//    public List<Instance> batchGet(Collection<Id> ids) {
//        return NncUtils.map(ids, this::get);
//    }
//
//    @Override
//    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
//        return NncUtils.filterByType(entities, entityClass);
//    }
//
//    @Override
//    public void close() {
//
//    }
//
//    @Override
//    public boolean containsIdSelf(Id id) {
//        return containsId(id);
//    }
//
//    @Override
//    public <T> T getBufferedEntity(Class<T> entityType, Id id) {
//        return entityType.cast(getBuffered(id));
//    }
//
//    @Nullable
//    @Override
//    public IEntityContext getParent() {
//        return null;
//    }
//
//    @Override
//    public boolean isNewEntity(Object entity) {
//        return ((Instance) entity).isNew();
//    }
//
//    @Override
//    public <T> T getRemoved(Class<T> entityClass, Id id) {
//        return null;
//    }
//
//    @Override
//    public boolean isPersisted(Object entity) {
//        return isNewEntity(entity);
//    }
//
//    @Nullable
//    @Override
//    public EventQueue getEventQueue() {
//        return null;
//    }
//
//    @Override
//    public InstanceInput createInstanceInput(InputStream stream) {
//        return null;
//    }
//
//    @Override
//    public long getTimeout() {
//        return 0;
//    }
//
//    @Override
//    public void setTimeout(long timeout) {
//
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public void setDescription(String description) {
//
//    }
//
//    @Override
//    public void forceReindex(ClassInstance instance) {
//
//    }
//
//    @Override
//    public Set<ClassInstance> getReindexSet() {
//        return Set.of();
//    }
//
//    @Override
//    public void forceSearchReindex(ClassInstance instance) {
//
//    }
//
//    @Override
//    public Set<ClassInstance> getSearchReindexSet() {
//        return Set.of();
//    }
//
//    @Override
//    public long getAppId(Object model) {
//        return 1;
//    }
//
//    @Override
//    public long getAppId() {
//        return 1;
//    }
//
//    @Override
//    public void batchRemove(Collection<Instance> instances) {
//        for (var inst : instances) {
//            if (entities.remove(inst) && inst.tryGetId() != null)
//                entityMap.remove(inst.getId());
//        }
//    }
//
//    @Override
//    public boolean remove(Instance instance) {
//        batchRemove(List.of(instance));
//        return true;
//    }
//
//    @Override
//    public IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider) {
//        return null;
//    }
//
//    @Override
//    public List<Reference> selectByKey(IndexKeyRT indexKey) {
//        var entities = memoryIndex.selectByKey(indexKey.getIndex().getIndexDef(),
//                new ArrayList<>(indexKey.getFields().values()));
//        return NncUtils.map(entities, e -> ((Entity) e).getReference());
//    }
//
//    @Override
//    public List<Reference> query(InstanceIndexQuery query) {
//        var entities = memoryIndex.query(new EntityIndexQuery<>(
//                query.index().getIndexDef(),
//                NncUtils.get(query.from(), this::convertToEntityIndexKey),
//                NncUtils.get(query.to(), this::convertToEntityIndexKey),
//                query.desc(),
//                query.limit() != null ? query.limit() : -1
//        ));
//        return NncUtils.map(entities, e -> ((Entity) e).getReference());
//    }
//
//    private EntityIndexKey convertToEntityIndexKey(InstanceIndexKey key) {
//        return new EntityIndexKey(new ArrayList<>(key.values()));
//    }
//
//    @Override
//    public long count(InstanceIndexQuery query) {
//        return memoryIndex.query(new EntityIndexQuery<>(
//                query.index().getIndexDef(),
//                NncUtils.get(query.from(), this::convertToEntityIndexKey),
//                NncUtils.get(query.to(), this::convertToEntityIndexKey),
//                query.desc(),
//                query.limit() != null ? query.limit() : -1
//        )).size();
//    }
//
//    @Override
//    public boolean containsUniqueKey(IndexKeyRT key) {
//        return selectFirstByKey(key) != null;
//    }
//
//    @Override
//    public void batchBind(Collection<Instance> instances) {
//        instances.forEach(this::bind);
//    }
//
//    @Override
//    public void registerCommitCallback(Runnable action) {
//
//    }
//
//    @Override
//    public <E> E getAttribute(ContextAttributeKey<E> key) {
//        return null;
//    }
//
//    @Override
//    public void initIdManually(Instance instance, Id id) {
//        instance.initId(id);
//    }
//
//    @Override
//    public void increaseVersionsForAll() {
//        for (Instance entity : entities) {
//            entity.incVersion();
//        }
//    }
//
//    @Override
//    public void updateMemoryIndex(ClassInstance instance) {
//    }
//
//    @Nullable
//    @Override
//    public Consumer<Object> getBindHook() {
//        return null;
//    }
//
//    @Override
//    public Instance getRemoved(Id id) {
//        return null;
//    }
//
//    @Override
//    public boolean isFinished() {
//        return false;
//    }
//
//    @Override
//    public void finish() {
//
//    }
//
//    @Override
//    public IInstanceContext getInstanceContext() {
//        return null;
//    }
//
//    @Override
//    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
//        return List.of();
//    }
//
//    @Override
//    public long count(EntityIndexQuery<?> query) {
//        return 0;
//    }
//
//    @Override
//    public void flushAndWriteInstances() {
//
//    }
//
//    @Override
//    public void initIds() {
//
//    }
//
//    @Override
//    public void addListener(ContextListener listener) {
//
//    }
//
//    @Override
//    public void setLockMode(LockMode mode) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public LockMode getLockMode() {
//        return LockMode.NONE;
//    }
//
//    @Override
//    public List<Id> filterAlive(List<Id> ids) {
//        return NncUtils.filter(ids, this::isAlive);
//    }
//
//    @Override
//    public boolean isAlive(Id id) {
//        return entityMap.containsKey(id);
//    }
//
//    @Override
//    public Instance get(Id id) {
//        return entityMap.get(id);
//    }
//
//    @Override
//    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
//        return query(new InstanceIndexQuery(
//                from.getIndex(),
//                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getFields().values())),
//                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getFields().values())),
//                false,
//                -1L
//        ));
//    }
//
//    @Override
//    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
//        return count(new InstanceIndexQuery(
//                from.getIndex(),
//                new InstanceIndexKey(from.getIndex(), new ArrayList<>(from.getFields().values())),
//                new InstanceIndexKey(to.getIndex(), new ArrayList<>(to.getFields().values())),
//                false,
//                -1L
//        ));
//    }
//
//    @Override
//    public List<Reference> indexSelect(IndexKeyRT key) {
//        var entities = memoryIndex.selectByKey(key.getIndex().getIndexDef(), new ArrayList<>(key.getFields().values()));
//        return NncUtils.map(entities, Instance::getReference);
//    }
//
//    @Override
//    public Reference createReference(Id id) {
//        return new Reference(id, () -> get(id));
//    }
//
//    @Override
//    public List<Instance> batchGetRoots(List<Long> treeIds) {
//        List<Id> ids = NncUtils.map(treeIds, treeId -> new PhysicalId(false, treeId, 0L));
//        return batchGet(ids);
//    }
//
//    @Nullable
//    @Override
//    public Instance getBuffered(Id id) {
//        return entityMap.get(id);
//    }
//
//    @Override
//    public String getClientId() {
//        return "";
//    }
//
//    @Override
//    public Instance internalGet(Id id) {
//        return entityMap.get(id);
//    }
//
//    @Override
//    public boolean contains(Id id) {
//        return entityMap.containsKey(id);
//    }
//
//    @Override
//    public ScanResult scan(long start, long limit) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void loadTree(long id) {
//
//    }
//
//    @Override
//    public TypeDefProvider getTypeDefProvider() {
//        return this;
//    }
//
//    @Override
//    public RedirectStatusProvider getRedirectStatusProvider() {
//        return this;
//    }
//
//    @Override
//    public boolean containsInstance(Instance instance) {
//        return entities.contains(instance);
//    }
//
//    @Override
//    public boolean containsId(Id id) {
//        return entityMap.containsKey(id);
//    }
//
//    @Override
//    public List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
//        return List.of();
//    }
//
//    @Override
//    public List<Instance> getRelocated() {
//        return List.of();
//    }
//
//    @Override
//    public void buffer(Id id) {
//
//    }
//
//    @Override
//    public void removeForwardingPointer(MvInstance instance, boolean clearingOldId) {
//
//    }
//
//    @Override
//    public ParameterizedMap getParameterizedMap() {
//        return null;
//    }
//
//    @Override
//    public void setParameterizedMap(ParameterizedMap parameterizedMap) {
//
//    }
//
//    @Override
//    public @NotNull Iterator<Instance> iterator() {
//        var it = entities.iterator();
//        return new Iterator<>() {
//            @Override
//            public boolean hasNext() {
//                return it.hasNext();
//            }
//
//            @Override
//            public Instance next() {
//                return it.next();
//            }
//        };
//    }
//
//
//}
