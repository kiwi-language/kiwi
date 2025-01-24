package org.metavm.entity;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;

public interface EntityRepository extends EntityProvider {

    <T extends Instance> T bind(T entity);

    void updateMemoryIndex(ClassInstance entity);
}
