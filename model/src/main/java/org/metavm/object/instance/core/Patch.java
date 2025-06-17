package org.metavm.object.instance.core;

import org.metavm.entity.EntityChange;
import org.metavm.entity.Tree;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionRT;

import java.util.Collection;
import java.util.List;

public record Patch(List<Tree> trees,
                    EntityChange<VersionRT> entityChange,
                    EntityChange<InstancePO> treeChanges,
                    Collection<Refcount> refcountChange
) {
}
