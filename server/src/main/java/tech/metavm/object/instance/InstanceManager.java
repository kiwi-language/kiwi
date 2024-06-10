package tech.metavm.object.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.object.instance.core.StructuralVisitor;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.query.GraphQueryExecutor;
import tech.metavm.object.instance.query.InstanceNode;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeParser;
import tech.metavm.object.type.ValueFormatter;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.*;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InstanceManager extends EntityContextFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(InstanceManager.class);

    private final InstanceStore instanceStore;

    private final InstanceQueryService instanceQueryService;

    public InstanceManager(EntityContextFactory entityContextFactory, InstanceStore instanceStore, InstanceQueryService instanceQueryService) {
        super(entityContextFactory);
        this.instanceStore = instanceStore;
        this.instanceQueryService = instanceQueryService;
    }

    @Transactional(readOnly = true)
    public GetInstanceResponse get(String id, int depth) {
        try (var context = newInstanceContext()) {
            var instanceId = Id.parse(id);
            var instance = context.get(instanceId);
            var instanceDTO = InstanceDTOBuilder.buildDTO(instance, depth);
            return new GetInstanceResponse(instanceDTO);
        }
    }

    public Page<InstanceDTO[]> select(SelectRequest request) {
        try (var entityContext = newContext()) {
            var context = entityContext.getInstanceContext();
            var klass = ((ClassType) TypeParser.parseType(request.type(), context.getTypeDefProvider())).resolve();
            var dataPage = instanceQueryService.query(InstanceQueryBuilder.newBuilder(klass)
                    .expression(request.condition())
                    .page(request.page())
                    .pageSize(request.pageSize())
                    .build(), entityContext
            );
            var roots = dataPage.data();
            List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(klass, sel, entityContext));
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            return new Page<>(
                    graphQueryExecutor.execute(klass, roots, selects),
                    dataPage.total()
            );
        }
    }

    public List<TreeDTO> getTrees(List<Long> ids) {
        try (var context = newInstanceContext()) {
            var systemIds = NncUtils.filter(ids, RegionConstants::isSystemId);
            var nonSystemIds = NncUtils.exclude(ids, RegionConstants::isSystemId);
            var systemInstances = instanceStore.loadForest(systemIds, ModelDefRegistry.getDefContext().getInstanceContext());
            var nonSystemInstances = instanceStore.loadForest(nonSystemIds, context);
            var instanceMap = new HashMap<Long, InstancePO>();
            systemInstances.forEach(i -> instanceMap.put(i.getId(), i));
            nonSystemInstances.forEach(i -> instanceMap.put(i.getId(), i));
            return ids.stream()
                    .map(instanceMap::get)
                    .map(i -> new TreeDTO(i.getId(), i.getVersion(), i.getNextNodeId(), i.getData()))
                    .collect(Collectors.toList());
        }
    }

    public List<TreeVersion> getVersions(List<Long> ids) {
        try (var context = newInstanceContext()) {
            return instanceStore.getVersions(ids, context);
        }
    }

    @Transactional(readOnly = true)
    public GetInstanceResponse getDefaultView(String id) {
        try (var context = newInstanceContext()) {
            var instanceId = Id.parse(id);
            var instance = context.get(instanceId);
            if (instance instanceof ClassInstance classInstance) {
                if (instanceId instanceof PhysicalId/* && !classInstance.getType().isStruct()*/) {
                    var defaultMapping = classInstance.getType().resolve().getDefaultMapping();
                    if (defaultMapping != null) {
                        var viewId = new DefaultViewId(false, defaultMapping.toKey(), instanceId);
                        var view = context.get(viewId);
                        return new GetInstanceResponse(InstanceDTOBuilder.buildDTO(view, 1));
                    }
                }
                return new GetInstanceResponse(InstanceDTOBuilder.buildDTO(instance, 1));
            } else
                throw new BusinessException(ErrorCode.NOT_A_CLASS_INSTANCE, id);
        }
    }

    @Transactional(readOnly = true)
    public GetInstancesResponse batchGet(List<String> ids, int depth) {
        try (var context = newInstanceContext()) {
            return batchGet(ids, depth, context);
        }
    }

    public GetInstancesResponse batchGet(List<String> ids, int depth, IInstanceContext context) {
        var instanceIds = NncUtils.map(ids, Id::parse);
        try (var ignored = ContextUtil.getProfiler().enter("batchGet")) {
            var instances = context.batchGet(instanceIds);
            context.buffer(instanceIds);
            try (var ignored1 = SerializeContext.enter()) {
                var instanceDTOs = NncUtils.map(instances, i -> InstanceDTOBuilder.buildDTO(i, depth));
                return new GetInstancesResponse(instanceDTOs);
            }
        }
    }

    @Transactional
    public void update(InstanceDTO instanceDTO) {
        try (var context = newContext()) {
            if (instanceDTO.isNew()) {
                throw BusinessException.invalidParams("Instance is new");
            }
            update(instanceDTO, context.getInstanceContext());
            context.finish();
        }
    }


    public void save(InstanceDTO instanceDTO, IInstanceContext context) {
        if (!instanceDTO.isNew()) {
            update(instanceDTO, context);
        } else {
            create(instanceDTO, context);
        }
    }

    public Instance update(InstanceDTO instanceDTO, IInstanceContext context) {
        return ValueFormatter.parseInstance(instanceDTO, context);
    }

    @Transactional
    public String create(InstanceDTO instanceDTO) {
        try (var context = newContext()) {
            var instance = create(instanceDTO, context.getInstanceContext());
            context.finish();
            if (instance.tryGetSource() != null) {
                logger.info("source id: {}", instance.getSource().tryGetId());
            }
            return Objects.requireNonNull(instance.tryGetId()).toString();
        }
    }

    public DurableInstance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return InstanceFactory.create(instanceDTO, context);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        try (var context = newInstanceContext()) {
            context.batchRemove(NncUtils.mapAndFilter(ids, id -> context.get(Id.parse(id)), Objects::nonNull));
            context.finish();
        }
    }

    @Transactional
    public void delete(String id) {
        try (var context = newInstanceContext()) {
            var instance = context.get(Id.parse(id));
            if (instance != null) {
                context.remove(instance);
                context.finish();
            }
        }
    }

    @Transactional
    public void deleteByTypes(List<String> typeIds) {
        // TODO run a task to remove instances by type
//        try (var context = newInstanceContext()) {
//            var types = NncUtils.mapAndFilter(typeIds, id -> context.getType(Id.parse(id)), type -> !type.isEnum());
//            var toRemove = NncUtils.flatMap(
//                    types,
//                    type -> context.getByType(type, null, 1000)
//            );
//            context.batchRemove(toRemove);
//            context.finish();
//        }
    }

    public List<String> getReferenceChain(String stringId, int rootMode) {
        try (var context = newInstanceContext()) {
            var id = Id.parse(stringId);
            ReferenceTree root = new ReferenceTree(context.get(id), rootMode);
            Set<Long> visited = new HashSet<>();
            Map<Id, ReferenceTree> trees = new HashMap<>();
            trees.put(id, root);
            visited.add(id.getTreeId());
            Set<Id> ids = new HashSet<>();
            ids.add(id);
            while (!ids.isEmpty()) {
                var refs = instanceStore.getAllStrongReferences(context.getAppId(), ids, visited);
                ids.clear();
                for (ReferencePO ref : refs) {
                    var sourceId = ref.getSourceTreeId();
                    if (visited.contains(sourceId)) continue;
                    visited.add(sourceId);
                    var inst = context.getRoot(sourceId);
                    var parent = trees.get(ref.getTargetInstanceId());
                    var tree = new ReferenceTree(inst, rootMode);
                    parent.addChild(tree);

                    inst.accept(new StructuralVisitor() {
                        @Override
                        public Void visitDurableInstance(DurableInstance instance) {
                            ids.add(instance.getId());
                            trees.put(instance.getId(), tree);
                            return super.visitDurableInstance(instance);
                        }
                    });
                }
            }
//            context.batchGet(visited);
            return root.getPaths();
        }
    }

    public QueryInstancesResponse query(InstanceQueryDTO query) {
        try (var entityContext = newContext()) {
            var context = entityContext.getInstanceContext();
            var mappingProvider = context.getMappingProvider();
            Type type = TypeParser.parseType(query.type(), context.getTypeDefProvider());
            if (type instanceof ClassType classType) {
                var internalQuery = InstanceQueryBuilder.newBuilder(classType.resolve())
                        .searchText(query.searchText())
                        .newlyCreated(NncUtils.map(query.createdIds(), Id::parse))
                        .fields(NncUtils.map(query.fields(), f -> InstanceQueryField.create(f, entityContext)))
                        .expression(query.expression())
                        .sourceMapping(NncUtils.get(query.sourceMappingId(), id -> mappingProvider.getMapping(Id.parse(id))))
                        .page(query.page())
                        .pageSize(query.pageSize())
                        .build();
                var dataPage1 = instanceQueryService.query(internalQuery, entityContext);
                return new QueryInstancesResponse(
                        new Page<>(
                                NncUtils.map(dataPage1.data(), Instance::toDTO),
                                dataPage1.total()
                        ),
                        List.of()
                );
            } else {
                return new QueryInstancesResponse(new Page<>(List.of(), 0L), List.of());
            }
        }
    }

    public List<InstanceDTO> loadByPaths(LoadInstancesByPathsRequest request) {
        try (var context = newInstanceContext()) {
            List<Path> paths = NncUtils.map(request.paths(), Path::create);
            Map<Instance, InstanceNode<?>> instance2node = buildObjectTree(paths, context);
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            graphQueryExecutor.loadTree(instance2node);

            Set<Instance> visited = new IdentitySet<>();
            List<InstanceDTO> result = new ArrayList<>();
            for (Path path : paths) {
                var instanceId = Id.parse(path.firstItem().substring(Constants.CONSTANT_ID_PREFIX.length()));
                Instance instance = context.get(instanceId);
                InstanceNode<?> node = instance2node.get(instance);
                List<Instance> values = node.getFetchResults(instance, path.subPath());
                for (Instance value : values) {
                    if (!visited.contains(value)) {
                        visited.add(value);
                        result.add(value.toDTO());
                    }
                }
            }
            return result;
        }
    }

    private Map<Instance, InstanceNode<?>> buildObjectTree(List<Path> paths, IInstanceContext context) {
        Map<Instance, PathTree> pathTreeMap = new HashMap<>();
        for (Path path : paths) {
            Instance instance = context.get(Id.parse(path.firstItem().substring(Constants.CONSTANT_ID_PREFIX.length())));
            PathTree pathTree = pathTreeMap.computeIfAbsent(instance, k -> new PathTree(k + ""));
            if (path.hasSubPath()) {
                pathTree.addPath(path.subPath());
            }
        }
        Map<Instance, InstanceNode<?>> objectTreeMap = new HashMap<>();
        pathTreeMap.forEach((instance, pathTree) ->
                objectTreeMap.put(instance, InstanceNode.create(pathTree, instance.getType()))
        );
        return objectTreeMap;
    }

    private IInstanceContext newInstanceContext() {
        //noinspection resource
        return newContext().getInstanceContext();
    }

}
