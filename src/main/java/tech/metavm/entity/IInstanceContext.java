package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Consumer;

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

    boolean existsInstances(Type type);

    IEntityContext getEntityContext();

    boolean containsInstance(Instance instance);

    boolean containsId(long id);

    List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit);

    void preload(Collection<Long> ids, LoadingOption...options);

    void addRemovalListener(Consumer<Instance> removalListener);

    void finish();

    boolean isFinished();

    void initIds();

    long getTenantId();

    Type getType(long id);

    default ClassType getClassType(long id) {
        return (ClassType) getType(id);
    }

    void batchRemove(Collection<Instance> instances);

    boolean remove(Instance instance);

    List<Instance> selectByKey(IndexKeyPO indexKeyPO);

    List<Instance> query(InstanceIndexQuery query);

    default Instance selectByUniqueKey(IndexKeyPO key) {
        return NncUtils.getFirst(selectByKey(key));
    }

    void bind(Instance instance);

    <E> E getAttribute(ContextAttributeKey<E> key);
}
