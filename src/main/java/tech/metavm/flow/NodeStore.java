package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityStore;
import tech.metavm.flow.persistence.NodeMapper;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class NodeStore implements EntityStore<NodeRT> {

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private FlowStore flowStore;

    @Override
    public List<NodeRT> batchGet(Collection<Long> ids, EntityContext context) {
        List<NodePO> nodePOs =  nodeMapper.selectByIds(ids);
        Map<Long, List<NodePO>> nodeMap = NncUtils.toMultiMap(nodePOs, NodePO::getFlowId);
        List<FlowRT> flows = flowStore.batchGet(nodeMap.keySet(), context);
        Map<Long, FlowRT> flowMap = NncUtils.toMap(flows, Entity::getId);
        List<NodeRT> result = new ArrayList<>();
        for (Map.Entry<Long, List<NodePO>> entry : nodeMap.entrySet()) {
            long flowId = entry.getKey();
            List<NodePO> nodes = entry.getValue();
            FlowRT flow = flowMap.get(flowId);
            if(flow != null) {
                result.addAll(
                        NncUtils.mapAndFilter(
                                nodes,
                                node -> flow.getNode(node.getId()),
                                Objects::nonNull
                        )
                );
            }
        }
        return result;
    }

    @Override
    public void batchInsert(List<NodeRT> entities) {
        if(NncUtils.isEmpty(entities)) {
            return;
        }
        Map<NodePO, NodeRT> pairs = NncUtils.toIdentityMap(entities, NodeRT::toPO);
        nodeMapper.batchInsert(pairs.keySet());
    }

    @Override
    public int batchUpdate(List<NodeRT> entities) {
        if(NncUtils.isEmpty(entities)) {
            return 0;
        }
        return nodeMapper.batchUpdate(NncUtils.map(entities, NodeRT::toPO));
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if(NncUtils.isEmpty(ids)) {
            return;
        }
        nodeMapper.batchDelete(ids);
    }

    @Override
    public Class<NodeRT> getEntityType() {
        return NodeRT.class;
    }
}
