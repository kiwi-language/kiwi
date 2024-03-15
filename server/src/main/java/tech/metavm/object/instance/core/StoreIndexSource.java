package tech.metavm.object.instance.core;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;

import java.util.List;

public class StoreIndexSource implements IndexSource {

    private final IInstanceStore instanceStore;

    public StoreIndexSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        return instanceStore.query(query, context);
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        return instanceStore.count(query, context);
    }

    @Override
    public long count(IndexKeyRT from, IndexKeyRT to, IInstanceContext context) {
        return instanceStore.indexCount(from.toPO(), to.toPO(), context);
    }

    @Override
    public List<Id> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context) {
        return instanceStore.indexScan(from.toPO(), to.toPO(), context);
    }

}
