package tech.metavm.object.instance;

import tech.metavm.entity.VersionSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceVersion;
import tech.metavm.util.NncUtils;

import java.util.List;

public class StoreVersionSource implements VersionSource {

    private final IInstanceStore instanceStore;

    public StoreVersionSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<InstanceVersion> getRootVersions(List<Long> ids, IInstanceContext context) {
        return NncUtils.map(
                instanceStore.getRootVersions(ids, context),
                v -> new InstanceVersion(v.getId(), v.getVersion())
        );
    }
}
