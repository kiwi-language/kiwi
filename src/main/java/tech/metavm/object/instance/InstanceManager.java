package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.instance.rest.LoadInstancesByPathsRequest;
import tech.metavm.object.instance.rest.SelectRequestDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
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

    public InstanceDTO get(long id, int depth) {
        Instance instance = newContext().get(id);
        return instance != null ? InstanceDTOBuilder.buildDTO(instance, depth) : null;
    }

    public Page<InstanceDTO[]> select(SelectRequestDTO request) {
        InstanceContext context = newContext();
        ClassType type = context.getClassType(request.typeId());
        SearchQuery searchQuery = new SearchQuery(
                ContextUtil.getTenantId(),
                type.getTypeIdsInHierarchy(),
                ExpressionParser.parse(type, request.condition(), context),
                request.page(),
                request.pageSize()
        );
        Page<Long> idPage = instanceSearchService.search(searchQuery);
        List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(type, sel, context));

        GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
        List<Instance> roots = NncUtils.map(idPage.data(), context::get);
        return new Page<>(
                graphQueryExecutor.execute(type, roots, selects),
                idPage.total()
        );
    }

    public List<InstanceDTO> batchGet(List<Long> ids) {
        return batchGet(ContextUtil.getTenantId(), ids, 1);
    }

    public List<InstanceDTO> batchGet(List<Long> ids, int depth) {
        return batchGet(ContextUtil.getTenantId(), ids, depth);
    }

    public List<InstanceDTO> batchGet(long tenantId, List<Long> ids, int depth) {
        return NncUtils.map(newContext(tenantId).batchGet(ids), i -> InstanceDTOBuilder.buildDTO(i, depth));
    }

    @Transactional
    public void update(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        InstanceContext context = newContext(asyncLogProcessing);
        if(instanceDTO.id() == null) {
            throw BusinessException.invalidParams("实例ID为空");
        }
        ValueFormatter.parseInstance(instanceDTO, context);
        context.finish();
    }

    @Transactional
    public long create(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        InstanceContext context = newContext(asyncLogProcessing);
        Instance instance = InstanceFactory.create(instanceDTO, context);
        context.finish();
        return instance.getId();
    }

    @Transactional
    public void delete(long id, boolean asyncLogProcessing) {
        InstanceContext context = newContext(asyncLogProcessing);
        Instance instance = context.get(id);
        if(instance != null) {
            context.remove(instance);
            context.finish();
        }
    }

    public Page<InstanceDTO> query(InstanceQueryDTO query) {
        long tenantId = ContextUtil.getTenantId();
        InstanceContext context = newContext(tenantId);
        ClassType type = context.getClassType(query.typeId());
        Expression expression = ExpressionParser.parse(type, query.searchText(), context);
        if((expression instanceof ConstantExpression constExpr) && constExpr.isString()) {
            Field titleField = type.getTileField();
            PrimitiveInstance searchTextInst = InstanceUtils.stringInstance(query.searchText());
            expression = ExpressionUtil.or(
                    ExpressionUtil.fieldStartsWith(titleField, searchTextInst),
                    ExpressionUtil.fieldLike(titleField, searchTextInst)
            );
        }
        SearchQuery searchQuery = new SearchQuery(
                tenantId,
                type.getTypeIdsInHierarchy(),
                expression,
                query.page(),
                query.pageSize()
        );
        Page<Long> idPage = instanceSearchService.search(searchQuery);

        List<Instance> instances = context.batchGet(idPage.data());
        instanceStore.loadTitles(NncUtils.map(instances, Instance::getId), context);
        return new Page<>(
                NncUtils.map(instances, Instance::toDTO),
                idPage.total()
        );
    }

    public List<InstanceDTO> loadByPaths(LoadInstancesByPathsRequest request) {
        IInstanceContext context = newContext();
        List<Path> paths = NncUtils.map(request.paths(), Path::create);
        Map<Instance, InstanceNode<?>> instance2node = buildObjectTree(paths, context);
        GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
        graphQueryExecutor.loadTree(instance2node);

        Set<Instance> visited = new IdentitySet<>();
        List<InstanceDTO> result = new ArrayList<>();
        for (Path path : paths) {
            long instanceId = parseIdFromPathItem(path.firstItem());
            Instance instance =  context.get(instanceId);
            InstanceNode<?> node = instance2node.get(instance);
            List<Instance> values = node.getFetchResults(instance, path.subPath());
            for (Instance value : values) {
                if(!visited.contains(value)) {
                    visited.add(value);
                    result.add(value.toDTO());
                }
            }
        }
        return result;
    }

    private Map<Instance, InstanceNode<?>> buildObjectTree(List<Path> paths, IInstanceContext context) {
        Map<Instance, PathTree> pathTreeMap = new HashMap<>();
        for (tech.metavm.object.instance.query.Path path : paths) {
            Instance instance = context.get(parseIdFromPathItem(path.firstItem()));
            PathTree pathTree = pathTreeMap.computeIfAbsent(instance, k -> new PathTree(k + ""));
            if(path.hasSubPath()) {
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
        if(pathItem.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            return Long.parseLong(pathItem.substring(Constants.CONSTANT_ID_PREFIX.length()));
        }
        throw new InternalException("Path item '" + pathItem + "' does not represent an identity");
    }

    private InstanceContext newContext() {
        return newContext(ContextUtil.getTenantId(), true);
    }

    private InstanceContext newContext(long tenantId) {
        return newContext(tenantId, true);
    }

    private InstanceContext newContext(boolean asyncLogProcessing) {
        return newContext(ContextUtil.getTenantId(), asyncLogProcessing);
    }

    private InstanceContext newContext(long tenantId, boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(tenantId, asyncLogProcessing);
    }

}
