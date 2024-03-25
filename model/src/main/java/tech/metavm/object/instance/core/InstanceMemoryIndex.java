package tech.metavm.object.instance.core;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexEntryRT;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.Index;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.object.instance.IndexKeyRT.MAX_INSTANCE;
import static tech.metavm.object.instance.IndexKeyRT.MIN_INSTANCE;

public class InstanceMemoryIndex {

    private final Map<Index, SubIndex> indexMap = new IdentityHashMap<>();
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final Map<ClassInstance, List<IndexKeyRT>> keyMap = new IdentityHashMap<>();

    public InstanceMemoryIndex(ParameterizedFlowProvider parameterizedFlowProvider) {
        this.parameterizedFlowProvider = parameterizedFlowProvider;
    }

    public @Nullable ClassInstance selectUnique(IndexKeyRT key) {
        NncUtils.requireTrue(key.getIndex().isUnique());
        return subIndex(key.getIndex()).selectUnique(key);
    }

    public List<ClassInstance> select(IndexKeyRT key) {
        return subIndex(key.getIndex()).select(key);
    }

    public List<ClassInstance> query(InstanceIndexQuery query) {
        return subIndex(query.index()).query(query);
    }

    public List<ClassInstance> scan(Index index, IndexKeyRT from, IndexKeyRT to) {
        return subIndex(index).scan(from, to);
    }

    public long count(Index index, IndexKeyRT from, IndexKeyRT to) {
        return subIndex(index).count(from, to);
    }

    private SubIndex subIndex(Index index) {
        return indexMap.computeIfAbsent(index, SubIndex::new);
    }

    public void save(ClassInstance instance) {
        remove(instance);
        var keys = instance.getIndexKeys(parameterizedFlowProvider);
        keyMap.put(instance, new ArrayList<>(keys));
        for (IndexKeyRT key : keys) {
            indexMap.computeIfAbsent(key.getIndex(), SubIndex::new).add(key, instance);
        }
    }

    public void remove(ClassInstance instance) {
        var oldKeys = keyMap.remove(instance);
        if (oldKeys != null) {
            for (IndexKeyRT oldKey : oldKeys) {
                indexMap.get(oldKey.getIndex()).remove(oldKey, instance);
            }
        }
    }

    private static class SubIndex {
        private final Index index;
        private final NavigableSet<IndexEntryRT> map = new TreeSet<>();

        private SubIndex(Index index) {
            this.index = index;
        }

        public List<ClassInstance> select(IndexKeyRT key) {
            return query(key.toQuery());
        }

        public @Nullable ClassInstance selectUnique(IndexKeyRT key) {
            NncUtils.requireTrue(index.isUnique());
            return NncUtils.first(select(key));
        }

        public void remove(IndexKeyRT key, ClassInstance instance) {
            map.remove(new IndexEntryRT(key, instance));
        }

        public void add(IndexKeyRT key, ClassInstance instance) {
            map.add(new IndexEntryRT(key, instance));
        }

        public List<ClassInstance> query(InstanceIndexQuery query) {
            var entries = query(query.index(), NncUtils.get(query.from(), InstanceIndexKey::toRT), NncUtils.get(query.to(), InstanceIndexKey::toRT));
            return entries.stream().map(IndexEntryRT::getInstance)
                    .distinct()
                    .limit(Objects.requireNonNullElse(query.limit(), Long.MAX_VALUE))
                    .toList();
        }

        private Collection<IndexEntryRT> query(Index index, @Nullable IndexKeyRT from, @Nullable IndexKeyRT to) {
            if (from == null)
                from = new IndexKeyRT(index, NncUtils.toMap(index.getFields(), f -> f, f -> MIN_INSTANCE));
            if (to == null)
                to = new IndexKeyRT(index, NncUtils.toMap(index.getFields(), f -> f, f -> MAX_INSTANCE));
            return map.subSet(new IndexEntryRT(from, MIN_INSTANCE), true, new IndexEntryRT(to, MAX_INSTANCE), true);
        }

        public List<ClassInstance> scan(IndexKeyRT from, IndexKeyRT to) {
            return query(from.getIndex(), from, to).stream()
                    .map(IndexEntryRT::getInstance)
                    .distinct()
                    .toList();
        }

        public long count(IndexKeyRT from, IndexKeyRT to) {
            return query(from.getIndex(), from, to).stream().map(IndexEntryRT::getInstance).distinct().count();
        }

    }

}
