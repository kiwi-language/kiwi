package tech.metavm.object.instance.core;

import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.IndexKeyRT;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceRepository extends InstanceProvider {

    DurableInstance get(Id id);

    @Nullable DurableInstance selectFirstByKey(IndexKeyRT key);

    boolean contains(Id id);

    void bind(DurableInstance instance);

    boolean remove(DurableInstance instance);

    List<Long> filterAlive(List<Long> ids);

    default DurableInstance get(RefDTO ref) {
        if (ref.isEmpty())
            return null;
        var id = ref.toId();
        if (id.tryGetPhysicalId() == null && !contains(id))
            return null;
        return get(id);
    }

}
