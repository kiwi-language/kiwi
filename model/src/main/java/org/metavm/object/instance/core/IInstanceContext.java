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
import org.metavm.util.InstanceInput;
import org.metavm.util.Utils;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
        return Utils.first(batchGetRoots(List.of(treeId)));
    }

    @Nullable
    Instance getBuffered(Id id);

    String getClientId();

    Instance internalGet(Id id);

    boolean contains(Id id);

    Profiler getProfiler();

    List<Instance> batchGet(Collection<Id> ids);

    ScanResult scan(long start, long limit);

    @Override
    default InstanceRepository instanceRepository() {
        return this;
    }

    void loadTree(long id);

    default boolean isReferenced(Instance instance) {
        if(instance instanceof MvInstance mvInst && mvInst.isReferencedByParent())
            return true;
        if(instance.tryGetId() instanceof PhysicalId id)
            return !getByReferenceTargetId(id, 0, 1).isEmpty();
        // TODO handle in-memory references
        return false;
    }

    TypeDefProvider getTypeDefProvider();

    RedirectStatusProvider getRedirectStatusProvider();

    boolean containsInstance(Instance instance);

    boolean containsId(Id id);

    List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit);

    List<Instance> getRelocated();

    void buffer(Id id);

    void removeForwardingPointer(MvInstance instance, boolean clearingOldId);

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
        return Utils.first(selectByKey(key));
    }

    default <T extends Instance> T bind(T instance) {
        batchBind(List.of(instance));
        return instance;
    }

    void batchBind(Collection<Instance> instances);

    void registerCommitCallback(Runnable action);

    <E> E getAttribute(ContextAttributeKey<E> key);

    void initIdManually(Instance instance, Id id);

    void increaseVersionsForAll();

    void updateMemoryIndex(ClassInstance instance);

    @Nullable Consumer<Object> getBindHook();

    Instance getRemoved(Id id);

    @Nullable EventQueue getEventQueue();

    InstanceInput createInstanceInput(InputStream stream);

    long getTimeout();

    void setTimeout(long timeout);

    String getDescription();

    void setDescription(String description);

    void forceReindex(ClassInstance instance);

    Set<ClassInstance> getReindexSet();

    void forceSearchReindex(ClassInstance instance);

    Set<ClassInstance> getSearchReindexSet();

    long getAppId(Instance instance);

}
