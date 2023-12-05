package tech.metavm.object.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.Page;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.InstanceQueryBuilder;
import tech.metavm.entity.InstanceQueryField;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.query.GraphQueryExecutor;
import tech.metavm.object.instance.query.InstanceNode;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.ValueFormatter;
import tech.metavm.util.*;

import java.util.*;

@Component
public class InstanceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceManager.class);

    private final InstanceStore instanceStore;

    private final InstanceContextFactory instanceContextFactory;

    private final InstanceQueryService instanceQueryService;

    public InstanceManager(InstanceStore instanceStore, InstanceContextFactory instanceContextFactory, InstanceQueryService instanceQueryService) {
        this.instanceStore = instanceStore;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceQueryService = instanceQueryService;
    }

    @Transactional(readOnly = true)
    public GetInstanceResponse get(long id, int depth) {
        var batchResp = batchGet(List.of(id), depth);
        return new GetInstanceResponse(batchResp.instances().get(0));
    }

    public Page<InstanceDTO[]> select(SelectRequest request) {
        try (var context = newContext()) {
            ClassType type = context.getClassType(request.typeId());
            Page<Instance> dataPage = instanceQueryService.query(InstanceQueryBuilder.newBuilder(type)
                    .expression(request.condition())
                    .page(request.page())
                    .pageSize(request.pageSize())
                    .build(), context
            );
            List<Instance> roots = dataPage.data();
            List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(type, sel, context));
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            return new Page<>(
                    graphQueryExecutor.execute(type, roots, selects, context.getEntityContext()),
                    dataPage.total()
            );
        }
    }

    public List<TreeDTO> getTrees(List<Long> ids) {
        try(var context = newContext()) {
            var instances = context.batchGet(ids);
            var roots = NncUtils.mapUnique(instances, Instance::getRoot);
            return NncUtils.map(roots, r -> r.toTree(true).toDTO());
        }
    }

    public List<InstanceVersionDTO> getVersions(List<Long> ids) {
        try(var context = newContext()) {
            var instances = context.batchGet(ids);
            var roots = NncUtils.mapUnique(instances, Instance::getRoot);
            return NncUtils.map(roots, r -> new InstanceVersionDTO(r.getIdRequired(), r.getVersion()));
        }
    }

    @Transactional(readOnly = true)
    public GetInstancesResponse batchGet(List<Long> ids) {
        return batchGet(ids, 1);
    }

    @Transactional(readOnly = true)
    public GetInstancesResponse batchGet(List<Long> ids, int depth) {
        try (var context = newContext();
             var ignored = ContextUtil.getProfiler().enter("batchGet")) {
            var instances = context.batchGet(ids);
            context.buffer(ids);
            try (var ignored1 = SerializeContext.enter()) {
                var instanceDTOs = NncUtils.map(instances, i -> InstanceDTOBuilder.buildDTO(i, depth));
                return new GetInstancesResponse(instanceDTOs);
            }
        }
    }

    @Transactional
    public void update(InstanceDTO instanceDTO) {
        try (var context = newContext(true)) {
            if (instanceDTO.id() == null) {
                throw BusinessException.invalidParams("实例ID为空");
            }
            update(instanceDTO, context);
            context.finish();
        }
    }


    public void save(InstanceDTO instanceDTO, IInstanceContext context) {
        if (instanceDTO.id() != null) {
            update(instanceDTO, context);
        } else {
            create(instanceDTO, context);
        }
    }

    public Instance update(InstanceDTO instanceDTO, IInstanceContext context) {
        return ValueFormatter.parseInstance(instanceDTO, context);
    }

    @Transactional
    public long create(InstanceDTO instanceDTO) {
        try (var context = newContext(true)) {
            Instance instance = create(instanceDTO, context);
            context.finish();
            return instance.getIdRequired();
        }
    }

    public Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return InstanceFactory.create(instanceDTO, context);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        try (var context = newContext(true)) {
            context.batchRemove(NncUtils.mapAndFilter(ids, context::get, Objects::nonNull));
            context.finish();
        }
    }

    @Transactional
    public void delete(long id) {
        try (var context = newContext(true)) {
            Instance instance = context.get(id);
            if (instance != null) {
                context.remove(instance);
                context.finish();
            }
        }
    }

    @Transactional
    public void deleteByTypes(List<Long> typeIds) {
        try (var context = newContext()) {
            var entityContext = context.getEntityContext();
            var types = NncUtils.mapAndFilter(typeIds, entityContext::getType, type -> !type.isEnum());
            List<Instance> toRemove = NncUtils.flatMap(
                    types,
                    type -> context.getByType(type, null, 1000)
            );
            context.batchRemove(toRemove);
            context.finish();
        }
    }

    public List<String> getReferenceChain(long id, int rootMode) {
        try (var context = newContext()) {
            ReferenceTree root = new ReferenceTree(context.get(id), rootMode);
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
                    var tree = new ReferenceTree(context.get(ref.getSourceId()), rootMode);
                    parent.addChild(tree);
                    trees.put(ref.getSourceId(), tree);
                }
            }
            context.batchGet(visited);
            return root.getPaths();
        }
    }

    public QueryInstancesResponse query(InstanceQueryDTO query) {
        try (var context = newContext()) {
            Type type = context.getType(query.typeId());
            if (type instanceof ClassType) {
                var internalQuery = InstanceQueryBuilder.newBuilder(type)
                        .searchText(query.searchText())
                        .newlyCreated(query.newlyCreated())
                        .fields(NncUtils.map(query.fields(), f -> InstanceQueryField.create(f, context)))
                        .expression(query.expression())
                        .page(query.page())
                        .pageSize(query.pageSize())
                        .build();
                var dataPage1 = instanceQueryService.query(internalQuery, context);
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
        try (var context = newContext()) {
            List<Path> paths = NncUtils.map(request.paths(), Path::create);
            Map<Instance, InstanceNode<?>> instance2node = buildObjectTree(paths, context);
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            graphQueryExecutor.loadTree(instance2node);

            Set<Instance> visited = new IdentitySet<>();
            List<InstanceDTO> result = new ArrayList<>();
            for (Path path : paths) {
                long instanceId = parseIdFromPathItem(path.firstItem());
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
            Instance instance = context.get(parseIdFromPathItem(path.firstItem()));
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

    private static long parseIdFromPathItem(String pathItem) {
        if (pathItem.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            return Long.parseLong(pathItem.substring(Constants.CONSTANT_ID_PREFIX.length()));
        }
        throw new InternalException("Path item '" + pathItem + "' does not represent an identity");
    }

    private IInstanceContext newContext() {
        return newContext(ContextUtil.getAppId(), true);
    }

    private IInstanceContext newContext(long appId) {
        return instanceContextFactory.newContext(appId);
    }

    private IInstanceContext newContext(boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(ContextUtil.getAppId(), asyncLogProcessing);
    }

    private IInstanceContext newContext(long appId, @SuppressWarnings("SameParameterValue") boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(appId, asyncLogProcessing);
    }

}
