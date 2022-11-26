//package tech.metavm.flow;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.metavm.entity.*;
//import tech.metavm.flow.persistence.NodeMapper;
//import tech.metavm.flow.persistence.NodePO;
//import tech.metavm.entity.StoreLoadRequest;
//import tech.metavm.util.NncUtils;
//
//import java.util.*;
//
//@Component
//public class NodeStore implements EntityStore<NodeRT> {
//
//    @Autowired
//    private NodeMapper nodeMapper;
//
//    @Override
//    public List<EntitySupplier> load(StoreLoadRequest request, InstanceContext context) {
//        List<NodePO> nodePOs =  nodeMapper.selectByIds(request.ids());
//        return EntitySupplier.fromList(
//                nodePOs,
//                nodePO -> () -> NodeFactory.getFlowNode(nodePO, context)
//        );
//    }
//
//    public Map<Long, List<Long>> getByFlowIds(List<Long> flowIds, InstanceContext context) {
//        List<NodePO> nodePOs =  nodeMapper.selectByFlowIds(flowIds);
//        context.preload(
//                NodeRT.class,
//                nodePOs
//        );
//        return NncUtils.toMultiMap(nodePOs, NodePO::getFlowId, NodePO::getId);
//    }
//
//    public List<NodeRT> batchGet(Collection<Long> ids, InstanceContext context, Set<LoadingOption> options) {
//        List<NodePO> nodePOs =  nodeMapper.selectByIds(ids);
//        Map<Long, List<NodePO>> nodeMap = NncUtils.toMultiMap(nodePOs, NodePO::getFlowId);
//        List<FlowRT> flows = context.batchGet(FlowRT.class, nodeMap.keySet());
//        Map<Long, FlowRT> flowMap = NncUtils.toMap(flows, Entity::getId);
//        List<NodeRT> result = new ArrayList<>();
//        for (Map.Entry<Long, List<NodePO>> entry : nodeMap.entrySet()) {
//            long flowId = entry.getKey();
//            List<NodePO> nodes = entry.getValue();
//            FlowRT flow = flowMap.get(flowId);
//            if(flow != null) {
//                result.addAll(
//                        NncUtils.mapAndFilter(
//                                nodes,
//                                node -> flow.getNode(node.getId()),
//                                Objects::nonNull
//                        )
//                );
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public void batchInsert(List<NodeRT> entities) {
//        if(NncUtils.isEmpty(entities)) {
//            return;
//        }
//        Map<NodePO, NodeRT> pairs = NncUtils.toIdentityMap(entities, NodeRT::toPO);
//        nodeMapper.batchInsert(pairs.keySet());
//    }
//
//    @Override
//    public int batchUpdate(List<NodeRT> entities) {
//        if(NncUtils.isEmpty(entities)) {
//            return 0;
//        }
//        return nodeMapper.batchUpdate(NncUtils.map(entities, NodeRT::toPO));
//    }
//
//    @Override
//    public void batchDelete(List<NodeRT> nodes) {
//        if(NncUtils.isEmpty(nodes)) {
//            return;
//        }
//        nodeMapper.batchDelete(NncUtils.map(nodes, Entity::getId));
//    }
//
//    @Override
//    public Class<NodeRT> getType() {
//        return NodeRT.class;
//    }
//}
