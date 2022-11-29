package tech.metavm.object.instance;

import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.ChangeList;

import java.util.Collection;
import java.util.List;

public interface IInstanceStore {

    void save(ChangeList<InstancePO> diff);

    List<Instance> selectByKey(IndexKeyPO key, InstanceContext context);

    List<InstancePO> load(StoreLoadRequest request, InstanceContext context);

    List<InstancePO> getByTypeIds(Collection<Long> typeIds, InstanceContext context);

}
