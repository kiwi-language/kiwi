package org.metavm.object.instance.core;

import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceRepository extends InstanceProvider {

    Instance get(Id id);

    @Nullable
    Reference selectFirstByKey(IndexKeyRT key);

    @Nullable
    Reference selectLastByKey(IndexKeyRT key);

    List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to);

    long indexCount(IndexKeyRT from, IndexKeyRT to);

    List<Reference> indexSelect(IndexKeyRT key);

    boolean contains(Id id);

    <T extends Instance> T bind(T instance);

    void updateMemoryIndex(ClassInstance instance);

    boolean remove(Instance instance);

    List<Id> filterAlive(List<Id> ids);

    long allocateTreeId();

    default Id allocateRootId() {
        return PhysicalId.of(allocateTreeId(), 0L);
    }

    default Id allocateRootId(ClassType type) {
        if (type.isValueType()) return null;
        else if (type.isEphemeral()) return TmpId.random();
        else return allocateRootId();
    }

}
