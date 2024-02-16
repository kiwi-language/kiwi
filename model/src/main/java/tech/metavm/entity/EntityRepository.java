package tech.metavm.entity;

public interface EntityRepository extends EntityProvider {

    <T> T bind(T entity);

    boolean tryBind(Object entity);

    boolean remove(Object object);

    boolean containsModel(Object object);

    default void rebind(Object entity) {
        EntityUtils.forEachDescendant(entity, e -> {
            if(!containsModel(e))
                tryBind(e);
        });
    }

}
