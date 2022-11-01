package tech.metavm.entity;

import tech.metavm.infra.IdService;
import tech.metavm.object.instance.ContextPlugin;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.meta.*;
import tech.metavm.user.RoleRT;
import tech.metavm.util.BusinessException;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static tech.metavm.object.meta.StdTypeConstants.*;

public class EntityContext {

    private boolean finished;
    private final long tenantId;
    private final SubContext<Entity> headContext = new SubContext<>();
    private final SubContext<Entity> bufferContext = new SubContext<>();
    private final StoreRegistry storeRegistry;
    private final IdService idService;
    private final Set<EntityKey> loaded = new HashSet<>();
    private final IdentitySet<Entity> loadedInstances = new IdentitySet<>();
    private final Map<EntityKey, Entity> refMap = new HashMap<>();
    private final InstanceContext instanceContext;

    public EntityContext(long tenantId,
                         StoreRegistry storeRegistry,
                         IdService idService,
                         InstanceStore instanceStore,
                         InstanceLogService instanceLogService,
                         boolean asyncProcessLogs,
                         List<ContextPlugin> plugins) {
        this.tenantId = tenantId;
        this.storeRegistry = storeRegistry;
        this.idService = idService;
        instanceContext = new InstanceContext(
                asyncProcessLogs,
                instanceStore,
                instanceLogService,
                this,
                plugins
        );
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids) {
        return batchGet(klass, ids, LoadingOption.none());
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids,
                                               LoadingOption firstOption, LoadingOption...restOptions) {
        return batchGet(klass, ids, LoadingOption.of(firstOption, restOptions));
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids, Set<LoadingOption> options) {
        Class<?> entityType = EntityUtils.getEntityType(klass);
        List<EntityKey> keys = NncUtils.map(ids, id -> new EntityKey(entityType, id));
        ensureLoaded(keys, options);
        return  (List<T>) NncUtils.map(keys, bufferContext::get);
    }

    public <T extends Entity> T get(Class<T> klass, long id) {
        return get(klass, id, LoadingOption.none());
    }

    public <T extends Entity> T get(Class<T> klass, long id, Set<LoadingOption> options) {
        return NncUtils.getFirst(batchGet(klass, List.of(id), options));
    }

    public void add(Entity entity) {
        if(entity.isPersisted()) {
            throw new RuntimeException("Can not add an already persisted entity, objectId: " + entity.getId());
        }
        bufferContext.add(entity);
    }

    public void remove(Entity entity) {
        ensureLoaded(List.of(Objects.requireNonNull(entity.key())), EnumSet.noneOf(LoadingOption.class));
        Entity trueEntity = EntityUtils.extractTrueEntity(entity);
        if(trueEntity != null) {
            bufferContext.remove(trueEntity);
        }
    }

    public <T> T getRef(Class<T> type, long id) {
        Object ref = refMap.computeIfAbsent(
                new EntityKey(type, id),
                k -> (Entity) EntityProxyFactory.getProxyInstance(type, id, this)
        );
        return (T) ref;
    }

//    public  <T extends Entity> boolean remove(Class<T> entityType, long objectId) {
//        T entity = get(entityType, objectId);
//        if(entity != null) {
//            bufferContext.remove(entity);
//            return true;
//        }
//        else {
//            return false;
//        }
//    }

    public void initIds() {
        instanceContext.initIds();
        bufferContext.initIds(getIdGenerator());
    }

