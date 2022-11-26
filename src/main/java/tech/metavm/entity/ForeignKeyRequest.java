//package tech.metavm.entity;
//
//import tech.metavm.util.NncUtils;
//
//import java.util.List;
//
//public record ForeignKeyRequest (
//        ForeignKey foreignKey
//) {
//
//    public static List<ForeignKeyRequest> create(ForeignKeyDef<?> def, List<Long> refIds) {
//        return NncUtils.map(refIds, refId -> new ForeignKeyRequest(new ForeignKey(def, refId)));
//    }
//
//    public Class<? extends Entity> getEntityType() {
//        return foreignKey.def().getEntityType();
//    }
//
//    public ForeignKeyDef<?> getForeignKeyDef() {
//        return foreignKey.def();
//    }
//
//    public long getRefId() {
//        return foreignKey.id();
//    }
//
//}
