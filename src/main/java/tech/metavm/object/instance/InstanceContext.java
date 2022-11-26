//package tech.metavm.object.instance;
//
//import org.springframework.transaction.support.TransactionSynchronization;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//import tech.metavm.entity.EntityContext;
//import tech.metavm.entity.InternalKey;
//import tech.metavm.entity.LoadingBuffer;
//import tech.metavm.entity.SubContext;
//import tech.metavm.infra.IdService;
//import tech.metavm.object.instance.log.InstanceLog;
//import tech.metavm.object.instance.log.InstanceLogService;
//import tech.metavm.object.instance.persistence.IndexKeyPO;
//import tech.metavm.object.instance.rest.InstanceDTO;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.BusinessException;
//import tech.metavm.util.NncUtils;
//
//import java.util.*;
//
//public class InstanceContext {
//
//    public static final long DEFAULT_LIMIT = 100;
//
//    private final long tenantId;
//    private final Map<Long, Optional<String>> titleMap = new HashMap<>();
//    private final SubContext<Instance> headSubContext = new SubContext<>();
//    private final SubContext<Instance> bufferSubContext = new SubContext<>();
//    private final Set<Long> loadedIds = new HashSet<>();
//    private final InstanceStore instanceStore;
//    private final EntityContext entityContext;
//    private final boolean asyncLogProcessing;
//    private final IdService idService;
//    private final List<ContextPlugin> plugins;
//    private final InstanceLogService instanceLogService;
//    private final LoadingBuffer loadingBuffer = new LoadingBuffer()
//
//    private List<InstanceLog> logs;
//    private boolean finished;
//
//    public InstanceContext(boolean asyncLogProcessing,
//                           InstanceStore instanceStore,
//                           InstanceLogService instanceLogService,
//                           EntityContext entityContext,
//                           List<ContextPlugin> plugins) {
//        this.tenantId = entityContext.getTenantId();
//        this.asyncLogProcessing = asyncLogProcessing;
//        this.instanceLogService = instanceLogService;
//        this.instanceStore = instanceStore;
//        this.entityContext = entityContext;
//        this.idService = entityContext.getIdService();
//        this.plugins = plugins;
//    }
//
//    public List<Instance> loadByTypeIds(List<Long> typeIds) {
//        List<Instance> instances = instanceStore.loadByTypeIds(typeIds, 0L, DEFAULT_LIMIT, this);
//        instances.forEach(this::addToContext);
//        return instances;
//    }
//
//    public Instance get(long id) {
//        ensureLoaded(id);
//        Instance instance = getBuffered(id);
//        if(instance == null) {
//            throw new RuntimeException("Instance " + id + " not found");
//        }
//        return instance;
//    }
//
//    public List<Instance> batchGet(Collection<Long> ids) {
//        if(NncUtils.isEmpty(ids)) {
//            return List.of();
//        }
//        ids = NncUtils.deduplicate(ids);
//        List<Long> idsToLoad = NncUtils.filterNot(ids, this::isLoaded);
//        doLoad(new LoadRequest(idsToLoad));
//        List<Instance> instances = new ArrayList<>();
//        for (Long id : ids) {
//            Instance instance = getBuffered(id);
//            if(instance == null) {
//                throw new RuntimeException("Instance not found for objectId: " + id);
//            }
//            instances.add(instance);
//        }
//        return instances;
//    }
//
//    public List<Instance> getByKey(IndexKeyPO indexKey) {
//        return instanceStore.selectByKey(indexKey, this);
//    }
//
//    public Instance getByUniqueKey(IndexKeyPO indexKey) {
//        return NncUtils.getFirst(instanceStore.selectByKey(indexKey, this));
//    }
//
//
//    public List<Instance> getByTypeIds(Collection<Long> typeIds) {
//        return instanceStore.getByTypeIds(typeIds, this);
//    }
//
//    public Instance upsert(InstanceDTO instanceDTO) {
//        if(instanceDTO.id() == null) {
//            return add(instanceDTO);
//        }
//        else {
//            return update(instanceDTO);
//        }
//    }
//
//    public Instance add(InstanceDTO instanceDTO) {
//        Instance instance = new Instance(instanceDTO, this);
//        bufferSubContext.add(instance);
//        return instance;
//    }
//
//    public Instance update(InstanceDTO update) {
//        if(update.id() == null) {
//            throw BusinessException.invalidParams("实例ID为空");
//        }
//        Instance instance = get(update.id());
//        instance.update(update);
//        return instance;
//    }
//
//    public void remove(long id) {
//        bufferSubContext.remove(get(id));
//    }
//
//    private Instance getBuffered(long id) {
//        return bufferSubContext.get(key(id));
//    }
//
//    private InternalKey key(long id) {
//        return new InternalKey(Instance.class, id);
//    }
//
//    private void ensureLoaded(long id) {
//        if(!isLoaded(id)) {
//            doLoad(LoadRequest.of(id));
//        }
//    }
//
//    public void loadRelationTitles(List<Instance> instances) {
//        Set<Long> destIds = new HashSet<>();
//        for (Instance instance : instances) {
//            for (InstanceRelation relation : instance.getRelations()) {
//                destIds.add(relation.getDestinationId());
//            }
//        }
//        doLoadTitles(destIds);
//    }
//
//    private void doLoadTitles(Collection<Long> ids) {
//        List<Long> idsToLoad = NncUtils.filterNot(ids, this::isTitleLoaded);
//        Map<Long, String> resultMap = instanceStore.loadTitles(tenantId, idsToLoad);
//        for (Long id : idsToLoad) {
//            titleMap.put(id, resultMap.containsKey(id) ? Optional.of(resultMap.get(id)) : Optional.empty());
//        }
//    }
//
//    public String getTitle(long id) {
//        doLoadTitles(List.of(id));
//        if(isLoaded(id)) {
//            Instance instance = get(id);
//            return instance != null ? instance.getTitle() : "";
//        }
//        else {
//            Optional<String> titleOpt = titleMap.get(id);
//            return titleOpt.orElse("");
//        }
//    }
//
//    private boolean isTitleLoaded(long id) {
//        return isLoaded(id) || titleMap.containsKey(id);
//    }
//
//    private void doLoad(LoadRequest request) {
//        loadedIds.addAll(request.ids);
//        List<Instance> instances = instanceStore.load(request.ids, this);
//        instances.forEach(this::addToContext);
//    }
//
//    private void addToContext(Instance instance) {
//        headSubContext.add(instance.copy());
//        bufferSubContext.add(instance);
//    }
//
//    private boolean isLoaded(long id) {
//        return loadedIds.contains(id);
//    }
//
//    public long getTenantId() {
//        return tenantId;
//    }
//
//    public Type getType(long typeId) {
//        return entityContext.getTypeRef(typeId);
//    }
//
//    public void initIds() {
//        bufferSubContext.initIds((size) -> idService.allocateIds(tenantId, size));
//    }
//
//    public void finish() {
//        if(finished) {
//            throw new IllegalStateException("Already finished");
//        }
//        finished = true;
//        initIds();
//        ContextDifference difference = diff();
//
//        for (ContextPlugin plugin : plugins) {
//            plugin.beforeSaving(difference);
//        }
//        instanceStore.save(difference);
//        for (ContextPlugin plugin : plugins) {
//            plugin.afterSaving(difference);
//        }
//        logs = difference.buildLogs();
//        headSubContext.rebuildFrom(bufferSubContext);
//        registerTransactionSynchronization();
//    }
//
//    private ContextDifference diff() {
//        ContextDifference diff = new ContextDifference(this);
//        diff.diff(
//                headSubContext.getFiltered(Instance::isPersistent),
//                bufferSubContext.getFiltered(Instance::isPersistent)
//        );
//        return diff;
//    }
//
//    private void processLogs() {
//        if(asyncLogProcessing) {
//            instanceLogService.asyncProcess(logs);
//        }
//        else {
//            instanceLogService.process(logs);
//        }
//    }
//
//    private void registerTransactionSynchronization() {
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//                        processLogs();
//                    }
//                }
//        );
//    }
//
//    public EntityContext getEntityContext() {
//        return entityContext;
//    }
//
//    private static record LoadRequest (
//            List<Long> ids
//    ) {
//        static LoadRequest of(long id) {
//            return new LoadRequest(List.of(id));
//        }
//    }
//
//}
