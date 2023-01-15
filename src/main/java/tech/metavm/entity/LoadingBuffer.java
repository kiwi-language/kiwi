package tech.metavm.entity;

import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
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

    public LoadingBuffer(InstanceContext context) {
        this.context = context;
        instanceStore = context.getInstanceStore();
    }

    public void load(LoadRequest request) {
        if(isMissing(request.id())) {
            identityRequests.add(request);
        }
    }

    public boolean isMissing(long id) {
        return !loaded.contains(id);
    }

    public void preload(long id, InstancePO instancePO) {
        if(isMissing(id)) {
            loaded.add(id);
            identityResultMap.put(id, instancePO);
        }
    }

    public void preload(Map<Long, InstancePO> supplierMap) {
        supplierMap.forEach(this::preload);
    }

    public InstancePO getInstancePO(long id) {
        if(!identityResultMap.containsKey(id)) {
            load(LoadRequest.create(id, ENUM_CONSTANTS_LAZY_LOADING));
            flush();
        }
        return identityResultMap.get(id);
    }

    public void flush() {
        flushIdRequests();
        flushByTypeRequests();
    }

    private void flushIdRequests() {
        if(identityRequests.isEmpty()) {
            return;
        }
        for (InstancePO instancePO : loadInstancePOs(identityRequests)) {
            identityResultMap.put(instancePO.getId(), instancePO);
        }
        identityRequests.clear();
    }

    private List<InstancePO> loadInstancePOs(List<LoadRequest> requests) {
        List<InstancePO> instancePOs = instanceStore.load(StoreLoadRequest.fromLoadRequests(requests), context);
        addAliveIds(instancePOs);
        return instancePOs;
    }

    public boolean isRefTargetAlive(long id) {
        return identityResultMap.containsKey(id) || aliveIds.contains(id);
    }

    private void addAliveIds(List<InstancePO> instancePOs) {
        Set<Long> refIds = new HashSet<>();
        for (InstancePO instancePO : instancePOs) {
            if(instancePO instanceof InstanceArrayPO arrayPO) {
                extractRefIdsFromArray(arrayPO, refIds);
            }
            else {
                extractRefIdsFromObject(instancePO, refIds);
            }
        }
        refIds = new HashSet<>(NncUtils.filterNot(refIds, identityResultMap::containsKey));
        aliveIds.addAll(instanceStore.getAliveInstanceIds(context.getTenantId(), refIds));
    }

    public void extractRefIdsFromObject(InstancePO instancePO, Set<Long> refIds) {
        for (Object fieldValue: instancePO.getData().values()) {
            NncUtils.invokeIfNotNull(convertToRefId(fieldValue), refIds::add);
        }
    }

    public void extractRefIdsFromArray(InstanceArrayPO arrayPO, Set<Long> refIds) {
        List<Object> elements = arrayPO.getElements();
        for (Object element : elements) {
            NncUtils.invokeIfNotNull(convertToRefId(element), refIds::add);
        }
    }

    private Long convertToRefId(Object fieldValue) {
        if(fieldValue instanceof IdentityPO identityPO) {
            return identityPO.id();
        }
        return null;
    }

    private void flushByTypeRequests() {
        if(byTypeRequests.isEmpty()) {
            return;
        }
        List<InstancePO> instancePOs =
                instanceStore.getByTypeIds(NncUtils.map(byTypeRequests, Entity::getId), context);
        Map<Long, List<InstancePO>> typeId2InstancePOs = NncUtils.toMultiMap(instancePOs, InstancePO::getTypeId);
        for (ClassType byTypeRequest : byTypeRequests) {
            byTypeResultMap.put(
                    byTypeRequest,
                    typeId2InstancePOs.computeIfAbsent(byTypeRequest.getId(), k-> new ArrayList<>())
            );
        }
    }

}