    public void finish() {
        if(finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        initIds();
        ContextDifference difference = new ContextDifference();
        difference.diff(headContext.getEntities(), bufferContext.getEntities());

        for (Map.Entry<Class<?>, EntityChange> entry : difference.getChangeMap().entrySet()) {
            Class klass = entry.getKey();
            EntityChange change = entry.getValue();
            if(!change.isEmpty()) {
                EntityStore<?> store = storeRegistry.getStore(klass);
                change.apply(store);
            }
        }
        headContext.clear();
        for (Entity entity : bufferContext.getEntities()) {
            entity.setPersisted(true);
            headContext.add(EntityUtils.copyEntity(entity));
        }
        instanceContext.finish();
    }

    private Function<Integer, List<Long>> getIdGenerator() {
        return (size) -> idService.allocateIds(tenantId, size);
    }

    private void ensureLoaded(List<EntityKey> keys, Set<LoadingOption> options) {
        List<EntityKey> keysToLoad = NncUtils.filterNot(keys, this::isLoaded);
        if(NncUtils.isNotEmpty(keysToLoad)) {
            doLoad(keysToLoad, options);
        }
    }

    private void doLoad(List<EntityKey> keys, Set<LoadingOption> options) {
        loaded.addAll(keys);
        Map<Class<?>, List<Long>> idGroups = NncUtils.groupBy(keys, EntityKey::type, EntityKey::id);
        for (Map.Entry<Class<?>, List<Long>> entry : idGroups.entrySet()) {
            EntityStore<?> store = storeRegistry.getStore((Class)entry.getKey());
            store.batchGet(NncUtils.deduplicate(entry.getValue()), this, options);
        }
    }

    protected void bind(Entity entity) {
//        if(!isLoaded(entity)) {
            loadedInstances.add(entity);
            bufferContext.add(entity);
            if(entity.isPersisted()) {
                loaded.add(entity.key());
                headContext.add(EntityUtils.copyEntity(entity));
            }
//        }
    }

//    protected long allocateId() {
//        return idService.allocateIds(tenantId, 1).get(0);
//    }

    public boolean isLoaded(Entity entity) {
        return loadedInstances.contains(entity);
    }

    public boolean isLoaded(EntityKey key) {
        return loaded.contains(key);
    }

    public Type getTypeRef(long typeId) {
        return getRef(Type.class, typeId);
    }

    public Type getType(long typeId) {
        return getType(typeId, LoadingOption.none());
    }

    public Type getType(long typeId, Set<LoadingOption> options) {
        Type type = get(Type.class, typeId, options);
        NncUtils.requireNonNull(type, () -> BusinessException.typeNotFound(typeId));
        return type;
    }

    public RoleRT getRole(long roleId) {
        return get(RoleRT.class, roleId);
    }

    public RoleRT getRoleRef(long roleId) {
        return getRef(RoleRT.class, roleId);
    }

    public Type getObjectType() {
        return getType(OBJECT);
    }

    public Type getIntType() {
        return getType(INT);
    }

    public Type getLongType() {
        return getType(LONG);
    }

    public Type getDoubleType() {
        return getType(DOUBLE);
    }

    public Type getStringType() {
        return getType(STRING);
    }

    public Type getBoolType() {
        return getType(BOOL);
    }

    public Type getUserType() {
        return getType(USER.ID);
    }

    public Type getRoleType() {
        return getType(ROLE.ID);
    }

    public Type getDateType() {
        return getType(DATE);
    }

    public Type getTimeType() {
        return getType(TIME);
    }

    public Type getRawNullableType() {
        return getType(NULLABLE);
    }

    public Type getRawArrayType() {
        return getType(ARRAY);
    }

    public Type getTypeByCategory(TypeCategory category) {
        return getTypeStore().getByCategory(category, this);
    }

    public Type getTypeByName(String name) {
        return getTypeStore().getByName(name, this);
    }

    public TypeStore getTypeStore() {
        return (TypeStore) storeRegistry.getStore(Type.class);
    }

    public InstanceContext getInstanceContext() {
        return instanceContext;
    }

    public long getTenantId() {
        return tenantId;
    }

    public Type getParameterizedType(Type rawType, List<Type> typeArguments) {
        return getTypeStore().getParameterizedType(rawType, typeArguments, this);
    }

    public Field getField(long id) {
        return get(Field.class, id);
    }

    public Field getFieldRef(long id) {
        return getRef(Field.class, id);
    }

    public <T extends Entity> EntityStore<T> getStore(Class<T> type) {
        return storeRegistry.getStore(type);
    }

    public IdService getIdService() {
        return idService;
    }

}
