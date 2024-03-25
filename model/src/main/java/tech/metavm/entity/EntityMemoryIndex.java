package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class EntityMemoryIndex {

    private final IdentitySet<Object> entities = new IdentitySet<>();
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
        if (entities.add(object)) {
            typeIndex.computeIfAbsent(klass, k -> new ArrayList<>()).add(object);
        }
        var defList = getIndexDefList(klass);
        defList.forEach(def -> save(def, object));
    }

    public <T> void save(IndexDef<T> indexDef, Object object) {
        var index = getIndex(indexDef);
        index.save(indexDef.getType().cast(object));
    }

    public void remove(Object object) {
        var klass = EntityUtils.getRealType(object.getClass());
        var objects = typeIndex.get(klass);
        if (objects != null)
            objects.remove(object);
        var defList = getIndexDefList(klass);
        defList.forEach(def -> remove(def, object));
    }

    public <T> void remove(IndexDef<T> indexDef, Object object) {
        var index = getIndex(indexDef);
        index.remove(indexDef.getType().cast(object));
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

        private final IdentityHashMap<Object, List<Key>> object2keys = new IdentityHashMap<>();
        private final IndexDef<T> indexDef;
        private final NavigableSet<Entry<T>> index = new TreeSet<>();

        private SubIndex(IndexDef<T> indexDef) {
            this.indexDef = indexDef;
        }

        public List<T> selectByKey(List<Object> values) {
            var key = new Key(values);
            //noinspection unchecked
            return index.subSet(new Entry<>(key, null), new Entry<>(key, (T) MAX_OBJECT)).stream().map(Entry::object).toList();
        }

        public @Nullable T selectByUniqueKey(List<Object> values) {
            NncUtils.requireTrue(indexDef.isUnique());
            return NncUtils.first(selectByKey(values));
        }

        private List<Key> getKey(Object object) {
            var fields = getIndexFields(indexDef);
            var values = NncUtils.map(fields, f -> ReflectionUtils.get(object, f));
            return List.of(new Key(values));
        }

        private List<Field> getIndexFields(IndexDef<?> def) {
            var klass = def.getType();
            return NncUtils.map(def.getFieldNames(), fn -> ReflectionUtils.getDeclaredFieldRecursively(klass, fn));
        }

        public List<T> query(EntityIndexQuery<T> query) {
            NncUtils.requireTrue(indexDef == query.indexDef());
            return query(NncUtils.get(query.from(), f -> new Key(f.values())),
                    NncUtils.get(query.to(), t -> new Key(t.values()))).stream()
                    .map(Entry::object)
                    .sorted(query.desc() ? Collections.reverseOrder(SubIndex::compareObject) : SubIndex::compareObject)
                    .distinct()
                    .limit(query.limit())
                    .toList();
        }

        private Collection<Entry<T>> query(@Nullable Key from, @Nullable Key to) {
            if(from == null && to == null)
                return index;
            if(from == null)
                return index.headSet(new Entry<>(to, null), true);
            if(to == null)
                return index.tailSet(new Entry<>(from, null), true);
            return index.subSet(new Entry<>(from, null), true, new Entry<>(to, null), true);
        }

        private static int compareObject(Object o1, Object o2) {
            if(o1 == o2)
                return 0;
            if(o1 == MAX_OBJECT)
                return 1;
            if(o2 == MAX_OBJECT)
                return -1;
            if (o1 instanceof Entity e1 && o2 instanceof Entity e2) {
                if (e1.tryGetId() != null && e2.tryGetId() != null)
                    return Objects.compare(e1.tryGetId(), e2.tryGetId(), Id::compareTo);
                if (e1.getTmpId() != null && e2.getTmpId() != null)
                    return Long.compare(e1.getTmpId(), e2.getTmpId());
            }
            return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
        }

        public void save(T object) {
            remove(object);
            var keys = getKey(object);
            object2keys.put(object, keys);
            for (Key key : keys) {
                if (indexDef.isUnique() && !key.containsNull()) {
                    var existing = query(key, key);
                    if (existing.size() > 1 || existing.size() == 1 && existing.iterator().next().object != object)
                        throw new InternalException("Duplicate values found for index key: " + key);
                }
                index.add(new Entry<>(key, object));
            }
        }

        public void remove(T object) {
            var keys = object2keys.remove(object);
            if (keys != null) {
                for (Key key : keys) {
                    index.remove(new Entry<>(key, object));
                }
            }
        }

        private record Entry<T>(Key key, T object) implements Comparable<Entry<T>> {

            @Override
            public int compareTo(@NotNull EntityMemoryIndex.SubIndex.Entry<T> o) {
                var keyComparison = key.compareTo(o.key);
                if (keyComparison != 0)
                    return keyComparison;
                return compareObject(object, o.object);
            }

        }

        public static final Object MAX_OBJECT = new Object() {
            @Override
            public String toString() {
                return "MAX_OBJECT";
            }
        };

        private record Key(List<Object> values) implements Comparable<Key> {

            boolean containsNull() {
                return values.contains(null);
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public int compareTo(@NotNull EntityMemoryIndex.SubIndex.Key o) {
                var it1 = values.iterator();
                var it2 = o.values.iterator();
                while (it1.hasNext() && it2.hasNext()) {
                    var v1 = it1.next();
                    var v2 = it2.next();
                    if (v1 == v2)
                        continue;
                    if(v1 == MAX_OBJECT)
                        return 1;
                    if(v2 == MAX_OBJECT)
                        return -1;
                    if (v1 == null)
                        return -1;
                    if (v2 == null)
                        return 1;
                    if (v1 instanceof Comparable c1 && v2 instanceof Comparable c2) {
                        var cmp = Objects.compare(c1, c2, Comparable::compareTo);
                        if (cmp != 0)
                            return cmp;
                    } else {
                        var cmp = Integer.compare(System.identityHashCode(v1), System.identityHashCode(v2));
                        if (cmp != 0)
                            return cmp;
                    }
                }
                return it1.hasNext() ? 1 : it2.hasNext() ? -1 : 0;
            }
        }
    }

}
