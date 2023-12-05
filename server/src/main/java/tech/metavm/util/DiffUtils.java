package tech.metavm.util;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.InstanceContext;
import org.springframework.stereotype.Component;
import org.apache.ibatis.annotations.Mapper;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static tech.metavm.entity.EntityUtils.getRealClass;

public class DiffUtils {

    public static final Set<Class<?>> CONTEXT_CLASSES = Set.of(
            InstanceContext.class
    );

    public static boolean isDifferent(Object value1, Object value2, Map<Object, Object> visited) {
        if (value1 == null && value2 == null) {
            return false;
        } else if (value1 == null || value2 == null) {
            return true;
        } else if (value1 instanceof Entity entity1) {
            if (value2 instanceof Entity entity2) {
                return isEntityDifferent(entity1, entity2, visited);
            } else {
                return true;
            }
        } else if (value1 instanceof Map<?, ?> map1) {
            if ((value2 instanceof Map<?, ?> map2)) {
                return isMapDifferent(map1, map2, visited);
            } else {
                return true;
            }
        } else if (value1 instanceof Collection<?> coll1) {
            if (value2 instanceof Collection<?> coll2) {
                return isCollectionDifferent(coll1, coll2, visited);
            } else {
                return true;
            }
        } else if(value1 instanceof byte[] bytes1 && value2 instanceof byte[] bytes2) {
            return !Arrays.equals(bytes1, bytes2);
        } else if (isPojo(value1.getClass())) {
            return isPojoDifferent(value1, value2, visited);
        } else {
            return !Objects.equals(value1, value2);
        }
    }

    public static boolean isPojoDifferent(Object pojo1, Object pojo2) {
        return isPojoDifferent(pojo1, pojo2, new HashMap<>());
    }

    private static boolean isPojoDifferent(Object pojo1, Object pojo2, Map<Object, Object> visited) {
        if (visited.size() > EntityUtils.MAXIMUM_DIFF_DEPTH) {
            throw new InternalException("Diff depth exceeds maximum depth " + EntityUtils.MAXIMUM_DIFF_DEPTH);
        }
        DiffPair diffPair = new DiffPair(pojo1, pojo2);
        if (visited.containsKey(diffPair)) {
            return false;
//            DiffState state = (DiffState) visited.get(diffPair);
//            if(state == DiffState.DONE) {
//                return false;
//            }
//            throw new RuntimeException("Back reference of POJO is currently not supported");
        }
        if (pojo1 == null && pojo2 == null) {
            return true;
        }
        if (pojo1 == null || pojo2 == null) {
            return false;
        }
        Class<?> realClass = getRealClass(pojo1.getClass(), pojo2.getClass());
        if (realClass == null) {
            return true;
        }

        visited.put(diffPair, DiffState.DOING);
//        visited.put(pojo2, pojo1);
        EntityUtils.ensureProxyInitialized(pojo1);
        EntityUtils.ensureProxyInitialized(pojo2);
//        Class<?> klass = pojo1.getClass();
        EntityDesc desc = DescStore.get(realClass);
        for (EntityProp prop : desc.getProps()) {
            if (prop.isTransient()) {
                continue;
            }
            Object value1 = prop.get(pojo1), value2 = prop.get(pojo2);
            if (isDifferent(value1, value2, visited)) {
                return true;
            }
        }
        visited.put(diffPair, DiffState.DONE);
        return false;
    }

    private static boolean isEntityDifferent(Entity entity1, Entity entity2, Map<Object, Object> visited) {
        if (entity1.getId() != null && entity2.getId() != null) {
            return !Objects.equals(entity1.key(), entity2.key());
        }
        if (entity1.getId() != null || entity2.getId() != null) {
            return false;
        } else {
            return isPojoDifferent(entity1, entity2, visited);
        }
    }

