package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceVersion;
import tech.metavm.object.version.Version;

import java.util.List;

public interface VersionSource {

    List<InstanceVersion> getRootVersions(List<Long> ids, IInstanceContext context);

}
