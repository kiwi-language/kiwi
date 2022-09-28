package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.dto.Page;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.flow.persistence.*;
import tech.metavm.flow.rest.FlowQuery;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<FlowRT> batchGet(Collection<Long> ids, EntityContext context) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<FlowPO> flowPOs =  flowMapper.selectByIds(ids);
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
