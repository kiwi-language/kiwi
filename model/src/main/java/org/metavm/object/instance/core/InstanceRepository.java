package org.metavm.object.instance.core;

import org.metavm.object.instance.IndexKeyRT;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceRepository extends InstanceProvider {

    Instance get(Id id);

    @Nullable
    Reference selectFirstByKey(IndexKeyRT key);

    List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to);

    long indexCount(IndexKeyRT from, IndexKeyRT to);

    List<Reference> indexSelect(IndexKeyRT key);

    boolean contains(Id id);

    <T extends Instance> T bind(T instance);

    boolean remove(Instance instance);

    List<Id> filterAlive(List<Id> ids);

}
