package tech.metavm.autograph;

import tech.metavm.entity.VersionSource;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceVersion;
import tech.metavm.object.instance.rest.InstanceVersionDTO;
import tech.metavm.object.instance.rest.InstanceVersionsRequest;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.util.List;

public class ServerVersionSource implements VersionSource {

    @Override
    public List<InstanceVersion> getRootVersions(List<Long> ids, IInstanceContext context) {
        var versions = HttpUtils.post("/instance/versions", new InstanceVersionsRequest(ids),
                new TypeReference<List<InstanceVersionDTO>>() {
                });
        return NncUtils.map(
                versions,
                v -> new InstanceVersion(v.id(), v.version())
        );
    }

}
