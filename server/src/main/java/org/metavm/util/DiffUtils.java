package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.object.instance.core.InstanceContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
        if (visited.size() > EntityUtils.MAXIMUM_DIFF_DEPTH)
            throw new InternalException("Diff depth exceeds maximum depth " + EntityUtils.MAXIMUM_DIFF_DEPTH);
        DiffPair diffPair = new DiffPair(pojo1, pojo2);
        if (visited.containsKey(diffPair)) return false;
        if (pojo1 == pojo2) return false;
        if (pojo1 == null || pojo2 == null) return true;
        Class<?> clazz = pojo1.getClass();
        if (clazz != pojo2.getClass()) return true;
        visited.put(diffPair, DiffState.DOING);
        EntityUtils.ensureProxyInitialized(pojo1);
        EntityUtils.ensureProxyInitialized(pojo2);
        EntityDesc desc = DescStore.get(clazz);
        for (EntityProp prop : desc.getProps()) {
            if (prop.isTransient()) continue;
            Object value1 = prop.get(pojo1), value2 = prop.get(pojo2);
            if (isDifferent(value1, value2, visited)) return true;
        }
        visited.put(diffPair, DiffState.DONE);
        return false;
    }

    private static boolean isEntityDifferent(Entity entity1, Entity entity2, Map<Object, Object> visited) {
        if (entity1.tryGetId() != null && entity2.tryGetId() != null) {
            return !Objects.equals(entity1.key(), entity2.key());
        }
        if (entity1.tryGetId() != null || entity2.tryGetId() != null) {
            return false;
        } else {
            return isPojoDifferent(entity1, entity2, visited);
        }
    }

    private static boolean isMapDifferent(Map<?, ?> map1, Map<?, ?> map2, Map<Object, Object> visited) {
        if (map1.size() != map2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = Utils.buildPairsForMap(map1, map2);
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
        return klass.isAnnotationPresent(Component.class)
                || klass.isAnnotationPresent(Repository.class) || klass.isAnnotationPresent(RestController.class)
                || klass.isAnnotationPresent(Service.class);
    }

    private static boolean isCollectionDifferent(Collection<?> coll1, Collection<?> coll2,
                                                 Map<Object, Object> visited) {
        if (coll1.size() != coll2.size()) {
            return true;
        }
        List<Pair<Object>> pairs = Utils.buildPairs(coll1, coll2);
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
