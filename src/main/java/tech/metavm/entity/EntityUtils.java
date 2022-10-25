package tech.metavm.entity;

import javassist.util.proxy.ProxyObject;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.meta.EnumConstant;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class EntityUtils {

    public static final Set<Class<?>> PRIM_CLASSES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, String.class, Date.class, LocalDateTime.class,
            BigInteger.class, BigDecimal.class, Class.class, Object.class
    );

    public static final Set<Class<?>> ENTITY_CLASSES = Set.of(
        Type.class, tech.metavm.object.meta.Field.class, Instance.class,
            EnumConstant.class
    );

    public static final Set<Class<?>> CONTEXT_CLASSES = Set.of(
            InstanceContext.class, EntityContext.class
    );

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
        if(entity1.getId() == null && entity2.getId() == null) {
            return extractTrueEntity(entity1) == extractTrueEntity(entity2);
        }
        return false;
    }

    private static boolean equals(Object value1, Object value2, IdentitySet<Object> visited) {
        if(value1 == null && value2 == null) {
            return true;
        }
        else if(value1 == null || value2 == null) {
            return false;
        }
        else if(value1 instanceof Entity entity1) {
            if(value2 instanceof Entity entity2) {
                return Objects.equals(entity1.key(), entity2.key());
            }
            else {
                return false;
            }
        }
        else if(value1 instanceof Map map1) {
            if((value2 instanceof Map map2)) {
                return mapEquals(map1, map2, visited);
            }
            else {
                return false;
            }
        }
        else if(value1 instanceof Collection coll1) {
            if(value2 instanceof Collection coll2) {
                return collectionEquals(coll1, coll2, visited);
            }
            else {
                return false;
            }
        }
        else if(isPojo(value1.getClass())) {
            return pojoEquals(value1, value2, visited);
        }
        else {
            return Objects.equals(value1, value2);
        }
    }

    public static boolean pojoEquals(Object pojo1, Object pojo2) {
        return pojoEquals(pojo1, pojo2, new IdentitySet<>());
    }

    private static boolean pojoEquals(Object pojo1, Object pojo2, IdentitySet<Object> visited) {
        if(visited.contains(pojo1) || visited.contains(pojo2)) {
            throw new RuntimeException("Back reference of POJO is currently not supported");
        }
        if(!pojo1.getClass().equals(pojo2.getClass())) {
            return false;
        }
        visited.add(pojo1);
        visited.add(pojo2);
        Class<?> klass = pojo1.getClass();
        EntityDesc desc = DescStore.get(klass);
        for (EntityProp prop : desc.getProps()) {
            if(prop.isTransient()) {
                continue;
            }
            Object value1 = prop.get(pojo1), value2 = prop.get(pojo2);
            if(!equals(value1, value2, visited)) {
                return false;
            }
        }
        return true;
    }

    private static boolean mapEquals(Map map1, Map map2, IdentitySet<Object> visited) {
        if(map1.size() != map2.size()) {
            return false;
        }
        List<Pair> pairs = NncUtils.buildPairsForMap(map1, map2);
        for (Pair pair : pairs) {
            if(!equals(pair.first(), pair.second(), visited)) {
                return false;
            }
        }
        return true;
    }

    private static boolean collectionEquals(Collection coll1, Collection coll2, IdentitySet<Object> visited) {
        if(coll1.size() != coll2.size()) {
            return false;
        }
        List<Pair> pairs = NncUtils.buildPairs(coll1, coll2);
        for (Pair pair : pairs) {
            if(!equals(pair.first(), pair.second(), visited)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPojo(Class<?> klass) {
        return !Entity.class.isAssignableFrom(klass)
                && !Map.class.isAssignableFrom(klass) && !Collection.class.isAssignableFrom(klass)
                && !isShallow(klass);
    }

    public static Entity extractTrueEntity(Entity entity) {
        if(entity instanceof ProxyObject proxyObject) {
            EntityMethodHandler handler = (EntityMethodHandler) proxyObject.getHandler();
            return handler.getEntity();
        }
        else {
            return entity;
        }
    }

    public static boolean isPrimitive(Class<?> klass) {
        return PRIM_CLASSES.contains(klass);
    }

    public static Entity copyEntity(Entity entity) {
        return (Entity) copyPojo(entity, new IdentityHashMap<>(), true);
    }

    public static List<Entity> getAllEntities(Entity entity) {
        List<Entity> allEntities = new ArrayList<>();
        traverse(entity, allEntities::add);
        return allEntities;
    }

    public static void traverse(Entity entity, Consumer<Entity> action) {
        traverse(entity, action, new IdentitySet<>());
    }

    public static void traverse(Entity entity, Consumer<Entity> action, IdentitySet<Entity> visited) {
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

    private static Object copy(Object object, IdentityHashMap<Object, Object> copyMap) {
        if(object == null) {
            return null;
        }
        if(copyMap.containsKey(object)) {
            return copyMap.get(object);
        }
        if(object instanceof List list) {
            return copyList(list, copyMap);
        }
        if(object instanceof Set set) {
            return copySet(set, copyMap);
        }
        if(object instanceof Map map) {
            return copyMap(map, copyMap);
        }
        if(isShallow(object.getClass())) {
            return object;
        }
        if(object instanceof Entity entity) {
            return entity.context.getRef(getEntityType(entity), entity.id);
        }
        return copyPojo(object, copyMap, false);
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
        if(isSpringBean(klass)) {
            return true;
        }
        return false;
    }

    public static Class<?> getEntityType(Entity entity) {
        return getEntityType(entity.getClass());
    }

    public static <T> Class<?> getEntityType(Class<T> type) {
        Class<?> tmp = type;
        while (tmp.getSuperclass() != Entity.class && tmp != Object.class) {
            tmp = tmp.getSuperclass();
        }
        if (tmp == Object.class) {
            throw new RuntimeException("category " + type.getName() + " is not an entity category");
        }
        return tmp;
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

    private static boolean isEntity(Class<?> klass) {
        return Entity.class.isAssignableFrom(klass) || ENTITY_CLASSES.contains(klass);
    }

    private static Object copyPojo(Object object, IdentityHashMap<Object, Object> copyMap, boolean ignoreTransient) {
        Object copy = ReflectUtils.newInstance(object.getClass());
        copyMap.put(object, copy);
        List<Field> fields = ReflectUtils.getAllFields(object.getClass());
        for (Field field : fields) {
            if(ignoreTransient && Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            ReflectUtils.set(
                    copy,
                    field,
                    copy(ReflectUtils.get(object, field), copyMap)
            );
        }
        return copy;
    }

    private static Object copySet(Set<Object> set, IdentityHashMap<Object, Object> copyMap) {
        Set<Object> copied = newSet(set);
        copyMap.put(set, copied);
        copied.addAll(NncUtils.map(set, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static Object copyList(List<Object> list, IdentityHashMap<Object, Object> copyMap) {
        List<Object> copied = newList(list);
        copyMap.put(list, copied);
        copied.addAll(NncUtils.map(list, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static Object copyMap(Map<Object, Object> map, IdentityHashMap<Object, Object> copyMap) {
        Map<Object, Object> copy = newMap(map);
        copyMap.put(map, copy);
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            copy.put(entry.getKey(), copy(entry.getValue(), copyMap));
        }
        return copy;
    }

    private static <T> List<T> newList(List<T> prototype) {
        if(prototype instanceof LinkedList) {
            return new LinkedList<>();
        }
        else {
            return new ArrayList<>();
        }
    }

    private static <K, V> Map<K, V> newMap(Map<K, V> prototype) {
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

    private static <T> Set<T> newSet(Set<T> prototype) {
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

}
