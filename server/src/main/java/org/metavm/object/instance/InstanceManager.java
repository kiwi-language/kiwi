package org.metavm.object.instance;

import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionParser;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.object.instance.query.GraphQueryExecutor;
import org.metavm.object.instance.query.InstanceNode;
import org.metavm.object.instance.query.Path;
import org.metavm.object.instance.query.PathTree;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.ValueFormatter;
import org.metavm.system.RegionConstants;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InstanceManager extends EntityContextFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(InstanceManager.class);

    private final InstanceStore instanceStore;

    private final InstanceQueryService instanceQueryService;

    private final MetaContextCache metaContextCache;

    public InstanceManager(EntityContextFactory entityContextFactory, InstanceStore instanceStore, InstanceQueryService instanceQueryService, MetaContextCache metaContextCache) {
        super(entityContextFactory);
        this.instanceStore = instanceStore;
        this.instanceQueryService = instanceQueryService;
        this.metaContextCache = metaContextCache;
    }

    @Transactional(readOnly = true)
    public GetInstanceResponse get(String id, int depth) {
        try (var context = newInstanceContext()) {
            var instanceId = Id.parse(id);
            var instance = context.get(instanceId).getReference();
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
                    graphQueryExecutor.execute(klass, NncUtils.map(roots, Reference::resolve), selects),
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
                        return new GetInstanceResponse(InstanceDTOBuilder.buildDTO(view.getReference(), 1));
                    }
                }
                return new GetInstanceResponse(InstanceDTOBuilder.buildDTO(instance.getReference(), 1));
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
                var instanceDTOs = NncUtils.map(instances, i -> InstanceDTOBuilder.buildDTO(i.getReference(), depth));
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

    public Value update(InstanceDTO instanceDTO, IInstanceContext context) {
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

    public Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return InstanceFactory.create(instanceDTO, context).resolve();
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
                    inst.forEachDescendant(instance -> {
                        ids.add(instance.getId());
                        trees.put(instance.getId(), tree);
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
                                NncUtils.map(dataPage1.data(), r -> r.resolve().toDTO()),
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
            Map<Value, InstanceNode<?>> instance2node = buildObjectTree(paths, context);
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            graphQueryExecutor.loadTree(instance2node);

            Set<Value> visited = new IdentitySet<>();
            List<InstanceDTO> result = new ArrayList<>();
            for (Path path : paths) {
                var instanceId = Id.parse(path.firstItem().substring(Constants.ID_PREFIX.length()));
                Value instance = context.get(instanceId).getReference();
                InstanceNode<?> node = Objects.requireNonNull(instance2node.get(instance),
                        () -> "Can not find node for instance " + instance);
                List<Value> values = node.getFetchResults(instance, path.subPath());
                for (Value value : values) {
                    if (!visited.contains(value)) {
                        visited.add(value);
                        result.add(value.toDTO());
                    }
                }
            }
            return result;
        }
    }

    private Map<Value, InstanceNode<?>> buildObjectTree(List<Path> paths, IInstanceContext context) {
        Map<Value, PathTree> pathTreeMap = new HashMap<>();
        for (Path path : paths) {
            Value instance = context.get(Id.parse(path.firstItem().substring(Constants.ID_PREFIX.length()))).getReference();
            PathTree pathTree = pathTreeMap.computeIfAbsent(instance, k -> new PathTree(k + ""));
            if (path.hasSubPath()) {
                pathTree.addPath(path.subPath());
            }
        }
        Map<Value, InstanceNode<?>> objectTreeMap = new HashMap<>();
        pathTreeMap.forEach((instance, pathTree) ->
                objectTreeMap.put(instance, InstanceNode.create(pathTree, instance.getType()))
        );
        return objectTreeMap;
    }

    @Override
    public IEntityContext newContext() {
        var appId = ContextUtil.getAppId();
        return entityContextFactory.newContext(appId, metaContextCache.get(appId));
    }

    private IInstanceContext newInstanceContext() {
        //noinspection resource
        return newContext().getInstanceContext();
    }

}
