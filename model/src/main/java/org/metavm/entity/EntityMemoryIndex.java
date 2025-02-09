package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
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
        return Utils.map(subList, type::cast);
    }

    public void save(Object object) {
        var klass = EntityUtils.getRealType(object.getClass());
        if (entities.add(object)) {
            typeIndex.computeIfAbsent(klass, k -> new ArrayList<>()).add(object);
        }
        var defList = getIndexDefList(klass);
        defList.forEach(def -> save(def, object));
    }

    public <T extends Entity> void save(IndexDef<T> indexDef, Object object) {
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

    public <T extends Entity> void remove(IndexDef<T> indexDef, Object object) {
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

    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, List<Value> values) {
        return getIndex(indexDef).selectByKey(values);
    }

    public <T extends Entity> @Nullable T selectByUniqueKey(IndexDef<T> indexDef, List<Value> values) {
        return getIndex(indexDef).selectByUniqueKey(values);
    }

    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        var index = getIndex(query.indexDef());
        return index.query(query);
    }

    private <T extends Entity> SubIndex<T> getIndex(IndexDef<T> def) {
        //noinspection unchecked
        return (SubIndex<T>) indices.computeIfAbsent(def, SubIndex::new);
    }

    public void clear() {
        entities.clear();
        typeIndex.clear();
        indices.clear();
    }

    private static class SubIndex<T extends Entity> {

        private final IdentityHashMap<Object, List<Key>> object2keys = new IdentityHashMap<>();
        private final IndexDef<T> indexDef;
        private final NavigableSet<Entry<T>> index = new TreeSet<>();

        private SubIndex(IndexDef<T> indexDef) {
            this.indexDef = indexDef;
        }

        public List<T> selectByKey(List<Value> values) {
            var key = new Key(values);
            //noinspection unchecked
            return index.subSet(new Entry<>(key, null), new Entry<>(key, (T) MAX_OBJECT)).stream().map(Entry::object).toList();
        }

        public @Nullable T selectByUniqueKey(List<Value> values) {
            return Utils.first(selectByKey(values));
        }

        private List<Key> getKey(Entity object) {
            return List.of(new Key(indexDef.getValues(object)));
        }

        public List<T> query(EntityIndexQuery<T> query) {
            Utils.require(indexDef == query.indexDef());
            return query(Utils.safeCall(query.from(), f -> new Key(f.values())),
                    Utils.safeCall(query.to(), t -> new Key(t.values()))).stream()
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
            if (o1 instanceof Reference e1 && o2 instanceof Reference e2) {
                if (e1.tryGetId() != null && e2.tryGetId() != null)
                    return Objects.compare(e1.tryGetId(), e2.tryGetId(), Id::compareTo);
//                if (e1.getTmpId() != null && e2.getTmpId() != null)
//                    return Long.compare(e1.getTmpId(), e2.getTmpId());
            }
            return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
        }

        public void save(T object) {
            var tracing = DebugEnv.traceMemoryIndex;
            if (tracing) {
                if (object instanceof Klass klass)
                    log.trace("Indexing class {}", klass.getQualifiedName());
            }
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

        public static final Object MAX_OBJECT = new Entity(TmpId.random()) {

            public int getEntityTag() {
                throw new UnsupportedOperationException();
            }

            @Generated
            public static void visitBody(StreamVisitor visitor) {
            }

            @Generated
            @Override
            public void readBody(MvInput input, Entity parent) {
            }

            @Generated
            @Override
            public void writeBody(MvOutput output) {
            }

            @Override
            public void forEachChild(Consumer<? super Instance> action) {

            }

            @Override
            public void forEachValue(Consumer<? super Instance> action) {

            }

            @Override
            protected void buildJson(Map<String, Object> map) {

            }

            @Override
            protected void buildSource(Map<String, Value> source) {
                
            }

            @Override
            public ClassType getInstanceType() {
                return null;
            }

            @Override
            public void forEachReference(Consumer<Reference> action) {

            }

            @Override
            public Klass getInstanceKlass() {
                return null;
            }

            @Nullable
            @Override
            public Entity getParentEntity() {
                return null;
            }

            @Override
            public String toString() {
                return "MAX_OBJECT";
            }

        };

        private record Key(@NotNull List<Value> values) implements Comparable<Key> {

            boolean containsNull() {
                return Utils.anyMatch(values, Value::isNull);
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
                    if (v1 instanceof StringReference s1 && v2 instanceof StringReference s2)
                        return s1.compareTo(s2);
                    if (v1 instanceof Reference e1 && v2 instanceof Reference e2) {
                        if (e1.tryGetId() != null && e2.tryGetId() != null)
                            return Objects.compare(e1.tryGetId(), e2.tryGetId(), Id::compareTo);
                    }
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
