//package tech.metavm.entity;
//
//import tech.metavm.util.NncUtils;
//
//public record ForeignKey(
//        ForeignKeyDef def,
//        long id
//) {
//
//    public void check(Class<?> entityType) {
//        NncUtils.requireEquals(entityType, def.getEntityType(),
//                "Foreign key definition '" + def + " doesn't belong to entity type: " + entityType.getName());
//    }
//
//}
