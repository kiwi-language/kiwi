package org.metavm.entity;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PhysicalId;

public interface EntityRepository extends EntityProvider {

    <T extends Instance> T bind(T entity);

    void updateMemoryIndex(ClassInstance entity);

    long allocateTreeId();

    default Id allocateRootId() {
        return PhysicalId.of(allocateTreeId(), 0L);
    }

}
