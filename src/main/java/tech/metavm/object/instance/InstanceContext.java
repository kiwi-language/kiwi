package tech.metavm.object.instance;

import tech.metavm.entity.EntityContext;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class InstanceContext {

    public static final long DEFAULT_LIMIT = 100;

    private final long tenantId;
    private final Map<Long, Optional<String>> titleMap = new HashMap<>();
    private final SubContext headSubContext = new SubContext();
    private final SubContext bufferSubContext = new SubContext();
    private final Set<Long> loadedIds = new HashSet<>();
    private final Map<Long, ModelLoadInfo> modelLoading = new HashMap<>();
    private final InstanceStore instanceStore;
    private final EntityContext entityContext;
    private final boolean asyncLogProcessing;

    private List<InstanceLog> logs;
    private boolean finished;

    public InstanceContext(long tenantId, boolean asyncLogProcessing, InstanceStore instanceStore, EntityContext entityContext) {
        this.tenantId = tenantId;
        this.asyncLogProcessing = asyncLogProcessing;
        this.instanceStore = instanceStore;
        this.entityContext = entityContext;
    }

    public List<Instance> loadByModelId(long modelId) {
        return loadByModelIds(List.of(modelId));
    }

    public List<Instance> loadByModelIds(List<Long> modelIds) {
        List<Instance> instances = instanceStore.loadByModelIds(modelIds, 0L, DEFAULT_LIMIT, this);
        instances.forEach(this::addToContext);
        return instances;
    }

    public Instance get(long id) {
        ensureLoaded(id);
        Instance instance = bufferSubContext.get(id);
        if(instance == null) {
            throw new RuntimeException("Instance " + id + " not found");
        }
        return instance;
    }

    public List<Instance> batchGet(List<Long> ids) {
        ids = NncUtils.deduplicate(ids);
        List<Long> idsToLoad = NncUtils.filterNot(ids, this::isLoaded);
        doLoad(new LoadRequest(idsToLoad));
        List<Instance> instances = new ArrayList<>();
        for (Long id : ids) {
            Instance instance = bufferSubContext.get(id);
            if(instance == null) {
                throw new RuntimeException("Instance not found for objectId: " + id);
            }
            instances.add(instance);
        }
        return instances;
    }

    public Instance upsert(InstanceDTO instanceDTO) {
        if(instanceDTO.id() == null) {
            return add(instanceDTO);
        }
        else {
            return update(instanceDTO);
        }
    }

    public Instance add(InstanceDTO instanceDTO) {
        Instance instance = new Instance(tenantId, instanceDTO, this);
        bufferSubContext.add(instance);
        return instance;
    }

    public Instance update(InstanceDTO update) {
        if(update.id() == null) {
            throw BusinessException.invalidParams("实例ID为空");
        }
        Instance instance = get(update.id());
        instance.update(update);
        return instance;
    }

    public void delete(long id) {
        bufferSubContext.delete(get(id));
    }

    private void ensureLoaded(long id) {
        if(!isLoaded(id)) {
            doLoad(LoadRequest.of(id));
        }
    }

    public void loadRelationTitles(List<Instance> instances) {
        Set<Long> destIds = new HashSet<>();
        for (Instance instance : instances) {
            for (InstanceRelation relation : instance.getRelations()) {
                destIds.add(relation.getDestinationId());
            }
        }
        doLoadTitles(destIds);
    }

    private void doLoadTitles(Collection<Long> ids) {
        List<Long> idsToLoad = NncUtils.filterNot(ids, this::isTitleLoaded);
        Map<Long, String> resultMap = instanceStore.loadTitles(tenantId, idsToLoad);
        for (Long id : idsToLoad) {
            titleMap.put(id, resultMap.containsKey(id) ? Optional.of(resultMap.get(id)) : Optional.empty());
        }
    }

    public String getTitle(long id) {
        doLoadTitles(List.of(id));
        if(isLoaded(id)) {
            Instance instance = get(id);
            return instance != null ? instance.getTitle() : "";
        }
        else {
            Optional<String> titleOpt = titleMap.get(id);
            return titleOpt.orElse("");
        }
    }

    private boolean isTitleLoaded(long id) {
        return isLoaded(id) || titleMap.containsKey(id);
    }

    private void doLoad(LoadRequest request) {
        loadedIds.addAll(request.ids);
        List<Instance> instances = instanceStore.load(request.ids, this);
        instances.forEach(this::addToContext);
    }

    private void addToContext(Instance instance) {
        headSubContext.add(instance.copy());
        bufferSubContext.add(instance);
    }

    private boolean isLoaded(long id) {
        return loadedIds.contains(id);
    }

    public long getTenantId() {
        return tenantId;
    }

    public Type getType(long typeId) {
        return entityContext.getType(typeId);
    }

    public void finish() {
        if(finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        ContextDifference difference = diff();
        instanceStore.save(difference);
        logs = difference.buildLogs();
        commit();
    }

    public List<InstanceLog> getLogs() {
        return logs;
    }

    private void commit() {
        headSubContext.rebuildFrom(bufferSubContext);
    }

    private ContextDifference diff() {
        ContextDifference diff = new ContextDifference(tenantId);
        diff.diff(headSubContext.getInstances(), bufferSubContext.getInstances());
        return diff;
    }

    public boolean isAsyncLogProcessing() {
        return asyncLogProcessing;
    }

    private static record LoadRequest (
            List<Long> ids
    ) {
        static LoadRequest of(long id) {
            return new LoadRequest(List.of(id));
        }
    }

    private static class SubContext {
        private final Map<Long, Instance> instanceMap = new HashMap<>();

        public Instance get(long id) {
            return instanceMap.get(id);
        }

        public void add(Instance instance) {
            instanceMap.put(instance.getId(), instance);
        }

        public void delete(Instance instance) {
            if(instanceMap.remove(instance.getId()) == null) {
                throw new RuntimeException("Instance not found, objectId: " + instance.getId());
            }
        }

        Collection<Instance> getInstances() {
            return instanceMap.values();
        }

        void rebuildFrom(SubContext that) {
            instanceMap.clear();
            that.instanceMap.forEach((id, instance) ->
                instanceMap.put(id, instance.copy())
            );
        }

    }

    private static class ModelLoadInfo {
        private final long modelId;
        private long limit;

        ModelLoadInfo(long modelId) {
            this.modelId = modelId;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public long getLimit() {
            return limit;
        }
    }

}
