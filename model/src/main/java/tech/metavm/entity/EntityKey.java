package tech.metavm.entity;

public record EntityKey(
        Class<?> type,
        long id
) {

    public static EntityKey create(Class<?> type, long id) {
        return new EntityKey(EntityUtils.getEntityType(type), id);
    }

}
