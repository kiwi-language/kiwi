//package tech.nonocode.object.instance;
//
//import tech.nonocode.constant.ColumnNames;
//import tech.nonocode.object.instance.persistence.IdSetNodePO;
//import tech.nonocode.object.instance.persistence.IdSetPO;
//import tech.nonocode.object.instance.persistence.InstancePO;
//import tech.nonocode.object.meta.NClass;
//import tech.nonocode.util.NncUtils;
//
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//public class InstanceSet extends AbsInstance {
//
//    private final InstanceContext context;
//    private final Set<Long> instanceIds;
//
//    public InstanceSet(InstancePO instancePO, List<IdSetNodePO> nodes, InstanceContext context) {
//        this(
//                instancePO.objectId(),
//                context.getModel(instancePO.modelId()),
//                NncUtils.map(nodes, IdSetNodePO::getValue),
//                context
//        );
//    }
//
//    public InstanceSet(
//            Long objectId,
//            NClass category,
//            List<Long> instanceIds,
//            InstanceContext context
//            ) {
//        super(objectId, category);
//        this.instanceIds = new LinkedHashSet<>(instanceIds);
//        this.context = context;
//    }
//
//    public NClass getElementType() {
//        return getType().getElementType();
//    }
//
//    public Set<Long> getInstanceIds() {
//        return instanceIds;
//    }
//
//    public List<Instance> getInstances() {
//        return context.batchGetInstances(instanceIds);
//    }
//
//    public boolean add(long instanceId) {
//        return instanceIds.add(instanceId);
//    }
//
//    public boolean remove(long instanceId) {
//        return instanceIds.remove(instanceId);
//    }
//
//    public List<IdSetNodePO> getNodes() {
//        return NncUtils.map(
//                instanceIds,
//                elementId -> new IdSetNodePO(getId(), elementId)
//        );
//    }
//
//    public InstancePO toPO() {
//        return new InstancePO(
//                context.getTenantId(),
//                getId(),
//                getType().getId(),
//                Map.of(
//                        ColumnNames.I0, instanceIds.size()
//                )
//        );
//    }
//
//}
