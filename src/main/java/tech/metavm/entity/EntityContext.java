package tech.metavm.entity;

import tech.metavm.infra.IdService;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.TypeStore;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;

public class EntityContext {

    public final long tenantId;
    private final SubContext headContext = new SubContext();
    private final SubContext bufferContext = new SubContext();
    private final StoreRegistry storeRegistry;
    private final IdService idService;
    private final Set<EntityKey> loaded = new HashSet<>();
    private final IdentitySet<Entity> loadedInstances = new IdentitySet<>();
    private final Map<EntityKey, Entity> refMap = new HashMap<>();

    public EntityContext(long tenantId, StoreRegistry storeRegistry, IdService idService) {
        this.tenantId = tenantId;
        this.storeRegistry = storeRegistry;
        this.idService = idService;
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids) {
        return batchGet(klass, ids, LoadingOption.none());
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids,
                                               LoadingOption firstOption, LoadingOption...restOptions) {
        return batchGet(klass, ids, LoadingOption.of(firstOption, restOptions));
    }

    public <T extends Entity> List<T> batchGet(Class<T> klass, Collection<Long> ids, EnumSet<LoadingOption> options) {
        Class<?> entityType = EntityUtils.getEntityType(klass);
        List<EntityKey> keys = NncUtils.map(ids, id -> new EntityKey(entityType, id));
        ensureLoaded(keys, options);
        return  (List<T>) NncUtils.map(keys, bufferContext::get);
    }

    public <T extends Entity> T get(Class<T> klass, long id) {
        return NncUtils.first(batchGet(klass, List.of(id)));
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

    public void sync() {
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
    }

    private void initIds() {
        List<Entity> newEntities = NncUtils.filter(bufferContext.entities(), Entity::isIdNull);
        if(NncUtils.isEmpty(newEntities)) {
            return;
        }
        List<Long> ids = idService.allocateIds(tenantId, newEntities.size());
        NncUtils.biForEach(newEntities, ids, Entity::initId);
    }

    private void ensureLoaded(List<EntityKey> keys, EnumSet<LoadingOption> options) {
        List<EntityKey> keysToLoad = NncUtils.filterNot(keys, this::isLoaded);
        if(NncUtils.isNotEmpty(keysToLoad)) {
            doLoad(keysToLoad, options);
        }
    }

    private void doLoad(List<EntityKey> keys, EnumSet<LoadingOption> options) {
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

    public Type getType(long typeId) {
        return getRef(Type.class, typeId);
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

    public long getTenantId() {
        return tenantId;
    }

    public Type resolveType(FieldDTO fieldDTO) {
        Type baseType;
        TypeCategory type = TypeCategory.getByCodeRequired(fieldDTO.type());
        if(type.isPrimitive()) {
            baseType = getTypeByCategory(TypeCategory.getByCodeRequired(fieldDTO.type()));
        }
        else {
            NncUtils.require(fieldDTO.targetId());
            baseType = getType(fieldDTO.targetId());
        }
        if(fieldDTO.multiValued()) {
            return getArrayType(baseType);
        }
        else if(fieldDTO.nullable()) {
            return getNullableType(baseType);
        }
        else {
            return baseType;
        }
    }


//    public Type resolveType(Type baseType, boolean nullable, boolean isArray) {
//        if(nullable) {
//            return getOrCreateArrayType(baseType);
//        }
//        else if(isArray) {
//            return getOrCreateArrayType(baseType);
//        }
//        else {
//            return baseType;
//        }
//    }

    public Type getNullableType(Type baseType) {
        Type nullableType = getTypeStore().getNullableType(baseType, this);
        return Objects.requireNonNullElseGet(nullableType, () -> createNullableType(baseType));
    }

    private Type createNullableType(Type baseType) {
        Type nullableType = new Type(
                baseType.getName() + "?",
                TypeCategory.NULLABLE,
                false,
                baseType.isEphemeral(),
                baseType,
                "可空类型",
                this
        );
        add(nullableType);
        return nullableType;
    }

    public Type getArrayType(Type baseType) {
        Type arrayType = getTypeStore().getArrayType(baseType, this);
        return Objects.requireNonNullElseGet(arrayType, () -> createArrayType(baseType));
    }

    private Type createArrayType(Type baseType) {
        Type arrayType = new Type(
                baseType.getName() + "[]",
                TypeCategory.ARRAY,
                false,
                baseType.isEphemeral(),
                baseType,
                "数组",
                this
        );
        add(arrayType);
        return arrayType;
    }

    public Field getField(long fieldId) {
        return getRef(Field.class, fieldId);
    }

    public <T extends Entity> EntityStore<T> getStore(Class<T> type) {
        return storeRegistry.getStore(type);
    }

    public void loadFieldsAndOptions(List<Type> types) {
        ((TypeStore) getStore(Type.class)).loadFields(types, this);
    }

    private static class SubContext {
        private final IdentityHashMap<Entity, Entity> entities = new IdentityHashMap<>();
        private final Map<EntityKey, Entity> entityMap = new HashMap<>();

        Entity get(EntityKey key) {
            return entityMap.get(key);
        }

        void add(Entity entity) {
            Objects.requireNonNull(entity);
            if(entity.key() != null) {
                Entity existing = entityMap.remove(entity.key());
                if(existing != null) {
                    entities.remove(existing);
                }
                entityMap.put(entity.key(), entity);
            }
            entities.put(entity, entity);
        }

        public Collection<Entity> entities() {
            return entities.values();
        }

        void clear() {
            entities.clear();
            entityMap.clear();
        }

        boolean remove(Entity entity) {
            Entity removed = entities.remove(entity);
            if(removed != null) {
                if(removed.key() != null) {
                    entityMap.remove(removed.key());
                }
                return true;
            }
            else {
                return false;
            }
        }

//        boolean remove(long objectId) {
//            Entity entity = entityMap.get(objectId);
//            if(entity != null) {
//                entities.remove(entity);
//                return true;
//            }
//            else {
//                return false;
//            }
//        }

        List<Entity> getEntities() {
            return new ArrayList<>(entities.values());
        }

    }

}
