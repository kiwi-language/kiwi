package tech.metavm.object.instance.core;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

public interface IInstanceContext extends InstanceSink, Closeable {

    IInstanceContext createSame(long tenantId);

    void setLockMode(LockMode mode);

    LockMode getLockMode();

    default void replace(Instance instance) {
        replace(List.of(instance));
    }

    void replace(Collection<Instance> instances);

    /*
     * Used to filter out dead ids from search result arsing from index rebuild delay
     */
    List<Long> filterAlive(List<Long> ids);

    boolean isAlive(long id);

    Instance get(long id);

    Instance get(RefDTO ref);

    @Nullable Instance getBuffered(long id);

    String getClientId();

    Instance internalGet(long id);

    Instance getIfPresentByTmpId(long tmpId);

    void withCache(Instance instance);

    boolean containsRef(RefDTO ref);

    Profiler getProfiler();

    List<Instance> batchGet(Collection<Long> ids);

    List<Instance> getByType(Type type, Instance startExclusive, long limit);

    List<Instance> scan(Instance startExclusive, long limit);

    boolean existsInstances(Type type, boolean persistedOnly);

    IEntityContext getEntityContext();

    boolean containsInstance(Instance instance);

    boolean containsId(long id);

    List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit);

    void buffer(long id);

    default void buffer(Collection<Long> ids) {
        ids.forEach(this::buffer);
    }

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

    void initIdManually(Instance instance, long id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    Instance getRemoved(long id);

    void invalidateCache(Instance instance);
}
