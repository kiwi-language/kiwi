package tech.metavm.entity;

import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface EntityStore<T extends Entity> {

    List<T> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options);

    default void bulk(ChangeList<T> changeList) {
        batchInsert(changeList.inserts());
        batchUpdate(changeList.updates());
        batchDelete(changeList.deletes());
    }

    void batchInsert(List<T> entities);

    int batchUpdate(List<T> entities);

    void batchDelete(List<T> entities);

    Class<T> getEntityType();

}
