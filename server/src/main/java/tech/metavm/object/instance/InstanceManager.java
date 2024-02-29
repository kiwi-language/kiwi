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
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.query.GraphQueryExecutor;
import tech.metavm.object.instance.query.InstanceNode;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.ParameterizedTypeProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.ValueFormatter;
import tech.metavm.util.*;

import java.util.*;

@Component
public class InstanceManager extends EntityContextFactoryBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceManager.class);

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
            ClassType type = context.getClassType(request.typeId());
            var dataPage = instanceQueryService.query(InstanceQueryBuilder.newBuilder(type)
                    .expression(request.condition())
                    .page(request.page())
                    .pageSize(request.pageSize())
                    .build(), entityContext
            );
            var roots = dataPage.data();
            List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(type, sel, entityContext));
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            return new Page<>(
                    graphQueryExecutor.execute(type, roots, selects, context.getParameterizedFlowProvider()),
                    dataPage.total()
            );
        }
    }

    public List<TreeDTO> getTrees(List<Long> ids) {
        try (var context = newInstanceContext()) {
            var instances = context.batchGet(NncUtils.map(ids, PhysicalId::of));
            var roots = NncUtils.mapUnique(instances, DurableInstance::getRoot);
            return NncUtils.map(roots, r -> r.toTree(true).toDTO());
        }
    }

    public List<InstanceVersionDTO> getVersions(List<String> ids) {
        try (var context = newInstanceContext()) {
            var instances = context.batchGet(NncUtils.map(ids, Id::parse));
            var roots = NncUtils.mapUnique(instances, DurableInstance::getRoot);
            return NncUtils.map(roots, r -> new InstanceVersionDTO(r.getPhysicalId(), r.getVersion()));
        }
    }

    @Transactional(readOnly = true)
    public GetInstanceResponse getDefaultView(String id) {
        try (var context = newInstanceContext()) {
            var instanceId = Id.parse(id);
            var instance = context.get(instanceId);
            if (instance instanceof ClassInstance classInstance) {
                if (instanceId instanceof PhysicalId) {
                    var defaultMapping = classInstance.getType().getDefaultMapping();
                    if (defaultMapping != null) {
                        var viewId = new DefaultViewId(defaultMapping.getId(), instanceId);
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
            if (instanceDTO.id() == null) {
                throw BusinessException.invalidParams("实例ID为空");
            }
            update(instanceDTO, context.getInstanceContext(), context.getGenericContext());
            context.finish();
        }
    }


    public void save(InstanceDTO instanceDTO, IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
        if (instanceDTO.id() != null) {
            update(instanceDTO, context, parameterizedTypeProvider);
        } else {
            create(instanceDTO, context, parameterizedTypeProvider);
        }
    }

    public Instance update(InstanceDTO instanceDTO, IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
        return ValueFormatter.parseInstance(instanceDTO, context, parameterizedTypeProvider);
    }

    @Transactional
    public String create(InstanceDTO instanceDTO) {
        try (var context = newContext()) {
            Instance instance = create(instanceDTO, context.getInstanceContext(), context.getGenericContext());
            context.finish();
            return Objects.requireNonNull(instance.getId()).toString();
        }
    }

    public Instance create(InstanceDTO instanceDTO, IInstanceContext context, ParameterizedTypeProvider parameterizedTypeProvider) {
        return InstanceFactory.create(instanceDTO, context, parameterizedTypeProvider);
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
    public void deleteByTypes(List<Long> typeIds) {
        try (var context = newInstanceContext()) {
            var types = NncUtils.mapAndFilter(typeIds, context::getType, type -> !type.isEnum());
            var toRemove = NncUtils.flatMap(
                    types,
                    type -> context.getByType(type, null, 1000)
            );
            context.batchRemove(toRemove);
            context.finish();
        }
    }

    public List<String> getReferenceChain(long id, int rootMode) {
        try (var context = newInstanceContext()) {
            ReferenceTree root = new ReferenceTree(context.get(PhysicalId.of(id)), rootMode);
            Set<Long> visited = new HashSet<>();
            Map<Long, ReferenceTree> trees = new HashMap<>();
            trees.put(id, root);
            visited.add(id);
            Set<Long> ids = new HashSet<>();
            ids.add(id);
            while (!ids.isEmpty()) {
                var refs = instanceStore.getAllStrongReferences(context.getAppId(), ids, visited);
                ids.clear();
                for (ReferencePO ref : refs) {
                    if (visited.contains(ref.getSourceId())) continue;
                    visited.add(ref.getSourceId());
                    ids.add(ref.getSourceId());
                    var parent = trees.get(ref.getTargetId());
                    var tree = new ReferenceTree(context.get(PhysicalId.of(ref.getSourceId())), rootMode);
                    parent.addChild(tree);
                    trees.put(ref.getSourceId(), tree);
                }
            }
            context.batchGet(NncUtils.map(visited, PhysicalId::new));
            return root.getPaths();
        }
    }

    public QueryInstancesResponse query(InstanceQueryDTO query) {
        try (var entityContext = newContext()) {
            var context = entityContext.getInstanceContext();
            var mappingProvider = context.getMappingProvider();
            Type type = context.getType(query.typeId());
            if (type instanceof ClassType) {
                var internalQuery = InstanceQueryBuilder.newBuilder(type)
                        .searchText(query.searchText())
                        .newlyCreated(NncUtils.map(query.createdIds(), Id::parse))
                        .fields(NncUtils.map(query.fields(), f -> InstanceQueryField.create(f, entityContext)))
                        .expression(query.expression())
                        .sourceMapping(NncUtils.get(query.sourceMappingId(), mappingProvider::getMapping))
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
