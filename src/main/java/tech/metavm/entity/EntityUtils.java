package tech.metavm.entity;

import javassist.util.proxy.ProxyObject;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EntityUtils {

    public static final long MAXIMUM_DIFF_DEPTH = 1000;

    public static final Set<Class<?>> PRIM_CLASSES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, String.class, Date.class, LocalDateTime.class,
            BigInteger.class, BigDecimal.class, Class.class, Object.class
    );

    public static final Set<Class<?>> ENTITY_CLASSES = Set.of(
        ClassType.class, tech.metavm.object.meta.Field.class, Instance.class,
            EnumConstantRT.class
    );

    public static final Set<Class<?>> CONTEXT_CLASSES = Set.of(
            InstanceContext.class
    );

    public static void clearIdRecursively(Object model) {
        traverseModelGraph(model, (path, o) -> {
            if(o instanceof IdInitializing idInitializing) {
                idInitializing.clearId();
            }
        });
    }

    public static void traverseModelGraph(Object model, BiConsumer<List<String>, Object> action) {
        traverseModelGraph0(model, action, new LinkedList<>(), new IdentitySet<>());
    }

    private static void traverseModelGraph0(Object model,
                                            BiConsumer<List<String>, Object> action,
                                            LinkedList<String> path,
                                            IdentitySet<Object> visited) {
        if(model == null || visited.contains(model)
                || isPrimitive(model.getClass())) {
            return;
        }
        visited.add(model);
        action.accept(path, model);
        Class<?> realClass = getRealType(model.getClass());
        EntityDesc desc = DescStore.get(realClass);
        for (EntityProp prop : desc.getProps()) {
            if(prop.isAccessible() && !prop.isTransient()) {
                path.addLast(prop.getName());
                traverseModelGraph0(prop.get(model), action, path, visited);
                path.removeLast();
            }
        }
    }

    public static <T extends Entity> boolean entityEquals(T entity1, T entity2) {
        if(entity1 == entity2) {
            return true;
        }
        if(entity1 == null || entity2 == null) {
            return false;
        }
        if(entity1.getId() != null && entity2.getId() != null) {
            return entity1.getId().equals(entity2.getId());
        }
        return false;
    }

    private static boolean isDifferent(Object value1, Object value2, Map<Object, Object> visited) {
        if(value1 == null && value2 == null) {
            return false;
        }
        else if(value1 == null || value2 == null) {
            return true;
        }
        else if(value1 instanceof Entity entity1) {
            if(value2 instanceof Entity entity2) {
                return isEntityDifferent(entity1, entity2, visited);
            }
            else {
                return true;
            }
        }
        else if(value1 instanceof Map<?,?> map1) {
            if((value2 instanceof Map<?,?> map2)) {
                return isMapDifferent(map1, map2, visited);
            }
            else {
                return true;
            }
        }
        else if(value1 instanceof Collection<?> coll1) {
            if(value2 instanceof Collection<?> coll2) {
                return isCollectionDifferent(coll1, coll2, visited);
            }
            else {
                return true;
            }
        }
        else if(isPojo(value1.getClass())) {
            return isPojoDifferent(value1, value2, visited);
        }
        else {
            return !Objects.equals(value1, value2);
        }
    }

    public static boolean pojoEquals(Object pojo1, Object pojo2) {
        return !isPojoDifferent(pojo1, pojo2);
    }

    public static boolean isPojoDifferent(Object pojo1, Object pojo2) {
        return isPojoDifferent(pojo1, pojo2, new HashMap<>());
    }

    private static boolean isPojoDifferent(Object pojo1, Object pojo2, Map<Object, Object> visited) {
        if(visited.size() > MAXIMUM_DIFF_DEPTH) {
            throw new InternalException("Diff depth exceeds maximum depth " + MAXIMUM_DIFF_DEPTH);
        }
        DiffPair diffPair = new DiffPair(pojo1, pojo2);
        if(visited.containsKey(diffPair)) {
            return false;
//            DiffState state = (DiffState) visited.get(diffPair);
//            if(state == DiffState.DONE) {
//                return false;
//            }
//            throw new RuntimeException("Back reference of POJO is currently not supported");
        }
        if(pojo1 == null && pojo2 == null) {
            return true;
        }
        if(pojo1 == null || pojo2 == null) {
            return false;
        }
        Class<?> realClass = getRealClass(pojo1.getClass(), pojo2.getClass());
        if(realClass == null) {
            return true;
        }

        visited.put(diffPair, DiffState.DOING);
//        visited.put(pojo2, pojo1);
        ensureProxyInitialized(pojo1);
        ensureProxyInitialized(pojo2);
//        Class<?> klass = pojo1.getClass();
        EntityDesc desc = DescStore.get(realClass);
        for (EntityProp prop : desc.getProps()) {
            if(prop.isTransient()) {
                continue;
            }
            Object value1 = prop.get(pojo1), value2 = prop.get(pojo2);
            if(isDifferent(value1, value2, visited)) {
                return true;
            }
        }
        visited.put(diffPair, DiffState.DONE);
        return false;
    }

    public static void ensureProxyInitialized(Object object) {
        if(object instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = (EntityMethodHandler<?>) proxyObject.getHandler();
            handler.ensureInitialized(object);
        }
    }

    public static boolean isModelInitialized(Object object) {
        if(object instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = (EntityMethodHandler<?>) proxyObject.getHandler();
            return handler.isInitialized();
        }
        else{
            return true;
        }
    }

    private static Class<?> getRealClass(Class<?> klass1, Class<?> klass2) {
        if(klass1 == klass2) {
            return klass1;
        }
        if(klass1 == klass2.getSuperclass()) {
            if(ProxyObject.class.isAssignableFrom(klass2)) {
                return klass1;
            }
        }
        if(klass2 == klass1.getSuperclass()) {
            if(ProxyObject.class.isAssignableFrom(klass1)) {
                return klass2;
            }
        }
        return null;
    }

    private static boolean isEntityDifferent(Entity entity1, Entity entity2, Map<Object, Object> visited) {
        if(entity1.getId() != null && entity2.getId() != null) {
            return !Objects.equals(entity1.key(), entity2.key());
        }
        if(entity1.getId() != null || entity2.getId() != null) {
            return false;
        }
        else {
            return isPojoDifferent(entity1, entity2, visited);
        }
    }

    private static boolean isMapDifferent(Map<?,?> map1, Map<?,?> map2, Map<Object,Object> visited) {
        if(map1.size() != map2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = NncUtils.buildPairsForMap(map1, map2);
        for (Pair<Object> pair : pairs) {
            if(isDifferent(pair.first(), pair.second(), visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCollectionDifferent(Collection<?> coll1, Collection<?> coll2,
                                                 Map<Object,Object> visited) {
        if(coll1.size() != coll2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = NncUtils.buildPairs(coll1, coll2);
        for (Pair<Object> pair : pairs) {
            if(isDifferent(pair.first(), pair.second(), visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPojo(Class<?> klass) {
        return !Entity.class.isAssignableFrom(klass)
                && !Map.class.isAssignableFrom(klass) && !Collection.class.isAssignableFrom(klass)
                && !isShallow(klass);
    }

    public static boolean isPrimitive(Class<?> klass) {
        return PRIM_CLASSES.contains(klass);
    }

    private static boolean isEnum(Class<?> klass) {
        return Enum.class.isAssignableFrom(klass);
    }

    public static Entity copyEntity(Entity entity) {
        return (Entity) copyPojo(entity, new IdentityHashMap<>(), true);
    }

    public static Long tryGetId(Object object) {
        if(object instanceof Identifiable identifiable) {
            return identifiable.getId();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T copyPojo(T pojo) {
        return (T) copyPojo(pojo, new IdentityHashMap<>(), true);
    }

    public static Value copyValue(Value value) {
        return (Value) copyPojo(value, new IdentityHashMap<>(), true);
    }

    @SuppressWarnings("unused")
    public static List<Entity> getAllEntities(Entity entity) {
        List<Entity> allEntities = new ArrayList<>();
        traverse(entity, allEntities::add);
        return allEntities;
    }

    public static void traverse(Entity entity, Consumer<Entity> action) {
        traverse(entity, action, new IdentitySet<>());
    }

    private static void traverse(Entity entity, Consumer<Entity> action, IdentitySet<Entity> visited) {
        if(entity == null || visited.contains(entity)) {
            return;
        }
        visited.add(entity);
        action.accept(entity);
        EntityDesc desc = DescStore.get(entity.getClass());
        for (EntityProp prop : desc.getProps()) {
            if(prop.isNull(entity)) {
                continue;
            }
            if(prop.isEntity(entity)) {
                traverse(prop.getEntity(entity), action, visited);
            }
            else if(prop.isEntityList(entity)) {
                for (Entity ref : prop.getEntityList(entity)) {
                    traverse(ref, action, visited);
                }
            }
            else if(prop.isEntityMap(entity)) {
                for (Entity ref : prop.getEntityMap(entity).values()) {
                    traverse(ref, action, visited);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T copy(T object, IdentityHashMap<Object, Object> copyMap) {
        if(object == null) {
            return null;
        }
        if(copyMap.containsKey(object)) {
            return (T) copyMap.get(object);
        }
        if(object instanceof List<?> list) {
            return (T) copyList(list, copyMap);
        }
        if(object instanceof Set<?> set) {
            return (T) copySet(set, copyMap);
        }
        if(object instanceof Map<?,?> map) {
            return (T) copyMap(map, copyMap);
        }
        if(isShallow(object.getClass())) {
            return object;
        }
        if(object instanceof Entity entity) {
            return (T) makeDummyRef(getRealEntityType(entity), entity.id);
        }
        return (T) copyPojo(object, copyMap, false);
    }

    private static <T extends Entity> T makeDummyRef(Class<T> entityType, long id) {
        return EntityProxyFactory.makeDummy(entityType, id);
    }

    private static boolean isShallow(Class<?> klass) {
        if(Enum.class.isAssignableFrom(klass)) {
            return true;
        }
        if(Record.class.isAssignableFrom(klass)) {
            return true;
        }
        if(isContextClass(klass)) {
            return true;
        }
        if(isPrimitive(klass)) {
            return true;
        }
        return isSpringBean(klass);
    }

    public static <T> Class<?> getEntityType(T entity) {
        return getEntityType(entity.getClass());
    }

    public static Class<? extends Entity> getRealEntityType(Entity entity) {
        return getRealType(entity.getClass()).asSubclass(Entity.class);
    }

    public static Class<?> getRealType(Object model) {
        return getRealType(model.getClass());
    }

    public static Class<?> getRealType(Class<?> type) {
        if(ProxyObject.class.isAssignableFrom(type)) {
            return type.getSuperclass();
        }
        return type;
    }

    public static Class<?> getEntityType(Class<?> type) {
        if(ProxyObject.class.isAssignableFrom(type)) {
            return type.getSuperclass();
        }
        else {
            return type;
        }
//        Class<?> tmp = type;
//        while (tmp.getSuperclass() != Entity.class && tmp != Object.class) {
//            if(tmp.isAnnotationPresent(EntityType.class)) {
//                return tmp.asSubclass(Entity.class);
//            }
//            tmp = tmp.getSuperclass();
//        }
//        if (tmp == Object.class) {
//            throw new RuntimeException("Class '" + type.getName() + "' is not an entity type");
//        }
//        return tmp.asSubclass(Entity.class);
    }

    public static Class<? extends Value> getValueType(Value value) {
        return getValueType(value.getClass());
    }

    public static Class<? extends Value> getValueType(Class<? extends Value> type) {
        Class<?> tmp = type;
        while (tmp.getSuperclass() != Value.class && tmp != Object.class) {
//            if(tmp.isAnnotationPresent(EntityType.class)) {
//                return tmp.asSubclass(Value.class);
//            }
            tmp = tmp.getSuperclass();
        }
        if (tmp == Object.class) {
            throw new RuntimeException("Class '" + type.getName() + "' is not an value type");
        }
        return tmp.asSubclass(Value.class);
    }

    private static boolean isContextClass(Class<?> klass) {
        return CONTEXT_CLASSES.contains(klass);
    }

    private static boolean isSpringBean(Class<?> klass) {
        while(klass != Object.class && klass != null) {
            if(isSpringBean0(klass)) {
                return true;
            }
            klass = klass.getSuperclass();
        }
        return false;
    }

    private static boolean isSpringBean0(Class<?> klass) {
        return klass.isAnnotationPresent(Component.class) || klass.isAnnotationPresent(Mapper.class)
                || klass.isAnnotationPresent(Repository.class) || klass.isAnnotationPresent(RestController.class)
                || klass.isAnnotationPresent(Service.class);
    }

    @SuppressWarnings("unused")
    private static boolean isEntity(Class<?> klass) {
        return Entity.class.isAssignableFrom(klass) || ENTITY_CLASSES.contains(klass);
    }

    private static Object copyPojo(Object object, IdentityHashMap<Object, Object> copyMap, boolean ignoreTransient) {
        Object copy = ReflectUtils.allocateInstance(object.getClass());
        copyMap.put(object, copy);
        copyPojo(object, copy, copyMap, ignoreTransient);
        return copy;
    }

    public static void copyPojo(Object src, Object target, IdentityHashMap<Object, Object> copyMap, boolean ignoreTransient) {
        List<Field> fields = ReflectUtils.getInstanceFields(src.getClass());
        for (Field field : fields) {
            if(ignoreTransient && Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            ReflectUtils.set(
                    target,
                    field,
                    copy(ReflectUtils.get(src, field), copyMap)
            );
        }
    }

    private static Set<Object> copySet(Set<?> set, IdentityHashMap<Object, Object> copyMap) {
        Set<Object> copied = newSet(set);
        copyMap.put(set, copied);
        copied.addAll(NncUtils.map(set, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static List<Object> copyList(List<?> list, IdentityHashMap<Object, Object> copyMap) {
        List<Object> copied = newList(list);
        copyMap.put(list, copied);
        copied.addAll(NncUtils.map(list, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static Map<Object, Object> copyMap(Map<?, ?> map, IdentityHashMap<Object, Object> copyMap) {
        Map<Object, Object> copy = newMap(map);
        copyMap.put(map, copy);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            copy.put(entry.getKey(), copy(entry.getValue(), copyMap));
        }
        return copy;
    }

    private static <T> List<T> newList(List<? extends T> prototype) {
        if(prototype instanceof LinkedList) {
            return new LinkedList<>();
        }
        if(prototype instanceof java.util.LinkedList) {
            return new java.util.LinkedList<>();
        }
        else {
            return new ArrayList<>();
        }
    }

    private static <K, V> Map<K, V> newMap(Map<? extends K, ? extends V> prototype) {
        if(prototype instanceof TreeMap) {
            return new TreeMap<>();
        }
        else if(prototype instanceof LinkedHashMap) {
            return new LinkedHashMap<>();
        }
        else {
            return new HashMap<>();
        }
    }

    private static <T> Set<T> newSet(Set<? extends T> prototype) {
        if(prototype instanceof TreeSet) {
            return new TreeSet<>();
        }
        else if(prototype instanceof LinkedHashSet) {
            return new LinkedHashSet<>();
        }
        else {
            return new HashSet<>();
        }
    }

    private enum DiffState {
        DOING,
        DONE
    }

    private record DiffPair(Object first, Object second) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiffPair diffPair = (DiffPair) o;
            return first == diffPair.first && second == diffPair.second;
        }

    }

}
