package org.manul.object.instance.core;

import org.manul.entity.EntityChange;
import org.manul.entity.Tree;
import org.manul.object.instance.persistence.InstancePO;
import org.manul.object.instance.persistence.VersionRT;

import java.util.Collection;
import java.util.List;

public record Patch(List<Tree> trees,
                    EntityChange<VersionRT> entityChange,
                    EntityChange<InstancePO> treeChanges,
                    Collection<Refcount> refcountChange
) {
}
