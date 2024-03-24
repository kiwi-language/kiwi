package tech.metavm.object.instance;

import tech.metavm.entity.VersionSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.TreeVersion;

import java.util.List;

public class StoreVersionSource implements VersionSource {

    private final IInstanceStore instanceStore;

    public StoreVersionSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        return instanceStore.getVersions(ids, context);
    }
}
