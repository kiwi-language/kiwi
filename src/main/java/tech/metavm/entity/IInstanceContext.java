package tech.metavm.entity;

import tech.metavm.object.instance.Instance;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IInstanceContext {
    List<Instance> batchGet(Collection<Long> ids,
                            LoadingOption firstOption, LoadingOption... restOptions);

    List<Instance> batchGet(Collection<Long> ids, Set<LoadingOption> options);


    IEntityContext getEntityContext();

    boolean containsId(long id);

    void finish();
}
