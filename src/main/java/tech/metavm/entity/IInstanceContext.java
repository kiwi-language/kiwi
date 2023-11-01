package tech.metavm.entity;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

public interface IInstanceContext extends InstanceSink, Closeable {

    IInstanceContext newContext(long tenantId);

    void setLockMode(LockMode mode);

    LockMode getLockMode();

    default void replace(Instance instance) {
        replace(List.of(instance));
    }

    void replace(Collection<Instance> instances);

    default Instance get(long id) {
        return NncUtils.getFirst(batchGet(List.of(id)));
    }

    Instance get(RefDTO ref);

    List<Instance> batchGet(Collection<Long> ids);

    List<Instance> getByType(Type type, Instance startExclusive, long limit);

    List<Instance> scan(Instance startExclusive, long limit);

    boolean existsInstances(Type type);

    IEntityContext getEntityContext();

    boolean containsInstance(Instance instance);

    boolean containsId(long id);

    List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit);

    void preload(Collection<Long> ids, LoadingOption...options);

    void addRemovalListener(Consumer<Instance> removalListener);

    void close();

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

    List<Instance> selectByKey(IndexKeyRT indexKey);

    List<Instance> query(InstanceIndexQuery query);

    default Instance selectByUniqueKey(IndexKeyRT key) {
        return NncUtils.getFirst(selectByKey(key));
    }

    void bind(Instance instance);

    <E> E getAttribute(ContextAttributeKey<E> key);

    boolean isNewInstance(Instance instance);

    void initIdManually(Instance instance, long id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);
}
