package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.RedirectStatusProvider;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceInput;
import org.metavm.util.Utils;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public class MockDefContext extends DefContext {

    private final Map<Id, Entity> entityMap = new HashMap<>();
    private final Set<Entity> entities = new HashSet<>();

    @Override
    public KlassDef<?> tryGetDef(TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KlassDef<?> getDef(Type javaType) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public KlassDef<?> getDefIfPresent(Type javaType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<KlassDef<?>> getAllDefList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Id getModelId(Object o) {
        return null;
    }

    @Override
    public boolean containsDef(TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Entity> entities() {
        return Collections.unmodifiableSet(entities);
    }

    @Override
    public Reference createReference(Id id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Instance> batchGetRoots(List<Long> treeIds) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Instance getBuffered(Id id) {
        return entityMap.get(id);
    }

    @Override
    public String getClientId() {
        return null;
    }

    @Override
    public Instance internalGet(Id id) {
        return entityMap.get(id);
    }

    @Override
    public boolean contains(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public Profiler getProfiler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Instance> batchGet(Collection<Id> ids) {
        return Utils.mapNonNull(ids, entityMap::get);
    }

    @Override
    public <T> List<T> getAllBufferedEntities(Class<T> entityClass) {
        return Utils.filterByType(entities, entityClass);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean containsIdSelf(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public <T> T getEntity(Class<T> entityType, Id id) {
        return entityType.cast(entityMap.get(id));
    }

    @Nullable
    @Override
    public EventQueue getEventQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceInput createInstanceInput(InputStream stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeout() {
        return 0;
    }

    @Override
    public void setTimeout(long timeout) {

    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void forceReindex(ClassInstance instance) {

    }

    @Override
    public Set<ClassInstance> getReindexSet() {
        return Set.of();
    }

    @Override
    public void forceSearchReindex(ClassInstance instance) {

    }

    @Override
    public Set<ClassInstance> getSearchReindexSet() {
        return Set.of();
    }

    @Override
    public long getAppId(Instance instance) {
        return 0;
    }

    @Override
    public long getAppId() {
        return 0;
    }

    @Override
    public void batchRemove(Collection<Instance> instances) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Instance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IInstanceContext createSame(long appId, TypeDefProvider typeDefProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Reference> selectByKey(IndexKeyRT indexKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Reference> query(InstanceIndexQuery query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count(InstanceIndexQuery query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsUniqueKey(IndexKeyRT key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void batchBind(Collection<Instance> instances) {
        instances.forEach(this::bind);
    }

    @Override
    public void registerCommitCallback(Runnable action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> E getAttribute(ContextAttributeKey<E> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void increaseVersionsForAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateMemoryIndex(ClassInstance instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long allocateTreeId() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Consumer<Object> getBindHook() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instance getRemoved(Id id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void addListener(ContextListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Value... refValues) {
        return List.of();
    }

    @Nullable
    @Override
    public <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        return null;
    }

    @Override
    public boolean containsUniqueKey(IndexDef<?> indexDef, Value... values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IInstanceContext createSame(long appId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLockMode(LockMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockMode getLockMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Id> filterAlive(List<Id> ids) {
        return Utils.filter(ids, this::containsId);
    }

    @Override
    public boolean isAlive(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public Instance get(Id id) {
        return entityMap.get(id);
    }

    @Override
    public <T extends Instance> T bind(T entity) {
        entityMap.put(entity.getId(), (Entity) entity);
        entities.add((Entity) entity);
        return entity;
    }

    @Override
    public List<Reference> indexScan(IndexKeyRT from, IndexKeyRT to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long indexCount(IndexKeyRT from, IndexKeyRT to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Reference> indexSelect(IndexKeyRT key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScanResult scan(long start, long limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadTree(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeDefProvider getTypeDefProvider() {
        return this;
    }

    @Override
    public RedirectStatusProvider getRedirectStatusProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsInstance(Instance instance) {
        return instance instanceof Entity && entities.contains(instance);
    }

    @Override
    public boolean containsId(Id id) {
        return entityMap.containsKey(id);
    }

    @Override
    public void buffer(Id id) {

    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        var it = entities.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Instance next() {
                return it.next();
            }
        };
    }
}
