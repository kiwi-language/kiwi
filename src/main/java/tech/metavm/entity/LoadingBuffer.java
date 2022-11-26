package tech.metavm.entity;

import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.*;

import static tech.metavm.entity.LoadingOption.ENUM_CONSTANTS_LAZY_LOADING;

public class LoadingBuffer {

    private final List<Type> byTypeRequests = new ArrayList<>();
    private final List<LoadRequest> identityRequests = new ArrayList<>();
    private final Map<Long, InstancePO> identityResultMap = new HashMap<>();
    private final Map<Type, List<InstancePO>> byTypeResultMap = new HashMap<>();
    private final Set<Long> loaded = new HashSet<>();
    private final InstanceContext context;
    private final InstanceStore instanceStore;

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

    public List<InstancePO> getByType(Type type) {
        if(!byTypeResultMap.containsKey(type)) {
            byTypeRequests.add(type);
            flush();
        }
        return byTypeResultMap.get(type);
    }

    public InstancePO getEntityPO(long id) {
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
        return instanceStore.load(StoreLoadRequest.fromLoadRequests(requests), context);
    }

    private void flushByTypeRequests() {
        if(byTypeRequests.isEmpty()) {
            return;
        }
        List<InstancePO> instancePOs =
                instanceStore.getByTypeIds(NncUtils.map(byTypeRequests, Entity::getId), context);
        Map<Long, List<InstancePO>> typeId2InstancePOs = NncUtils.toMultiMap(instancePOs, InstancePO::getTypeId);
        for (Type byTypeRequest : byTypeRequests) {
            byTypeResultMap.put(
                    byTypeRequest,
                    typeId2InstancePOs.computeIfAbsent(byTypeRequest.getId(), k-> new ArrayList<>())
            );
        }
    }

}
