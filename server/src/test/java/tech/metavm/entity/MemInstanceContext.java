package tech.metavm.entity;

import tech.metavm.application.Application;
import tech.metavm.event.EventQueue;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.object.type.Type;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.TestConstants.APP_ID;

public class MemInstanceContext extends BufferingInstanceContext {

//    protected final EntityIdProvider idService;
    private final Map<LoadByTypeRequest, List<InstancePO>> loadByTypeCache = new HashMap<>();
    private boolean finished;
    private IEntityContext entityContext;
    private @Nullable Function<Long, Type> typeProvider;
    private final EventQueue eventQueue = new MockEventQueue();
    private final IInstanceStore instanceStore;

    public MemInstanceContext() {
        this(APP_ID, new MockIdProvider(), new MemInstanceStore(), null);
    }

    public MemInstanceContext(long appId,
                              EntityIdProvider idProvider,
                              IInstanceStore instanceStore,
                              IInstanceContext parent) {

        super(appId,
                List.of(new StoreTreeSource(instanceStore)),
                new StoreVersionSource(instanceStore),
                idProvider,
                new StoreIndexSource(instanceStore)
                , MockRegistry.getDefContext(), parent, false);
//        super(appId, instanceStore,
//
//                MockRegistry.getDefContext(), parent, false);
        typeProvider = typeId -> getEntityContext().getType(typeId);
        this.instanceStore = instanceStore;
        setBindHook(job -> getEntityContext().bind(job));
    }

    public MemInstanceContext initData(Collection<Instance> instances) {
        replace(instances);
        return this;
    }

    public void setTypeProvider(@Nullable Function<Long, Type> typeProvider) {
        this.typeProvider = typeProvider;
    }

    @Override
    protected Instance allocateInstance(long id) {
        throw new InternalException("Can not find instance for id " + id);
    }

    @Override
    public IEntityContext getEntityContext() {
        return entityContext;
    }

    @Override
    public void buffer(long id) {

    }

    @Override
    protected void finishInternal() {
        NncUtils.requireFalse(finished, "already finished");
        initIds();
        ChangeList<InstancePO> changeList = ChangeList.inserts(
                NncUtils.map(this, instance -> PersistenceUtils.toInstancePO(instance, ROOT_APP_ID))
        );
        instanceStore.save(changeList);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Type getType(long id) {
        NncUtils.requireNonNull(typeProvider, "typeProvider not set");
        return typeProvider.apply(id);
    }

    @Override
    public void invalidateCache(Instance instance) {
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public EventQueue getEventQueue() {
        return eventQueue;
    }

    @Override
    public IInstanceContext createSame(long appId) {
        return new MemInstanceContext(
                appId, idProvider, instanceStore, getParent()
        );
    }

    @Override
    protected boolean checkAliveInStore(long id) {
        return !instanceStore.load(StoreLoadRequest.create(id), this).isEmpty();
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.getByReferenceTargetId(
                        targetId,
                        NncUtils.getOrElse(startExclusive, Instance::getId, -1L),
                        limit,
                        this
                ),
                this::get
        );
    }

    public void initIds() {
        try (var ignored = getProfiler().enter("initIds")) {
            Function<Map<Type, Integer>, Map<Type, List<Long>>> idGenerator = getIdGenerator();
            List<Instance> instancesToInitId = NncUtils.filter(this, Instance::isIdNull);
            if (instancesToInitId.isEmpty()) {
                return;
            }
            Map<Type, Integer> countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
            Map<Type, List<Long>> idMap = idGenerator.apply(countMap);
            Map<Type, List<Instance>> type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
            Map<Long, Instance> allocatedMap = new HashMap<>();
            type2instances.forEach((type, instances) -> {
                List<Long> ids = idMap.get(type);
                for (Long id : ids) {
                    boolean contains1 = allocatedMap.containsKey(id);
                    if (contains1) {
                        throw new InternalException();
                    }
                    boolean contains = containsId(id);
                    if (contains) {
                        throw new InternalException();
                    }
                }
                for (Instance instance : instances) {
                    allocatedMap.put(instance.getId(), instance);
                }
                NncUtils.biForEach(instances, ids, Instance::initId);
            });
            for (Instance instance : instancesToInitId) {
                onIdInitialized(instance);
            }
        }
    }

    private Long interceptGetTypeId(long id) {
        return id == Constants.ROOT_APP_ID || id == Constants.PLATFORM_APP_ID ?
                getDefContext().getType(Application.class).getIdRequired() : null;
    }

    private Function<Map<Type, Integer>, Map<Type, List<Long>>> getIdGenerator() {
        return (typeId2count) -> idProvider.allocate(appId, typeId2count);
    }

    private List<InstancePO> loadByType(LoadByTypeRequest request) {
        var cachedResult = loadByTypeCache.get(request);
        if (cachedResult != null)
            return cachedResult;
        var result = instanceStore.queryByTypeIds(
                List.of(
                        new ByTypeQuery(
                                NncUtils.requireNonNull(request.type().getId(), "Type id is not initialized"),
                                request.startExclusive().isNull() ? -1L : request.startExclusive().getIdRequired() + 1L,
                                request.limit()
                        )
                ),
                this
        );
        loadByTypeCache.put(request, result);
        return result;
    }

    private List<Instance> getByType(Type type, Instance startExclusive, long limit, boolean persistedOnly) {
        if (startExclusive == null) {
            startExclusive = InstanceUtils.nullInstance();
        }
        List<InstancePO> instancePOs = loadByType(new LoadByTypeRequest(type, startExclusive, limit));
        List<Instance> persistedResult = NncUtils.map(instancePOs, instancePO -> get(instancePO.getId()));
        if (persistedResult.size() >= limit || persistedOnly) {
            return persistedResult;
        }
        Set<Long> persistedIds = NncUtils.mapUnique(persistedResult, Instance::getId);
        var result = NncUtils.union(
                persistedResult,
                getByTypeFromBuffer(type, startExclusive, (int) (limit - persistedResult.size()), persistedIds)
        );
        return result;
    }

    @Override
    public List<Instance> getByType(Type type, Instance startExclusive, long limit) {
        return getByType(type, startExclusive, limit, false);
    }

    private List<Instance> getByTypeFromBuffer(Type type, Instance startExclusive, int limit, Set<Long> persistedIds) {
        List<Instance> typeInstances = NncUtils.filter(
                this,
                i -> type == i.getType() && !persistedIds.contains(i.getId())
        );
        if (startExclusive.isNull()) {
            return typeInstances.subList(0, Math.min(typeInstances.size(), limit));
        }
        int index = typeInstances.indexOf(startExclusive);
        return typeInstances.subList(
                index + 1,
                Math.min(typeInstances.size(), index + 1 + limit)
        );
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        if (NncUtils.anyMatch(this, i -> type.isInstance(i) && (!persistedOnly || i.isPersisted()))) {
            return true;
        }
        return type.getId() != null && NncUtils.isNotEmpty(getByType(type, null, 1, persistedOnly));
    }

    @Override
    public List<Instance> query(InstanceIndexQuery query) {
        List<Long> instanceIds = instanceStore.query(query, this);
        return NncUtils.map(instanceIds, this::get);
    }

    @Override
    public long count(InstanceIndexQuery query) {
        return instanceStore.count(query, this);
    }

    @Override
    public List<Instance> scan(Instance startExclusive, long limit) {
        return NncUtils.map(
                instanceStore.scan(List.of(
                        new ScanQuery(startExclusive.isNull() ? 0L : startExclusive.getIdRequired() + 1L, limit)
                ), this),
                instancePO -> get(instancePO.getId())
        );
    }
}
