//package tech.metavm.object.meta;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.metavm.dto.Page;
//import tech.metavm.entity.*;
//import tech.metavm.object.meta.persistence.ConstraintPO;
//import tech.metavm.object.meta.persistence.mappers.ConstraintMapper;
//import tech.metavm.util.NncUtils;
//
//import java.util.*;
//
//@Component
//public class ConstraintStore implements EntityStore<ConstraintRT> {
//
//    @Autowired
//    private ConstraintMapper constraintMapper;
//
//    @Override
//    public List<EntitySupplier> load(StoreLoadRequest request, InstanceContext context) {
//        List<ConstraintPO> constraintPOs = constraintMapper.selectByIds(request.ids());
//        return EntitySupplier.fromList(
//                constraintPOs,
//                constraintPO -> () -> ConstraintFactory.createFromPO(constraintPO, context)
//        );
//    }
//
//    public List<ConstraintRT> batchGet(Collection<Long> ids, InstanceContext context, Set<LoadingOption> options) {
//        List<ConstraintPO> constraintPOs = constraintMapper.selectByIds(ids);
//        return createFromPOs(constraintPOs, context);
//    }
//
//    public Page<ConstraintRT> query(long typeId, int page, int pageSize, InstanceContext context) {
//        long total = constraintMapper.countByTypeId(typeId);
//        long start = (long) pageSize * (page - 1);
//        List<ConstraintPO> poList = constraintMapper.selectByTypeId(typeId, start, pageSize);
//        return new Page<>(createFromPOs(poList, context), total);
//    }
//
//    public Map<Long, List<Long>> getByDeclaringTypeIds(List<Long> declaringTypeIds, InstanceContext context) {
//        List<ConstraintPO> constraintPOs = constraintMapper.selectByTypeIds(declaringTypeIds);
//        context.preload(
//                ConstraintRT.class,
//                EntitySupplier.fromList(
//                        constraintPOs,
//                        constraintPO -> () -> ConstraintFactory.createFromPO(constraintPO, context)
//                )
//        );
//        return NncUtils.toMultiMap(constraintPOs, ConstraintPO::getDeclaringTypeId, ConstraintPO::getId);
//    }
//
//    private List<ConstraintRT> createFromPOs(List<ConstraintPO> constraintPOs, InstanceContext context) {
//        Set<Long> typeIds = NncUtils.mapUnique(constraintPOs, ConstraintPO::getDeclaringTypeId);
//        Map<Long, Type> typeMap = NncUtils.toEntityMap(context.batchGet(Type.class, typeIds));
//
//        List<ConstraintRT> results = new ArrayList<>();
//        for (ConstraintPO constraintPO : constraintPOs) {
//            Type type = typeMap.get(constraintPO.getDeclaringTypeId());
//            NncUtils.getAndInvoke(type, t -> t.getConstraint(constraintPO.getId()), results::add);
//        }
//        return results;
//    }
//
//    @Override
//    public void batchInsert(List<ConstraintRT> entities) {
//        constraintMapper.batchInsert(NncUtils.map(entities, ConstraintRT::toPO));
//    }
//
//    @Override
//    public int batchUpdate(List<ConstraintRT> entities) {
//        return constraintMapper.batchUpdate(NncUtils.map(entities, ConstraintRT::toPO));
//    }
//
//    @Override
//    public void batchDelete(List<ConstraintRT> entities) {
//        constraintMapper.batchDelete(NncUtils.map(entities, Entity::getId));
//    }
//
//    @Override
//    public Class<ConstraintRT> getType() {
//        return ConstraintRT.class;
//    }
//
//}
