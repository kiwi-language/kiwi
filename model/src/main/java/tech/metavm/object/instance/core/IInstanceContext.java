package tech.metavm.object.instance.core;

import tech.metavm.entity.ContextAttributeKey;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.InstanceSink;
import tech.metavm.entity.LockMode;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.NncUtils;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IInstanceContext extends InstanceSink, Closeable, InstanceRepository {

    IInstanceContext createSame(long appId);

    void setLockMode(LockMode mode);

    LockMode getLockMode();

    /*
     * Used to filter out dead ids from search result arsing from index rebuild delay
     */
    List<Id> filterAlive(List<Id> ids);

    boolean isAlive(Id id);

    DurableInstance get(Id id);

//    Instance get(RefDTO ref);

//    Instance get(InstanceId id);

    @Nullable
    DurableInstance getBuffered(Id id);

    String getClientId();

    DurableInstance internalGet(Id id);

//    Instance getIfPresentByTmpId(long tmpId);

    boolean contains(Id id);

    Profiler getProfiler();

    List<DurableInstance> batchGet(Collection<Id> ids);

    List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit);

    List<DurableInstance> scan(DurableInstance startExclusive, long limit);

    boolean existsInstances(Type type, boolean persistedOnly);

    ParameterizedFlowProvider getParameterizedFlowProvider();

    TypeProvider getTypeProvider();

    MappingProvider getMappingProvider();

    boolean containsInstance(DurableInstance instance);

    boolean containsId(Id id);

    List<DurableInstance> getByReferenceTargetId(long targetId, DurableInstance startExclusive, long limit);

    void buffer(Id id);

    default void buffer(Collection<? extends Id> ids) {
        ids.forEach(this::buffer);
    }

    void close();

    void finish();

    boolean isFinished();

    void initIds();

    long getAppId();

    Type getType(Id id);

    default ClassType getClassType(Id id) {
        return (ClassType) getType(id);
    }

    void batchRemove(Collection<DurableInstance> instances);

    boolean remove(DurableInstance instance);

    List<ClassInstance> selectByKey(IndexKeyRT indexKey);

    List<ClassInstance> query(InstanceIndexQuery query);

    long count(InstanceIndexQuery query);

    default DurableInstance selectFirstByKey(IndexKeyRT key) {
        return NncUtils.first(selectByKey(key));
    }

    default void bind(DurableInstance instance) {
        batchBind(List.of(instance));
    }

    void batchBind(Collection<DurableInstance> instances);

    void registerCommitCallback(Runnable action);

    <E> E getAttribute(ContextAttributeKey<E> key);

    void initIdManually(DurableInstance instance, Id id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    DurableInstance getRemoved(Id id);

    void invalidateCache(DurableInstance instance);

    @Nullable EventQueue getEventQueue();
}
