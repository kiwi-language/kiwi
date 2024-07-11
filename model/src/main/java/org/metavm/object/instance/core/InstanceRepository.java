package org.metavm.object.instance.core;

import org.metavm.object.instance.IndexKeyRT;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceRepository extends InstanceProvider {

    DurableInstance get(Id id);

    @Nullable
    InstanceReference selectFirstByKey(IndexKeyRT key);

    List<InstanceReference> indexScan(IndexKeyRT from, IndexKeyRT to);

    long indexCount(IndexKeyRT from, IndexKeyRT to);

    List<InstanceReference> indexSelect(IndexKeyRT key);

    boolean contains(Id id);

    void bind(DurableInstance instance);

    boolean remove(DurableInstance instance);

    List<Id> filterAlive(List<Id> ids);

}
