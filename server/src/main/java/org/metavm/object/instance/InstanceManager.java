package org.metavm.object.instance;

import org.metavm.common.Page;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionParser;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.query.GraphQueryExecutor;
import org.metavm.object.instance.query.InstanceNode;
import org.metavm.object.instance.query.Path;
import org.metavm.object.instance.query.PathTree;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
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

import static java.util.Objects.requireNonNull;

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
        try (var context = newContext()) {
            var instanceId = Id.parse(id);
            var instance = context.get(instanceId).getReference();
            var instanceDTO = InstanceDTOBuilder.buildDTO(instance, depth);
            return new GetInstanceResponse(instanceDTO);
        }
    }

    public Page<InstanceDTO[]> select(SelectRequest request) {
        try (var entityContext = newContext()) {
            var klass = ((ClassType) TypeParser.parseType(request.type(), entityContext.getTypeDefProvider())).getKlass();
            var dataPage = instanceQueryService.query(InstanceQueryBuilder.newBuilder(klass)
                    .expression(request.condition())
                    .page(request.page())
                    .pageSize(request.pageSize())
                    .build(), entityContext
            );
            var roots = dataPage.data();
            List<Expression> selects = Utils.map(request.selects(), sel -> ExpressionParser.parse(klass, sel, entityContext));
            GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor();
            return new Page<>(
                    graphQueryExecutor.execute(klass, Utils.map(roots, Reference::get), selects),
                    dataPage.total()
            );
        }
    }

    public List<TreeDTO> getTrees(List<Long> ids) {
        try (var context = newContext()) {
            var systemIds = Utils.filter(ids, RegionConstants::isSystemId);
            var nonSystemIds = Utils.exclude(ids, RegionConstants::isSystemId);
            var systemInstances = instanceStore.loadForest(systemIds, ModelDefRegistry.getDefContext());
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
        try (var context = newContext()) {
            return instanceStore.getVersions(ids, context);
        }
    }

    @Transactional(readOnly = true)
    public GetInstancesResponse batchGet(List<String> ids, int depth) {
        try (var context = newContext()) {
            return batchGet(ids, depth, context);
        }
    }

    public GetInstancesResponse batchGet(List<String> ids, int depth, IInstanceContext context) {
        var instanceIds = Utils.map(ids, Id::parse);
        try (var ignored = ContextUtil.getProfiler().enter("batchGet")) {
            var instances = context.batchGet(instanceIds);
            context.buffer(instanceIds);
            try (var ignored1 = SerializeContext.enter()) {
                var instanceDTOs = Utils.map(instances, i -> InstanceDTOBuilder.buildDTO(i.getReference(), depth));
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
            update(instanceDTO, context);
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
            var instance = create(instanceDTO, context);
            context.finish();
            return requireNonNull(instance.tryGetId()).toString();
        }
    }

    public Instance create(InstanceDTO instanceDTO, IInstanceContext context) {
        return InstanceFactory.create(instanceDTO, context).get();
    }

    public QueryInstancesResponse query(InstanceQueryDTO query) {
        try (var entityContext = newContext()) {
            var klass = requireNonNull(entityContext.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(query.type())));
            var classType = klass.getType();
            var internalQuery = InstanceQueryBuilder.newBuilder(classType.getKlass())
                    .searchText(query.searchText())
                    .newlyCreated(Utils.map(query.createdIds(), Id::parse))
                    .fields(Utils.map(query.fields(), f -> InstanceQueryField.create(f, entityContext)))
                    .expression(query.expression())
                    .page(query.page())
                    .pageSize(query.pageSize())
                    .build();
            var dataPage1 = instanceQueryService.query(internalQuery, entityContext);
            return new QueryInstancesResponse(
                    new Page<>(
                            Utils.map(dataPage1.data(), r -> r.get().getStringId()),
                            dataPage1.total()
                    )
            );
        }
    }

    public List<InstanceDTO> loadByPaths(LoadInstancesByPathsRequest request) {
        try (var context = newContext()) {
            List<Path> paths = Utils.map(request.paths(), Path::create);
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
                objectTreeMap.put(instance, InstanceNode.create(pathTree, instance.getValueType()))
        );
        return objectTreeMap;
    }

    @Override
    public IInstanceContext newContext() {
        var appId = ContextUtil.getAppId();
        return entityContextFactory.newContext(appId, metaContextCache.get(appId));
    }

}
