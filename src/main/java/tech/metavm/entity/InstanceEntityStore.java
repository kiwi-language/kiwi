//package tech.metavm.entity;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.metavm.dto.Page;
//import tech.metavm.object.instance.AbsInstance;
//import tech.metavm.object.instance.core.Instance;
//import tech.metavm.object.instance.core.InstanceContext;
//import tech.metavm.object.instance.InstanceQueryService;
//import tech.metavm.object.instance.persistence.IndexKeyPO;
//import tech.metavm.object.instance.rest.InstanceQueryDTO;
//import tech.metavm.object.meta.StdTypeConstants;
//import tech.metavm.entity.StoreLoadRequest;
//import tech.metavm.util.ContextUtil;
//import tech.metavm.util.NncUtils;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@Component
//public class InstanceEntityStore implements EntityStore<InstanceEntity> {
//
//    @Autowired
//    private InstanceQueryService instanceQueryService;
//
//    @Override
//    public List<EntitySupplier> load(StoreLoadRequest request, EntityContext context) {
//        InstanceContext instanceContext = context.getInstanceContext();
//        List<Instance> instances = instanceContext.batchGet(request.ids());
//        return EntitySupplier.fromList(
//                instances,
//                instance -> () -> InstanceFactory.create(instance, InstanceEntity.class)
//        );
//    }
//
//    public <T extends InstanceEntity> Page<T> query(Class<T> entityType,
//                                                    int page,
//                                                    int pageSize,
//                                                    String searchText,
//                                                    EntityContext context) {
//        Page<Long> idPage = instanceQueryService.query(
//                ContextUtil.getTenantId(),
//                new InstanceQueryDTO(
//                        StdTypeConstants.USER.ID,
//                        searchText,
//                        page,
//                        pageSize
//                )
//        );
//        return new Page<>(
//                batchGet(entityType, idPage.data(), context, LoadingOption.none()),
//                idPage.total()
//        );
//    }
//
//    public List<InstanceEntity> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
//        return batchGet(InstanceEntity.class, ids, context, options);
//    }
//
//    public <T extends InstanceEntity> List<T> batchGet(Class<T> entityType,
//                                                       Collection<Long> ids,
//                                                       EntityContext context,
//                                                       Set<LoadingOption> options) {
//        context.load(InstanceEntity.class, ids, options);
//        return context.batchGet(entityType, ids);
////        InstanceContext instanceContext = context.getInstanceContext();
////        List<Instance> instances = instanceContext.batchGet(ids);
////        return createFromInstances(instances, entityType);
//    }
//
//    public Map<Long, List<Long>> getByTypeIds(Collection<Long> typeIds,
//                                              EntityContext context) {
//        InstanceContext instanceContext = context.getInstanceContext();
//        List<Instance> instances = instanceContext.getByTypeIds(typeIds);
////        context.preload(
////                entityType,
////                EntitySupplier.fromList(
////                        instances,
////                        instance -> () -> InstanceEntityFactory.create(instance, entityType)
////                )
////        );
//        return NncUtils.toMultiMap(instances, instance -> instance.getType().getId(), AbsInstance::getId);
//    }
//
//    public <T extends InstanceEntity> List<T> selectByKey(Class<T> entityType, IndexKeyPO indexKey, EntityContext context) {
//        List<Instance> instances = context.getInstanceContext().getByKey(indexKey);
//        return context.batchGet(entityType, NncUtils.map(instances, AbsInstance::getId));
//    }
//
//    @Override
//    public void batchInsert(List<InstanceEntity> entities) {
//        NncUtils.forEach(entities, InstanceEntity::saveToContext);
//    }
//
//    @Override
//    public int batchUpdate(List<InstanceEntity> entities) {
//        NncUtils.forEach(entities, InstanceEntity::saveToContext);
//        return entities.size();
//    }
//
//    @Override
//    public void batchDelete(List<InstanceEntity> entities) {
//        entities.forEach(InstanceEntity::removeFromContext);
//    }
//
//    public Class<InstanceEntity> getType() {
//        return InstanceEntity.class;
//    }
//}
