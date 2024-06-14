package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.TreeVersion;

import java.util.List;

public interface VersionSource {

    List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context);

}
