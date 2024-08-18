package org.metavm.object.instance.core;

import org.metavm.entity.ContextAttributeKey;
import org.metavm.entity.InstanceIndexQuery;
import org.metavm.entity.InstanceSink;
import org.metavm.entity.LockMode;
import org.metavm.entity.natives.CallContext;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.RedirectStatusProvider;
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

public interface IInstanceContext extends InstanceSink, Closeable, InstanceRepository, CallContext, Iterable<Instance> {

    IInstanceContext createSame(long appId);

    void setLockMode(LockMode mode);

    LockMode getLockMode();

    /*
     * Used to filter out dead ids from search result arsing from index rebuild delay
     */
    List<Id> filterAlive(List<Id> ids);

    boolean isAlive(Id id);

    Instance get(Id id);

    Reference createReference(Id id);

    List<Instance> batchGetRoots(List<Long> treeIds);

    default Instance getRoot(long treeId) {
        return NncUtils.first(batchGetRoots(List.of(treeId)));
    }

//    Instance get(RefDTO ref);

//    Instance get(InstanceId id);

    @Nullable
    Instance getBuffered(Id id);

    String getClientId();

    Instance internalGet(Id id);

//    Instance getIfPresentByTmpId(long tmpId);

    boolean contains(Id id);

    Profiler getProfiler();

    List<Instance> batchGet(Collection<Id> ids);

//    List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit);

//    List<DurableInstance> scan(DurableInstance startExclusive, long limit);

//    boolean existsInstances(Type type, boolean persistedOnly);

    ScanResult scan(long start, long limit);

    @Override
    default InstanceRepository instanceRepository() {
        return this;
    }

//    TypeProvider getTypeProvider();

    void loadTree(long id);

    default boolean isReferenced(Instance instance) {
        if(instance.isReferencedByParent())
            return true;
        if(instance.tryGetId() instanceof PhysicalId id)
            return !getByReferenceTargetId(id, 0, 1).isEmpty();
        // TODO handle in-memory references
        return false;
    }

    TypeDefProvider getTypeDefProvider();

    MappingProvider getMappingProvider();

    RedirectStatusProvider getRedirectStatusProvider();

    boolean containsInstance(Instance instance);

    boolean containsId(Id id);

    List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit);

    List<Instance> getRelocated();

    void buffer(Id id);

    void removeForwardingPointer(Instance instance, boolean clearingOldId);

    default void buffer(Collection<? extends Id> ids) {
        ids.forEach(this::buffer);
    }

    void close();

    boolean containsIdSelf(Id id);

    void finish();

    boolean isFinished();

    void initIds();

    long getAppId();

    void batchRemove(Collection<Instance> instances);

    boolean remove(Instance instance);

    IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider);

    List<Reference> selectByKey(IndexKeyRT indexKey);

    List<Reference> query(InstanceIndexQuery query);

    long count(InstanceIndexQuery query);

    boolean containsUniqueKey(IndexKeyRT key);

    default Reference selectFirstByKey(IndexKeyRT key) {
        return NncUtils.first(selectByKey(key));
    }

    default void bind(Instance instance) {
        batchBind(List.of(instance));
    }

    void batchBind(Collection<Instance> instances);

    void registerCommitCallback(Runnable action);

    <E> E getAttribute(ContextAttributeKey<E> key);

    void initIdManually(Instance instance, Id id);

    void increaseVersionsForAll();

    void evict(Instance instance);

    void pubBack(Instance instance);

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    Instance getRemoved(Id id);

    void invalidateCache(Reference instance);

    @Nullable EventQueue getEventQueue();

    InstanceInput createInstanceInput(InputStream stream);

    long getTimeout();

    void setTimeout(long timeout);
}
