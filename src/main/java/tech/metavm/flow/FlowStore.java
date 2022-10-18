package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.entity.LoadingOption;
import tech.metavm.flow.persistence.*;
import tech.metavm.flow.rest.FlowQuery;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class FlowStore implements EntityStore<FlowRT> {

    @Autowired
    private FlowMapper flowMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private ScopeMapper scopeMapper;

    public Page<FlowRT> query(FlowQuery queryDTO, EntityContext context) {
        long total = flowMapper.count(queryDTO);
        List<FlowPO> flowPOs = flowMapper.query(queryDTO);
        if(NncUtils.isEmpty(flowPOs)) {
            return new Page<>(
                    List.of(),
                    0
            );
        }
        List<Long> scopeIds = NncUtils.map(flowPOs, FlowPO::getRootScopeId);
        List<ScopePO> scopePOs = scopeMapper.selectByIds(scopeIds);
        Map<Long, List<ScopePO>> scopePOMap = NncUtils.toMultiMap(scopePOs, ScopePO::getFlowId);

        return new Page<>(
                NncUtils.map(flowPOs, po -> new FlowRT(po, scopePOMap.get(po.getId()), List.of(), context)),
                total
        );
    }

    public List<String> getReferringFlowNames(Type type) {
        List<FlowPO> flowPOs = flowMapper.selectByInputTypeIds(List.of(type.getId()));
        return NncUtils.filterAndMap(
                flowPOs,
                f -> !f.getTypeId().equals(type.getId()),
                FlowPO::getName
        );
    }

    public List<FlowRT> getByOwner(Type owner) {
        List<FlowPO> flowPOs =  flowMapper.selectByOwnerIds(List.of(owner.getId()));
        return createFromPOs(flowPOs, owner.getContext());
    }

    @Override
    public List<FlowRT> batchGet(Collection<Long> ids, EntityContext context, EnumSet<LoadingOption> options) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<FlowPO> flowPOs =  flowMapper.selectByIds(ids);
        return createFromPOs(flowPOs, context);
    }

    private List<FlowRT> createFromPOs(List<FlowPO> flowPOs, EntityContext context) {
        if(NncUtils.isEmpty(flowPOs)) {
            return List.of();
        }
        List<Long> ids = NncUtils.map(flowPOs, FlowPO::getId);
        List<NodePO> nodePOs = nodeMapper.selectByFlowIds(ids);
        Map<Long, List<NodePO>> nodePOMap = NncUtils.toMultiMap(nodePOs, NodePO::getFlowId);
        List<ScopePO> scopePOs = scopeMapper.selectByFlowIds(ids);
        Map<Long, List<ScopePO>> scopePOMap = NncUtils.toMultiMap(scopePOs, ScopePO::getFlowId);

        List<FlowRT> results = new ArrayList<>();
        for (FlowPO flowPO : flowPOs) {
            results.add(
                    new FlowRT(
                            flowPO,
                            scopePOMap.get(flowPO.getId()),
                            nodePOMap.get(flowPO.getId()),
                            context
                    )
            );
        }
        return results;
    }

    @Override
    public void bulk(ChangeList<FlowRT> changeList) {
        batchInsert(changeList.inserts());
        batchUpdate(changeList.updates());
        batchDelete(NncUtils.map(changeList.deletes(), Entity::getId));
    }

    @Override
    public void batchInsert(List<FlowRT> flows) {
        if(NncUtils.isEmpty(flows)) {
            return;
        }
        Map<FlowPO, FlowRT> toInsertMap = NncUtils.toIdentityMap(flows, FlowRT::toPO);
        flowMapper.batchInsert(toInsertMap.keySet());
    }

    @Override
    public int batchUpdate(List<FlowRT> entities) {
        if(NncUtils.isEmpty(entities)) {
            return 0;
        }
        return flowMapper.batchUpdate(NncUtils.map(entities, FlowRT::toPO));
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if(NncUtils.isEmpty(ids)) {
            return;
        }
        flowMapper.batchDelete(ids);
    }

    @Override
    public Class<FlowRT> getEntityType() {
        return FlowRT.class;
    }
}
