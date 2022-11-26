//package tech.metavm.object.meta;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import tech.metavm.entity.*;
//import tech.metavm.object.meta.persistence.FieldPO;
//import tech.metavm.object.meta.persistence.TypePO;
//import tech.metavm.object.meta.persistence.mappers.FieldMapper;
//import tech.metavm.object.meta.persistence.mappers.TypeMapper;
//import tech.metavm.util.NncUtils;
//
//import java.util.*;
//
//@Component
//public class FieldStore implements EntityStore<Field> {
//
//    @Autowired
//    private FieldMapper fieldMapper;
//
//    @Autowired
//    private TypeMapper typeMapper;
//
//    @Override
//    public List<EntitySupplier> load(StoreLoadRequest request, InstanceContext context) {
//        List<FieldPO> fieldPOs = NncUtils.splitAndMerge(
//                request.ids(),
//                StdTypeManager::isStandardTypeId,
//                StdTypeManager::getFieldPOs,
//                fieldMapper::selectByIds
//        );
//        for (FieldPO fieldPO : fieldPOs) {
//            context.load(Type.class, fieldPO.getDeclaringTypeId());
//            context.load(Type.class, fieldPO.getTypeId());
//        }
//        return EntitySupplier.fromList(
//                fieldPOs,
//                fieldPO -> () -> new Field(fieldPO, context)
//        );
//    }
//
//    public Map<Long, List<Long>> loadByDeclaringTypeIds(List<Long> declaringTypeIds, InstanceContext context) {
//        List<FieldPO> fieldPOs = NncUtils.splitAndMerge(
//                declaringTypeIds,
//                StdTypeManager::isStandardTypeId,
//                StdTypeManager::getFieldPOsByTypeIds,
//                fieldMapper::selectByDeclaringTypeIds
//        );
//
//        context.preload(
//                Field.class,
//                EntitySupplier.fromList(fieldPOs, fieldPO -> () -> new Field(fieldPO, context))
//        );
//        return NncUtils.toMultiMap(fieldPOs, FieldPO::getDeclaringTypeId, FieldPO::getId);
//    }
//
//    public List<Field> getByDeclaringTypeIds(Collection<Long> declaringTypeIds, InstanceContext context) {
//        List<Long> fieldIds = preloadByDeclaringTypeIds(declaringTypeIds, context);
//        return context.batchGet(Field.class, fieldIds);
//    }
//
//    public List<Long> preloadByDeclaringTypeIds(Collection<Long> declaringTypeIds, InstanceContext context) {
//        List<FieldPO> fieldPOs = NncUtils.splitAndMerge(
//                declaringTypeIds,
//                StdTypeManager::isStandardTypeId,
//                StdTypeManager::getFieldPOsByTypeIds,
//                fieldMapper::selectByDeclaringTypeIds
//        );
//        context.preload(
//                Field.class,
//                NncUtils.map(
//                        fieldPOs,
//                        fieldPO -> new EntitySupplier(fieldPO.getId(), () -> new Field(fieldPO, context))
//                )
//        );
//        return NncUtils.map(fieldPOs, FieldPO::getId);
//    }
//
//    public List<Field> batchGet(Collection<Long> ids, InstanceContext context, Set<LoadingOption> options) {
//        List<FieldPO> fieldPOs = fieldMapper.selectByIds(ids);
//        Set<Long> ownerIds = NncUtils.mapUnique(fieldPOs, FieldPO::getDeclaringTypeId);
//        List<Type> types = context.batchGet(Type.class, ownerIds, options);
//        Map<Long, Type> typeMap = NncUtils.toMap(types, Type::getId);
//        List<Field> results = new ArrayList<>();
//        for (FieldPO fieldPO : fieldPOs) {
//            Type owner = typeMap.get(fieldPO.getDeclaringTypeId());
//            Field field = NncUtils.get(owner, t -> t.getField(fieldPO.getId()));
//            if(field != null) {
//                results.add(field);
//            }
//        }
//        return results;
//    }
//
//    public List<String> getReferringFieldNames(Type type) {
//        List<FieldPO> fieldPOs = fieldMapper.selectByTypeIds(List.of(type.getId()));
//        if(NncUtils.isEmpty(fieldPOs)) {
//            return List.of();
//        }
//        Set<Long> ownerIds = NncUtils.mapUnique(fieldPOs, FieldPO::getDeclaringTypeId);
//        List<TypePO> typePOs = typeMapper.selectByIds(ownerIds);
//        Map<Long, TypePO> typePOMap = NncUtils.toMap(typePOs, TypePO::getId);
//        List<String> results = new ArrayList<>();
//        for (FieldPO fieldPO : fieldPOs) {
//            TypePO owner = typePOMap.get(fieldPO.getDeclaringTypeId());
//            if(owner != null && !owner.getId().equals(type.getId())) {
//                results.add(owner.getName() + "." + fieldPO.getName());
//            }
//        }
//        return results;
//    }
//
//    @Override
//    public void batchInsert(List<Field> fields) {
//        fieldMapper.batchInsert(NncUtils.map(fields, Field::toPO));
//    }
//
//    @Override
//    public int batchUpdate(List<Field> fields) {
//        return fieldMapper.batchUpdate(NncUtils.map(fields, Field::toPO));
//    }
//
//    @Override
//    public void batchDelete(List<Field> fields) {
//        fieldMapper.batchDelete(NncUtils.map(fields, Entity::getId));
//    }
//
//    @Override
//    public Class<Field> getType() {
//        return Field.class;
//    }
//
//}
