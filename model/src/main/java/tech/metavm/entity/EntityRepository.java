package tech.metavm.entity;

public interface EntityRepository extends EntityProvider {

    <T> T bind(T entity);

    boolean tryBind(Object entity);

    boolean remove(Object object);

}
