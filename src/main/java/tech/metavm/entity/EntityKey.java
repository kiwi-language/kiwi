package tech.metavm.entity;

public record EntityKey(
        Class<? extends Entity> type,
        long id
) {

    public static EntityKey create(Class<? extends Entity> type, long id) {
        return new EntityKey(EntityUtils.getEntityType(type), id);
    }

//    InternalKey toInternalKey() {
//        return new InternalKey(type, id);
//    }

}
