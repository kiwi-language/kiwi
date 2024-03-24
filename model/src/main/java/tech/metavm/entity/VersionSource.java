package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.TreeVersion;

import java.util.List;

public interface VersionSource {

    List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context);

}
