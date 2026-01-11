package org.manul.entity;

import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.TreeVersion;

import java.util.List;

public interface VersionSource {

    List<TreeVersion> getVersions(List<Long> ids, IInstanceContext context);

}
