package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.ConstantExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionParser;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.query.GraphQueryExecutor;
import tech.metavm.object.instance.query.InstanceNode;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.ValueFormatter;
import tech.metavm.util.*;

import java.util.*;

@Component
public class InstanceManager {

    private final InstanceStore instanceStore;

    private final InstanceContextFactory instanceContextFactory;

    private final InstanceSearchService instanceSearchService;

    public InstanceManager(InstanceStore instanceStore, InstanceContextFactory instanceContextFactory, InstanceSearchService instanceSearchService) {
        this.instanceStore = instanceStore;
        this.instanceContextFactory = instanceContextFactory;
        this.instanceSearchService = instanceSearchService;
    }

    public GetInstanceResponse get(long id, int depth) {
        var batchResp = batchGet(List.of(id), depth);
        return new GetInstanceResponse(batchResp.instances().get(0), batchResp.contextTypes());
    }

    public Page<InstanceDTO[]> select(SelectRequestDTO request) {
        try (var context = newContext()) {
            ClassType type = context.getClassType(request.typeId());
            SearchQuery searchQuery = new SearchQuery(
                    ContextUtil.getTenantId(),
                    type.getSubTypeIds(),
                    ExpressionParser.parse(type, request.condition(), context),
                    false,
                    request.page(),
                    request.pageSize()
            );
            Page<Long> idPage = instanceSearchService.search(searchQuery);
            List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(type, sel, context));

            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            List<Instance> roots = NncUtils.map(idPage.data(), context::get);
            return new Page<>(
                    graphQueryExecutor.execute(type, roots, selects, context.getEntityContext()),
                    idPage.total()
            );
        }
    }

    public GetInstancesResponse batchGet(List<Long> ids) {
        return batchGet(ContextUtil.getTenantId(), ids, 1);
    }

    public GetInstancesResponse batchGet(List<Long> ids, int depth) {
        return batchGet(ContextUtil.getTenantId(), ids, depth);
    }

    public GetInstancesResponse batchGet(long tenantId, List<Long> ids, int depth) {
        try (var context = newContext(tenantId)) {
            var instances = context.batchGet(ids);
            try (var serContext = SerializeContext.enter()) {
                var instanceDTOs = NncUtils.map(instances, i -> InstanceDTOBuilder.buildDTO(i, depth));
                serContext.writeDependencies();
                return new GetInstancesResponse(instanceDTOs, serContext.getTypes());
            }
        }
    }

    @Transactional
    public void update(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        try (var context = newContext(asyncLogProcessing)) {
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
    public long create(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        try (var context = newContext(asyncLogProcessing)) {
            Instance instance = create(instanceDTO, context);
            context.finish();
            return instance.getIdRequired();
        }
    }

    public Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return InstanceFactory.create(instanceDTO, context);
    }

    @Transactional
    public void batchDelete(List<Long> ids, boolean asyncLogProcessing) {
        try (var context = newContext(asyncLogProcessing)) {
            context.batchRemove(NncUtils.mapAndFilter(ids, context::get, Objects::nonNull));
            context.finish();
        }
    }

    @Transactional
    public void delete(long id, boolean asyncLogProcessing) {
        try (var context = newContext(asyncLogProcessing)) {
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
                    type -> context.getByType(type, null, 100)
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
                var refs = instanceStore.getAllStrongReferences(context.getTenantId(), ids, visited);
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

    public QueryInstancesResponse query(InstanceQuery query) {
        try (var context = newContext()) {
            Type type = context.getType(query.typeId());
            if (type instanceof ClassType classType) {
                Expression expression =
                        query.searchText() != null ?
                                ExpressionParser.parse(classType, query.searchText(), context) :
                                ExpressionUtil.trueExpression();
                if ((expression instanceof ConstantExpression constExpr) && constExpr.isString()) {
                    Field titleField = classType.getTileField();
                    PrimitiveInstance searchTextInst = InstanceUtils.stringInstance(query.searchText());
                    expression = ExpressionUtil.or(
                            ExpressionUtil.fieldStartsWith(titleField, searchTextInst),
                            ExpressionUtil.fieldLike(titleField, searchTextInst)
                    );
                }
                SearchQuery searchQuery = new SearchQuery(
                        context.getTenantId(),
                        query.includeSubTypes() ? classType.getSubTypeIds() : Set.of(classType.getIdRequired()),
                        expression,
                        false,
                        query.page(),
                        query.pageSize()
                );
                Page<Long> idPage = instanceSearchService.search(searchQuery);

                List<Instance> instances = context.batchGet(idPage.data());
                instanceStore.loadTitles(NncUtils.map(instances, Instance::getId), context);
                var page = new Page<>(
                        NncUtils.map(instances, Instance::toDTO),
                        idPage.total()
                );
                if (query.includeContextTypes()) {
                    try (var serContext = SerializeContext.enter()) {
                        serContext.writeType(classType);
                        serContext.writeDependencies();
                        return new QueryInstancesResponse(page, serContext.getTypes());
                    }
                } else {
                    return new QueryInstancesResponse(page, List.of());
                }
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
        for (tech.metavm.object.instance.query.Path path : paths) {
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
        return newContext(ContextUtil.getTenantId(), true);
    }

    private IInstanceContext newContext(long tenantId) {
        return instanceContextFactory.newContext(tenantId);
    }

    private IInstanceContext newContext(boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(ContextUtil.getTenantId(), asyncLogProcessing);
    }

    private IInstanceContext newContext(long tenantId, @SuppressWarnings("SameParameterValue") boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(tenantId, asyncLogProcessing);
    }

}
