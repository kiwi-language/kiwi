package org.manul.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.manul.entity.InstanceIndexQuery;
import org.manul.object.instance.IInstanceStore;
import org.manul.object.instance.IndexKeyRT;
import org.manul.object.instance.IndexSource;

import java.util.List;

public class StoreIndexSource implements IndexSource {

    private final IInstanceStore instanceStore;

    public StoreIndexSource(@NotNull IInstanceStore instanceStore) {
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
