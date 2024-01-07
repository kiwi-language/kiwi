package tech.metavm.object.instance.core;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.Index;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

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
        if(oldKeys != null) {
            for (IndexKeyRT oldKey : oldKeys) {
                indexMap.get(oldKey.getIndex()).remove(oldKey, instance);
            }
        }
    }

    private class SubIndex {
        private final Index index;
        private final Map<IndexKeyRT, IdentitySet<ClassInstance>> map = new HashMap<>();

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
            var instances = map.get(key);
            if (instances != null)
                instances.remove(instance);
        }

        public void add(IndexKeyRT key, ClassInstance instance) {
            map.computeIfAbsent(key, k -> new IdentitySet<>()).add(instance);
        }

        public List<ClassInstance> query(InstanceIndexQuery query) {
            var matches = new IdentitySet<ClassInstance>();
            for (var instances : map.values()) {
                for (var instance : instances) {
                    if (query.matches(instance, parameterizedFlowProvider))
                        matches.add(instance);
                }
            }
            return Instances.sortAndLimit(new ArrayList<>(matches), query.desc(), NncUtils.orElse(query.limit(), -1L));
        }

    }

}
