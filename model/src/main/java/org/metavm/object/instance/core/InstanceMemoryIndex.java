package org.metavm.object.instance.core;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.IndexEntryRT;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.Index;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.object.instance.IndexKeyRT.MAX_INSTANCE;
import static org.metavm.object.instance.IndexKeyRT.MIN_INSTANCE;

public class InstanceMemoryIndex {

    public static final Logger logger = LoggerFactory.getLogger(InstanceMemoryIndex.class);

    private final Map<Index, SubIndex> indexMap = new IdentityHashMap<>();
    private final Map<ClassInstance, List<IndexKeyRT>> keyMap = new IdentityHashMap<>();

    public InstanceMemoryIndex() {
    }

    public @Nullable ClassInstance selectUnique(IndexKeyRT key) {
        Utils.require(key.getIndex().isUnique());
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
        var keys = instance.getIndexKeys();
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
            Utils.require(index.isUnique());
            return Utils.first(select(key));
        }

        public void remove(IndexKeyRT key, ClassInstance instance) {
            map.remove(new IndexEntryRT(key, instance));
        }

        public void add(IndexKeyRT key, ClassInstance instance) {
            map.add(new IndexEntryRT(key, instance));
        }

        public List<ClassInstance> query(InstanceIndexQuery query) {
            var entries = query(query.index(), Utils.safeCall(query.from(), InstanceIndexKey::toRT), Utils.safeCall(query.to(), InstanceIndexKey::toRT));
            return entries.stream().map(IndexEntryRT::getInstance)
                    .distinct()
                    .limit(Objects.requireNonNullElse(query.limit(), Long.MAX_VALUE))
                    .toList();
        }

        private Collection<IndexEntryRT> query(Index index, @Nullable IndexKeyRT from, @Nullable IndexKeyRT to) {
            if (from == null)
                from = new IndexKeyRT(index, List.of(MIN_INSTANCE));
            if (to == null)
                to = new IndexKeyRT(index, List.of(MAX_INSTANCE));
            return map.subSet(new IndexEntryRT(from, (ClassInstance) MIN_INSTANCE.get()), true, new IndexEntryRT(to, (ClassInstance) MAX_INSTANCE.get()), true);
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
