package tech.metavm.object.instance.core.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInstanceRepository implements InstanceRepository {

    private final Map<Id, DurableInstance> instanceMap = new HashMap<>();
    private final IdentitySet<DurableInstance> instances = new IdentitySet<>();

    @Override
    public DurableInstance get(Id id) {
        return instanceMap.get(id);
    }

    @Nullable
    @Override
    public DurableInstance selectFirstByKey(IndexKeyRT key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassInstance> indexScan(IndexKeyRT from, IndexKeyRT to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        return 0;
    }

    @Override
    public List<ClassInstance> indexSelect(IndexKeyRT key) {
        return List.of();
    }

    @Override
    public boolean contains(Id id) {
        return instanceMap.containsKey(id);
    }

    @Override
    public void bind(DurableInstance instance) {
        var id = instance.tryGetId();
        if (id != null)
            instanceMap.put(id, instance);
    }

    @Override
    public boolean remove(DurableInstance instance) {
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
        return NncUtils.filter(ids, instanceMap::containsKey);
    }

}
