package tech.metavm.object.instance;

import tech.metavm.entity.VersionSource;
import tech.metavm.object.instance.core.*;
import tech.metavm.util.NncUtils;

import java.util.List;

public class StoreVersionSource implements VersionSource {

    private final IInstanceStore instanceStore;

    public StoreVersionSource(IInstanceStore instanceStore) {
        this.instanceStore = instanceStore;
    }

    @Override
    public List<InstanceVersion> getRootVersions(List<String> ids, IInstanceContext context) {
        return NncUtils.map(
                instanceStore.getRootVersions(NncUtils.map(ids, Id::parse), context),
                v -> new InstanceVersion(v.getInstanceId(), v.getVersion())
        );
    }
}
