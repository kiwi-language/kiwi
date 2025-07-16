package org.metavm.object.instance.core;

import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.event.EventQueue;
import org.metavm.flow.Code;
import org.metavm.flow.Flow;
import org.metavm.flow.Function;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public interface IInstanceContext extends InstanceSink, Closeable, InstanceRepository, CallContext, Iterable<Instance>, TypeDefProvider, RedirectStatusProvider, EntityRepository {

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

    TypeDefProvider getTypeDefProvider();

    RedirectStatusProvider getRedirectStatusProvider();

    boolean containsInstance(Instance instance);

    boolean containsId(Id id);

    void buffer(Id id);

    default void buffer(Collection<? extends Id> ids) {
        ids.forEach(this::buffer);
    }

    void close();

    boolean containsIdSelf(Id id);

    void finish();

    boolean isFinished();

    long getAppId();

    void batchRemove(Collection<Instance> instances);

    boolean remove(Instance instance);

    IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider);

    boolean isMigrating();

    List<Reference> selectByKey(IndexKeyRT indexKey);

    List<Reference> query(InstanceIndexQuery query);

    long count(InstanceIndexQuery query);

    boolean containsUniqueKey(IndexKeyRT key);

    default <T extends Instance> T bind(T instance) {
        batchBind(List.of(instance));
        return instance;
    }

    void batchBind(Collection<Instance> instances);

    void registerCommitCallback(Runnable action);

    <E> E getAttribute(ContextAttributeKey<E> key);

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

    <T> List<T> getAllBufferedEntities(Class<T> entityClass);

    default <T> T getEntity(Class<T> clazz, String id) {
        return clazz.cast(get(Id.parse(id)));
    }

    default <T> T getEntity(Class<T> clazz, Id id) {
        return clazz.cast(get(id));
    }

    default Flow getFlow(Id id) {
        return getEntity(Flow.class, id);
    }

    default Flow getFlow(String id) {
        return getEntity(Flow.class, id);
    }

    default Code getCode(Id id) {
        return getEntity(Code.class, id);
    }

    default Code getCode(String id) {
        return getEntity(Code.class, id);
    }

    default Method getMethod(Id id) {
        return getEntity(Method.class, id);
    }

    default Method getMethod(String id) {
        return getEntity(Method.class, id);
    }

    default Function getFunction(Id id) {
        return getEntity(Function.class, id);
    }

    default Function getFunction(String id) {
        return getEntity(Function.class, id);
    }


    default ITypeDef getTypeDef(String id) {
        return getEntity(ITypeDef.class, id);
    }

    @Override
    default ITypeDef getTypeDef(Id id) {
        return getEntity(ITypeDef.class, id);
    }

    @Override
    default RedirectStatus getRedirectStatus(Id id) {
        return getEntity(RedirectStatus.class, id);
    }



    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef,
                                           Value... values);

    @Nullable
    <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values);

    default Field getField(String id) {
        return getEntity(Field.class, id);
    }

    default Field getField(Id id) {
        return getEntity(Field.class, id);
    }

    default List<Klass> loadKlasses() {
        return Klasses.loadKlasses(this);
    }

    <T extends Entity> List<T> query(EntityIndexQuery<T> query);

    long count(EntityIndexQuery<?> query);

    boolean containsUniqueKey(IndexDef<?> indexDef, Value... values);

    default @Nullable Klass findKlassByQualifiedName(String qualifiedName) {
        return selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(qualifiedName));
    }

    default Klass getKlassByQualifiedName(String qualifiedName) {
        var klass = findKlassByQualifiedName(qualifiedName);
        if (klass == null)
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, qualifiedName);
        return klass;
    }

    default Commit getCommit(String id) {
        return getEntity(Commit.class, id);
    }

    @Override
    default Id allocateRootId(ClassType type) {
        return EntityRepository.super.allocateRootId(type);
    }

    @Override
    default Id allocateRootId() {
        return InstanceRepository.super.allocateRootId();
    }

    void dumpContext();
}
