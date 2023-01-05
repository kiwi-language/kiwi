package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.*;

public interface IInstanceContext extends InstanceSink {

    default void replace(Instance instance) {
        replace(List.of(instance));
    }

    void replace(Collection<Instance> instances);

    default Instance get(long id) {
        return NncUtils.getFirst(batchGet(List.of(id)));
    }

    List<Instance> batchGet(Collection<Long> ids);

    List<Instance> getByType(Type type, Instance startExclusive, long limit);

    IEntityContext getEntityContext();

    boolean containsInstance(Instance instance);

    boolean containsId(long id);

    void preload(Collection<Long> ids, LoadingOption...options);

    void finish();

    boolean isFinished();

    void initIds();

    long getTenantId();

    Type getType(long id);

    default ClassType getClassType(long id) {
        return (ClassType) getType(id);
    }

    boolean remove(Instance instance);

    List<Instance> selectByKey(IndexKeyPO indexKeyPO);

    default Instance selectByUniqueKey(IndexKeyPO key) {
        return NncUtils.getFirst(selectByKey(key));
    }

    void bind(Instance instance);

    <E> E getAttribute(ContextAttributeKey<E> key);
}
