package org.metavm.entity;

import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;

public interface EntityRepository extends EntityProvider {

    <T extends Instance> T bind(T entity);

    void updateMemoryIndex(ClassInstance entity);

    long allocateTreeId();

    default Id allocateRootId() {
        return PhysicalId.of(allocateTreeId(), 0L);
    }

    default Id allocateRootId(ClassType type) {
        if (type.isValueType()) return null;
        else if (type.isEphemeral()) return TmpId.random();
        else return allocateRootId();
    }

    boolean remove(Instance instance);
}