//package tech.metavm.flow;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.metavm.dto.Page;
//import tech.metavm.entity.*;
//import tech.metavm.flow.persistence.FlowMapper;
//import tech.metavm.flow.persistence.FlowPO;
//import tech.metavm.flow.rest.FlowQuery;
//import tech.metavm.entity.StoreLoadRequest;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.NncUtils;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@Component
//public class FlowStore implements EntityStore<FlowRT> {
//
//    @Autowired
//    private FlowMapper flowMapper;
//
//    @Override
//    public List<EntitySupplier> load(StoreLoadRequest request, InstanceContext context) {
//        List<FlowPO> flowPOs =  flowMapper.selectByIds(request.ids());
//        return EntitySupplier.fromList(
//                flowPOs,
//                flowPO -> () -> new FlowRT(flowPO, context)
//        );
//    }
//
//    public Page<FlowRT> query(FlowQuery queryDTO, InstanceContext context) {
//        long total = flowMapper.count(queryDTO);
//        List<FlowPO> flowPOs = flowMapper.query(queryDTO);
//        if(NncUtils.isEmpty(flowPOs)) {
//            return new Page<>(
//                    List.of(),
//                    0
//            );
//        }
//        return new Page<>(
//                NncUtils.map(flowPOs, po -> new FlowRT(po, context)),
//                total
//        );
//    }
//
//    public List<String> getReferringFlowNames(Type type) {
//        List<FlowPO> flowPOs = flowMapper.selectByInputTypeIds(List.of(type.getId()));
//        return NncUtils.filterAndMap(
//                flowPOs,
//                f -> !f.getTypeId().equals(type.getId()),
//                FlowPO::getName
//        );
//    }
//
//    public List<FlowRT> getByOwner(Type owner) {
//        List<FlowPO> flowPOs =  flowMapper.selectByOwnerIds(List.of(owner.getId()));
//        return createFromPOs(flowPOs, owner.getContext());
//    }
//
//    public List<FlowRT> batchGet(Collection<Long> ids, InstanceContext context, Set<LoadingOption> options) {
//        if(NncUtils.isEmpty(ids)) {
//            return List.of();
//        }
//        List<FlowPO> flowPOs =  flowMapper.selectByIds(ids);
//        return createFromPOs(flowPOs, context);
//    }
//
//    private List<FlowRT> createFromPOs(List<FlowPO> flowPOs, InstanceContext context) {
//        return NncUtils.map(
//                flowPOs,
//                flowPO -> new FlowRT(flowPO, context)
//        );
//    }
//
////    private List<FlowRT> createFromPOs(List<FlowPO> flowPOs, EntityContext context) {
////        if(NncUtils.isEmpty(flowPOs)) {
////            return List.of();
////        }
////        List<Long> ids = NncUtils.map(flowPOs, FlowPO::getId);
////        List<NodePO> nodePOs = nodeMapper.selectByFlowIds(ids);
////        Map<Long, List<NodePO>> nodePOMap = NncUtils.toMultiMap(nodePOs, NodePO::getFlowId);
////        List<ScopePO> scopePOs = scopeMapper.selectByFlowIds(ids);
////        Map<Long, List<ScopePO>> scopePOMap = NncUtils.toMultiMap(scopePOs, ScopePO::getFlowId);
////
////        List<FlowRT> results = new ArrayList<>();
////        for (FlowPO flowPO : flowPOs) {
////            results.add(
////                    new FlowRT(
////                            flowPO,
////                            scopePOMap.get(flowPO.getId()),
////                            nodePOMap.get(flowPO.getId()),
////                            context
////                    )
////            );
////        }
////        return results;
////    }
//
//    @Override
//    public void batchInsert(List<FlowRT> flows) {
//        if(NncUtils.isEmpty(flows)) {
//            return;
//        }
//        Map<FlowPO, FlowRT> toInsertMap = NncUtils.toIdentityMap(flows, FlowRT::toPO);
//        flowMapper.batchInsert(toInsertMap.keySet());
//    }
//
//    @Override
//    public int batchUpdate(List<FlowRT> entities) {
//        if(NncUtils.isEmpty(entities)) {
//            return 0;
//        }
//        return flowMapper.batchUpdate(NncUtils.map(entities, FlowRT::toPO));
//    }
//
//    @Override
//    public void batchDelete(List<FlowRT> entities) {
//        if(NncUtils.isEmpty(entities)) {
//            return;
//        }
//        flowMapper.batchDelete(NncUtils.map(entities, Entity::getId));
//    }
//
//    @Override
//    public Class<FlowRT> getType() {
//        return FlowRT.class;
//    }
//}