    private static boolean isMapDifferent(Map<?, ?> map1, Map<?, ?> map2, Map<Object, Object> visited) {
        if (map1.size() != map2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = NncUtils.buildPairsForMap(map1, map2);
        for (Pair<Object> pair : pairs) {
            if (isDifferent(pair.first(), pair.second(), visited)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isShallow(Class<?> klass) {
        if (Enum.class.isAssignableFrom(klass)) {
            return true;
        }
        if (Record.class.isAssignableFrom(klass)) {
            return true;
        }
        if (isContextClass(klass)) {
            return true;
        }
        if (EntityUtils.isPrimitive(klass)) {
            return true;
        }
        return isSpringBean(klass);
    }

    private static boolean isSpringBean(Class<?> klass) {
        while (klass != Object.class && klass != null) {
            if (isSpringBean0(klass)) {
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

    private static boolean isCollectionDifferent(Collection<?> coll1, Collection<?> coll2,
                                                 Map<Object, Object> visited) {
        if (coll1.size() != coll2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = NncUtils.buildPairs(coll1, coll2);
        for (Pair<Object> pair : pairs) {
            if (isDifferent(pair.first(), pair.second(), visited)) {
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

    private static boolean isContextClass(Class<?> klass) {
        return CONTEXT_CLASSES.contains(klass);
    }

    public static boolean pojoEquals(Object pojo1, Object pojo2) {
        return !isPojoDifferent(pojo1, pojo2);
    }

    @SuppressWarnings("unchecked")
    public static <T> T copy(T object, IdentityHashMap<Object, Object> copyMap) {
        if (object == null) {
            return null;
        }
        if (copyMap.containsKey(object)) {
            return (T) copyMap.get(object);
        }
        if (object instanceof List<?> list) {
            return (T) copyList(list, copyMap);
        }
        if (object instanceof Set<?> set) {
            return (T) copySet(set, copyMap);
        }
        if (object instanceof Map<?, ?> map) {
            return (T) copyMap(map, copyMap);
        }
        if (isShallow(object.getClass())) {
            return object;
        }
        if(object instanceof byte[] bytes) {
            return (T) Arrays.copyOf(bytes, bytes.length);
        }
        if (object instanceof Entity entity) {
            return (T) EntityUtils.makeDummyRef(EntityUtils.getRealEntityType(entity), entity.getIdRequired());
        }
        return (T) copyPojo(object, copyMap, false);
    }

    public static Object copyPojo(Object object, IdentityHashMap<Object, Object> copyMap, boolean ignoreTransient) {
        Object copy = ReflectUtils.allocateInstance(object.getClass());
        copyMap.put(object, copy);
        copyPojo(object, copy, copyMap, ignoreTransient);
        return copy;
    }

    public static void copyPojo(Object src, Object target, IdentityHashMap<Object, Object> copyMap, boolean ignoreTransient) {
        List<Field> fields = ReflectUtils.getInstanceFields(src.getClass());
        for (Field field : fields) {
            if (ignoreTransient && Modifier.isTransient(field.getModifiers())) {
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
        Set<Object> copied = EntityUtils.newSet(set);
        copyMap.put(set, copied);
        copied.addAll(NncUtils.map(set, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static List<Object> copyList(List<?> list, IdentityHashMap<Object, Object> copyMap) {
        List<Object> copied = EntityUtils.newList(list);
        copyMap.put(list, copied);
        copied.addAll(NncUtils.map(list, ele -> copy(ele, copyMap)));
        return copied;
    }

    private static Map<Object, Object> copyMap(Map<?, ?> map, IdentityHashMap<Object, Object> copyMap) {
        Map<Object, Object> copy = EntityUtils.newMap(map);
        copyMap.put(map, copy);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            copy.put(entry.getKey(), copy(entry.getValue(), copyMap));
        }
        return copy;
    }

    public static Entity copyEntity(Entity entity) {
        return (Entity) copyPojo(entity, new IdentityHashMap<>(), true);
    }

    @SuppressWarnings("unchecked")
    public static <T> T copyPojo(T pojo) {
        return (T) copyPojo(pojo, new IdentityHashMap<>(), true);
    }

    public static Value copyValue(Value value) {
        return (Value) copyPojo(value, new IdentityHashMap<>(), true);
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
