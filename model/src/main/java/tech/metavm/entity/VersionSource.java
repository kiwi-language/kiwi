package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceVersion;

import java.util.List;

public interface VersionSource {

    List<InstanceVersion> getRootVersions(List<String> ids, IInstanceContext context);

}
