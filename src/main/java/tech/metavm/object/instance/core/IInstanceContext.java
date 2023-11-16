package tech.metavm.object.instance.core;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Profiler;

import javax.annotation.Nullable;
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

    Profiler getProfiler();

    List<Instance> batchGet(Collection<Long> ids);

    List<Instance> getByType(Type type, Instance startExclusive, long limit);

    List<Instance> scan(Instance startExclusive, long limit);

    boolean existsInstances(Type type, boolean persistedOnly);

    IEntityContext getEntityContext();

    boolean containsInstance(Instance instance);

    boolean containsId(long id);

    List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit);

    void preload(Collection<Long> ids, LoadingOption...options);

    void addRemovalListener(Consumer<Instance> listener);

    void addInitializationListener(Consumer<Instance> listener);

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

    boolean isPersistedInstance(Instance instance);

    void initIdManually(Instance instance, long id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    Instance getRemoved(long id);
}
