package org.metavm.object.instance.core;

import org.metavm.entity.ContextAttributeKey;
import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.InstanceSink;
import org.metavm.entity.LockMode;
import org.metavm.entity.natives.CallContext;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.InstanceInput;
import org.metavm.util.NncUtils;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IInstanceContext extends InstanceSink, Closeable, InstanceRepository, CallContext, Iterable<DurableInstance> {

    IInstanceContext createSame(long appId);

    void setLockMode(LockMode mode);

    LockMode getLockMode();

    /*
     * Used to filter out dead ids from search result arsing from index rebuild delay
     */
    List<Id> filterAlive(List<Id> ids);

    boolean isAlive(Id id);

    DurableInstance get(Id id);

    InstanceReference createReference(Id id);

    List<DurableInstance> batchGetRoots(List<Long> treeIds);

    default DurableInstance getRoot(long treeId) {
        return NncUtils.first(batchGetRoots(List.of(treeId)));
    }

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

//    List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit);

//    List<DurableInstance> scan(DurableInstance startExclusive, long limit);

//    boolean existsInstances(Type type, boolean persistedOnly);

    List<InstanceReference> scan(long start, long limit);

    @Override
    default InstanceRepository instanceRepository() {
        return this;
    }

//    TypeProvider getTypeProvider();

    TypeDefProvider getTypeDefProvider();

    MappingProvider getMappingProvider();

    boolean containsInstance(DurableInstance instance);

    boolean containsId(Id id);

    List<DurableInstance> getByReferenceTargetId(Id targetId, long startExclusive, long limit);

    List<DurableInstance> getMigrated();

    void buffer(Id id);

    void removeForwardingPointer(DurableInstance instance);

    default void buffer(Collection<? extends Id> ids) {
        ids.forEach(this::buffer);
    }

    void close();

    boolean containsIdSelf(Id id);

    void finish();

    boolean isFinished();

    void initIds();

    long getAppId();

    void batchRemove(Collection<DurableInstance> instances);

    boolean remove(DurableInstance instance);

    List<InstanceReference> selectByKey(IndexKeyRT indexKey);

    List<InstanceReference> query(InstanceIndexQuery query);

    long count(InstanceIndexQuery query);

    default InstanceReference selectFirstByKey(IndexKeyRT key) {
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

    void invalidateCache(InstanceReference instance);

    @Nullable EventQueue getEventQueue();

    InstanceInput createInstanceInput(InputStream stream);

}
