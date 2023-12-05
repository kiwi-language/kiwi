package tech.metavm.object.instance.core;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
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

    IInstanceContext createSame(long appId);

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

    void close();

    void finish();

    boolean isFinished();

    void initIds();

    long getAppId();

    Type getType(long id);

    default ClassType getClassType(long id) {
        return (ClassType) getType(id);
    }

    void batchRemove(Collection<Instance> instances);

    boolean remove(Instance instance);

    List<Instance> selectByKey(IndexKeyRT indexKey);

    List<Instance> query(InstanceIndexQuery query);

    long count(InstanceIndexQuery query);

    default Instance selectByUniqueKey(IndexKeyRT key) {
        return NncUtils.first(selectByKey(key));
    }

    void bind(Instance instance);

    void registerCommitCallback(Runnable action);

    <E> E getAttribute(ContextAttributeKey<E> key);

    void initIdManually(Instance instance, long id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    Instance getRemoved(long id);

    void invalidateCache(Instance instance);

    @Nullable EventQueue getEventQueue();
}
