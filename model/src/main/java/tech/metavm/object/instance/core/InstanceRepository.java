package tech.metavm.object.instance.core;

import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.IndexKeyRT;

import javax.annotation.Nullable;
import java.util.List;

public interface InstanceRepository extends InstanceProvider {

    DurableInstance get(Id id);

    @Nullable DurableInstance selectFirstByKey(IndexKeyRT key);

    List<ClassInstance> indexScan(IndexKeyRT from, IndexKeyRT to);

    long indexCount(IndexKeyRT from, IndexKeyRT to);

    List<ClassInstance> indexSelect(IndexKeyRT key);

    boolean contains(Id id);

    void bind(DurableInstance instance);

    boolean remove(DurableInstance instance);

    List<Id> filterAlive(List<Id> ids);

}
