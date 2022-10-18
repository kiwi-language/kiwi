package tech.metavm.entity;

import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public interface EntityStore<T extends Entity> {

    List<T> batchGet(Collection<Long> ids, EntityContext context, EnumSet<LoadingOption> options);

    default void bulk(ChangeList<T> changeList) {
        batchInsert(changeList.inserts());
        batchUpdate(changeList.updates());
        batchDelete(NncUtils.map(changeList.deletes(), Entity::getId));
    }

    void batchInsert(List<T> entities);

    int batchUpdate(List<T> entities);

    void batchDelete(List<Long> ids);

    Class<T> getEntityType();

}
