package tech.metavm.entity;

import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class EntityMemoryIndex {

    private final Map<Class<?>, List<Object>> typeIndex = new IdentityHashMap<>();
    private final Map<IndexDef<?>, SubIndex<?>> indices = new IdentityHashMap<>();

    public <T> List<T> selectByType(Class<? extends T> type, T startExclusive, long limit) {
        var objects = typeIndex.getOrDefault(type, List.of());
        int startIdx;
        if (startExclusive == null)
            startIdx = 0;
        else {
            int idx = objects.indexOf(startExclusive);
            if (idx < 0)
                return List.of();
            startIdx = idx + 1;
        }
        var subList = objects.subList(startIdx, Math.min(objects.size(), startIdx + (int) limit));
        return NncUtils.map(subList, type::cast);
    }

    public void save(Object object) {
        var klass = EntityUtils.getRealType(object.getClass());
        var objects = typeIndex.computeIfAbsent(klass, k -> new ArrayList<>());
        if(!objects.contains(object))
            objects.add(object);
        var defList = getIndexDefList(klass);
        for (IndexDef<?> indexDef : defList)
            save(indexDef, object);
    }

    public <T> void save(IndexDef<T> indexDef, Object object) {
        var index = getIndex(indexDef);
        index.save(indexDef.getType().cast(object));
    }

    private List<IndexDef<?>> getIndexDefList(Class<?> klass) {
        List<IndexDef<?>> indexDefList = new ArrayList<>();
        ReflectionUtils.forEachField(klass, f -> {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == IndexDef.class)
                indexDefList.add((IndexDef<?>) Objects.requireNonNull(ReflectionUtils.get(null, f)));
        });
        return indexDefList;
    }

    public <T> List<T> selectByKey(IndexDef<T> indexDef, List<Object> values) {
        return getIndex(indexDef).selectByKey(values);
    }

    public <T> @Nullable T selectByUniqueKey(IndexDef<T> indexDef, List<Object> values) {
        return getIndex(indexDef).selectByUniqueKey(values);
    }

    public <T> List<T> query(EntityIndexQuery<T> query) {
        var index = getIndex(query.indexDef());
        return index.query(query);
    }

    private <T> SubIndex<T> getIndex(IndexDef<T> def) {
        //noinspection unchecked
        return (SubIndex<T>) indices.computeIfAbsent(def, SubIndex::new);
    }

    private static class SubIndex<T> {

        private final IndexDef<T> indexDef;
        private final Map<Key, Set<T>> index = new HashMap<>();

        private SubIndex(IndexDef<T> indexDef) {
            this.indexDef = indexDef;
        }

        public List<T> selectByKey(List<Object> values) {
            var key = new Key(values);
            return NncUtils.map(index.getOrDefault(key, Set.of()), indexDef.getType()::cast);
        }

        public @Nullable T selectByUniqueKey(List<Object> values) {
            NncUtils.requireTrue(indexDef.isUnique());
            return NncUtils.first(selectByKey(values));
        }

        private Key getKey(Object object) {
            var fields = getIndexFields(indexDef);
            var values = NncUtils.map(fields, f -> ReflectionUtils.get(object, f));
            return new Key(values);
        }

        private List<Field> getIndexFields(IndexDef<?> def) {
            var klass = def.getType();
            return NncUtils.map(def.getFieldNames(), fn -> ReflectionUtils.getDeclaredFieldRecursively(klass, fn));
        }

        public List<T> query(EntityIndexQuery<T> query) {
            NncUtils.requireTrue(indexDef == query.indexDef());
            var result = new ArrayList<T>();
            index.forEach((key, objects) -> {
                if (key.match(query))
                    result.addAll(objects);
            });
            if (query.desc())
                result.sort((o1, o2) -> -compareObject(o2, o1));
            else
                result.sort(this::compareObject);
            return result.subList(0, Math.min((int) query.limit(), result.size()));
        }

        private int compareObject(Object o1, Object o2) {
            if (o1 instanceof Entity e1 && o2 instanceof Entity e2) {
                if (e1.getId() != null && e2.getId() != null)
                    return Long.compare(e1.getId(), e2.getId());
                if (e1.getTmpId() != null && e2.getTmpId() != null)
                    return Long.compare(e1.getTmpId(), e2.getTmpId());
            }
            return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
        }

        public void save(T object) {
            var key = getKey(object);
            var objects = index.computeIfAbsent(key, k -> new IdentitySet<>());
            if (indexDef.isUnique() && !key.containsNull() && !objects.isEmpty()) {
                if (objects.iterator().next() != object)
                    throw new InternalException("Duplicate values found for index key: " + key);
            } else
                objects.add(object);
        }

        private record Key(List<Object> values) {

            boolean match(EntityIndexQuery<?> query) {
                var ref = new Object() {
                    boolean matches = true;
                };
                NncUtils.biForEach(values, query.items(), (v, item) -> {
                    if (!ref.matches)
                        return;
                    ref.matches = item.operator().evaluate(v, item.value());
                });
                return ref.matches;
            }

            boolean containsNull() {
                return values.contains(null);
            }

        }
    }

}
