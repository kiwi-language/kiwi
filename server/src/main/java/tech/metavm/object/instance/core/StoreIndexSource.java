package tech.metavm.object.instance.core;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.NncUtils;

import java.util.List;

public class StoreIndexSource implements IndexSource {

    private final IInstanceStore instanceStore;

    public StoreIndexSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<Long> query(InstanceIndexQuery query, IInstanceContext context) {
        return instanceStore.query(query, context);
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        return instanceStore.count(query, context);
    }

    @Override
    public List<Long> queryByType(long typeId, long startId, long limit, IInstanceContext context) {
        return NncUtils.map(
                instanceStore.queryByTypeIds(List.of(new ByTypeQuery(typeId, startId, limit)), context),
                InstancePO::getId
        );
    }
}
