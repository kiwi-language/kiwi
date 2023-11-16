package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.IdAndValue;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.entity.LoadingOption.ENUM_CONSTANTS_LAZY_LOADING;

public class LoadingBuffer {

    private final List<ClassType> byTypeRequests = new ArrayList<>();
    private final List<LoadRequest> identityRequests = new ArrayList<>();
    private final Map<Long, InstancePO> identityResultMap = new HashMap<>();
    private final Map<ClassType, List<InstancePO>> byTypeResultMap = new HashMap<>();
    private final Set<Long> loaded = new HashSet<>();
    private final InstanceContext context;
    private final IInstanceStore instanceStore;
    private final Set<Long> aliveIds = new HashSet<>();
    private final Map<Long, RangeCache<Long>> rangeCaches = new HashMap<>();
    private final RangeCache<Long> globalRangeCache = new RangeCache<>(this::loadGlobalRange);

    public LoadingBuffer(InstanceContext context) {
        this.context = context;
        instanceStore = context.getInstanceStore();
    }

    public void load(LoadRequest request) {
        if (isMissing(request.id())) {
            identityRequests.add(request);
        }
    }

    public boolean isMissing(long id) {
        return !loaded.contains(id);
    }

    public void preload(long id, InstancePO instancePO) {
        if (isMissing(id)) {
            loaded.add(id);
            identityResultMap.put(id, instancePO);
        }
    }

    public void preload(Map<Long, InstancePO> supplierMap) {
        supplierMap.forEach(this::preload);
    }

    public InstancePO getInstancePO(long id) {
        if (!identityResultMap.containsKey(id)) {
            load(LoadRequest.create(id, ENUM_CONSTANTS_LAZY_LOADING));
            flush();
        }
        return identityResultMap.get(id);
    }

    public List<InstancePO> flush() {
        List<InstancePO> results = new ArrayList<>();
        flushIdRequests(results);
        flushByTypeRequests(new ArrayList<>());
        return NncUtils.deduplicate(results, InstancePO::getId);
    }

    public List<InstancePO> scan(long startId, long limit) {
        return NncUtils.map(
                globalRangeCache.query(List.of(new RangeQuery(startId, limit))).values().iterator().next(),
                identityResultMap::get
        );
    }

    private RangeCache<Long> getRangeCache(long typeId) {
        return rangeCaches.computeIfAbsent(typeId, k -> new RangeCache<>(
                queries -> loadTypeRange(typeId, queries)
        ));
    }


    private Map<RangeQuery, List<IdAndValue<Long>>> loadGlobalRange(List<RangeQuery> queries) {
        List<InstancePO> instancePOS = instanceStore.scan(
                NncUtils.map(queries, q -> new ScanQuery(q.startId(), q.limit())),
                context
        );
        return buildRangeResult(queries, instancePOS);
    }

    private Map<RangeQuery, List<IdAndValue<Long>>> loadTypeRange(long typeId, List<RangeQuery> queries) {
        List<InstancePO> instancePOs = instanceStore.queryByTypeIds(
                NncUtils.map(queries, q -> new ByTypeQuery(typeId, q.startId(), q.limit())),
                context
        );
        return buildRangeResult(queries, instancePOs);
    }

    private Map<RangeQuery, List<IdAndValue<Long>>> buildRangeResult(List<RangeQuery> queries, List<InstancePO> instancePOs) {
        for (InstancePO instancePO : instancePOs) {
            if (!identityResultMap.containsKey(instancePO.getId())) {
                identityResultMap.put(instancePO.getId(), instancePO);
            }
        }
        queries = new ArrayList<>(queries);
        queries.sort(Comparator.comparingLong(RangeQuery::startId));
        Map<RangeQuery, List<IdAndValue<Long>>> result = new HashMap<>();
        for (InstancePO instancePO : instancePOs) {
            int index = NncUtils.binarySearch(queries, instancePO.getId(), (q, id) -> Long.compare(q.startId(), id));
            RangeQuery query = index >= 0 ? queries.get(index) : queries.get(-index - 1);
            result.computeIfAbsent(query, k -> new ArrayList<>()).add(
                    new IdAndValue<>(instancePO.getIdRequired(), instancePO.getId())
            );
        }
        return result;
    }

    private void flushIdRequests(List<InstancePO> results) {
        if (identityRequests.isEmpty()) {
            return;
        }
        var loaded = loadInstancePOs(identityRequests);
        results.addAll(loaded);
        for (InstancePO instancePO : loaded) {
            identityResultMap.put(instancePO.getId(), instancePO);
        }
        identityRequests.clear();
    }

    private List<InstancePO> loadInstancePOs(List<LoadRequest> requests) {
        List<InstancePO> instancePOs = instanceStore.load(StoreLoadRequest.fromLoadRequests(requests), context);
        addAliveIds(instancePOs);
        return instancePOs;
    }

    public boolean isAlive(long id) {
        return identityResultMap.containsKey(id) || aliveIds.contains(id);
    }

    private void addAliveIds(List<InstancePO> instancePOs) {
        Set<Long> refIds = new HashSet<>();
        for (InstancePO instancePO : instancePOs) {
            if (instancePO instanceof InstanceArrayPO arrayPO) {
                extractRefIdsFromArray(arrayPO, refIds);
            } else {
                extractRefIdsFromObject(instancePO, refIds);
            }
        }
        refIds = new HashSet<>(NncUtils.exclude(refIds, identityResultMap::containsKey));
        aliveIds.addAll(instanceStore.getAliveInstanceIds(context.getTenantId(), refIds));
    }

    public void extractRefIdsFromObject(InstancePO instancePO, Set<Long> refIds) {
        for (Map<String, Object> subMap : instancePO.getData().values()) {
            for (Object fieldValue : subMap.values()) {
                NncUtils.invokeIfNotNull(convertToRefId(fieldValue), refIds::add);
            }
        }
    }

    public void extractRefIdsFromArray(InstanceArrayPO arrayPO, Set<Long> refIds) {
        List<Object> elements = arrayPO.getElements();
        for (Object element : elements) {
            NncUtils.invokeIfNotNull(convertToRefId(element), refIds::add);
        }
    }

    private Long convertToRefId(Object fieldValue) {
        if (fieldValue instanceof IdentityPO identityPO) {
            return identityPO.id();
        }
        return null;
    }

    private void flushByTypeRequests(List<InstancePO> results) {
        if (byTypeRequests.isEmpty()) {
            return;
        }
        List<InstancePO> instancePOs =
                instanceStore.getByTypeIds(NncUtils.map(byTypeRequests, Entity::getId), context);
        results.addAll(instancePOs);
        Map<Long, List<InstancePO>> typeId2InstancePOs = NncUtils.toMultiMap(instancePOs, InstancePO::getTypeId);
        for (ClassType byTypeRequest : byTypeRequests) {
            byTypeResultMap.put(
                    byTypeRequest,
                    typeId2InstancePOs.computeIfAbsent(byTypeRequest.getId(), k -> new ArrayList<>())
            );
        }
    }

}
