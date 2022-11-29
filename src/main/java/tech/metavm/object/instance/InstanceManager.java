package tech.metavm.object.instance;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.InstanceFactory;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceQueryDTO;
import tech.metavm.object.instance.rest.SelectRequestDTO;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.instance.search.SearchQuery;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

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

    public InstanceDTO get(long id) {
        IInstance instance = createContext().get(id);
        return instance != null ? instance.toDTO() : null;
    }

    public Page<Object[]> select(SelectRequestDTO request) {
        InstanceContext context = createContext();
        Type type = context.getType(request.typeId());
        SearchQuery searchQuery = new SearchQuery(
                ContextUtil.getTenantId(),
                request.typeId(),
                ExpressionParser.parse(type, request.condition()),
                request.page(),
                request.pageSize()
        );
        Page<Long> idPage = instanceSearchService.search(searchQuery);
        List<Expression> selects = NncUtils.map(request.selects(), sel -> ExpressionParser.parse(type, sel));

        GraphQueryExecutor graphQueryExecutor = new GraphQueryExecutor(context);
        return new Page<>(
                graphQueryExecutor.execute(idPage.data(), selects),
                idPage.total()
        );
    }

    public List<InstanceDTO> batchGet(List<Long> ids) {
        return batchGet(ContextUtil.getTenantId(), ids);
    }

    public List<InstanceDTO> batchGet(long tenantId, List<Long> ids) {
        return NncUtils.map(createContext(tenantId).batchGet(ids), IInstance::toDTO);
    }

    @Transactional
    public void update(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        InstanceContext context = createContext(asyncLogProcessing);
        if(instanceDTO.id() == null) {
            throw BusinessException.invalidParams("实例ID为空");
        }
        IInstance instance = context.get(instanceDTO.id());
        instance.update(instanceDTO);
        context.finish();
    }

    @Transactional
    public long create(InstanceDTO instanceDTO, boolean asyncLogProcessing) {
        InstanceContext context = createContext(asyncLogProcessing);
        Instance instance = InstanceFactory.create(instanceDTO, context);
        context.finish();
        return instance.getId();
    }

    @Transactional
    public void delete(long id, boolean asyncLogProcessing) {
        InstanceContext context = createContext(asyncLogProcessing);
        Instance instance = context.get(id);
        if(instance != null) {
            context.remove(instance);
            context.finish();
        }
    }

    public Page<InstanceDTO> query(InstanceQueryDTO query) {
        long tenantId = ContextUtil.getTenantId();
        InstanceContext context = createContext(tenantId);
        Type type = context.getType(query.typeId());
        Expression expression = ExpressionParser.parse(type, query.searchText());
        if(expression instanceof ConstantExpression) {
            Field titleField = type.getTileField();
            expression = ExpressionUtil.or(
                    ExpressionUtil.fieldStartsWith(titleField, query.searchText()),
                    ExpressionUtil.fieldLike(titleField, query.searchText())
            );
        }
        SearchQuery searchQuery = new SearchQuery(
                tenantId,
                query.typeId(),
                expression,
                query.page(),
                query.pageSize()
        );
        Page<Long> idPage = instanceSearchService.search(searchQuery);

        List<Instance> instances = context.batchGet(idPage.data());
        instanceStore.loadTitles(NncUtils.map(instances, IInstance::getId), context);
        return new Page<>(
                NncUtils.map(instances, IInstance::toDTO),
                idPage.total()
        );
    }

    @Transactional
    public void onSyncSuccess(List<InstanceLog> logs) {
        instanceStore.updateSyncVersion(NncUtils.map(logs, InstanceLog::getVersion));
    }

    private InstanceContext createContext() {
        return createContext(ContextUtil.getTenantId(), true);
    }

    private InstanceContext createContext(long tenantId) {
        return createContext(tenantId, true);
    }

    private InstanceContext createContext(boolean asyncLogProcessing) {
        return createContext(ContextUtil.getTenantId(), asyncLogProcessing);
    }

    private InstanceContext createContext(long tenantId, boolean asyncLogProcessing) {
        return instanceContextFactory.newContext(tenantId, asyncLogProcessing);
    }

}
