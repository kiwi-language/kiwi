package org.metavm.object.instance.core.mocks;

import org.jetbrains.annotations.Nullable;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceRepository;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.IdentitySet;
import org.metavm.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInstanceRepository implements InstanceRepository {

    private int nextTreeId = 1000000;

    private final Map<Id, Instance> instanceMap = new HashMap<>();
    private final IdentitySet<Instance> instances = new IdentitySet<>();

    @Override
    public Instance get(Id id) {
        return instanceMap.get(id);
    }

    @Override
    public Reference selectFirstByKey(IndexKeyRT key) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Reference selectLastByKey(IndexKeyRT key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return 0;
    }

    @Override
    public List<Reference> indexSelect(IndexKeyRT key) {
        return List.of();
    }

    @Override
    public boolean contains(Id id) {
        return instanceMap.containsKey(id);
    }

    @Override
    public <T extends Instance> T bind(T instance) {
        var id = instance.tryGetId();
        if (id != null)
            instanceMap.put(id, instance);
        return instance;
    }

    @Override
    public boolean remove(Instance instance) {
        var removed = instances.remove(instance);
        if (removed) {
            var id = instance.tryGetId();
            if (id != null)
                instanceMap.remove(id);
            return true;
        } else
            return false;
    }

    @Override
    public List<Id> filterAlive(List<Id> ids) {
        return Utils.filter(ids, instanceMap::containsKey);
    }

    @Override
    public long allocateTreeId() {
        return nextTreeId++;
    }

}
