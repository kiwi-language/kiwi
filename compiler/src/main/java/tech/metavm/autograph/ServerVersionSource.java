package tech.metavm.autograph;

import tech.metavm.entity.VersionSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.TreeVersion;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ServerVersionSource implements VersionSource {

    private final TypeClient typeClient;

    public ServerVersionSource(TypeClient typeClient) {
        this.typeClient = typeClient;
    }

    @Override
    public List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context) {
        var versions = typeClient.getVersions(
                new InstanceVersionsRequest(ids));
        return NncUtils.map(
                versions,
                v -> new TreeVersion(v.id(), v.version())
        );
    }

}
