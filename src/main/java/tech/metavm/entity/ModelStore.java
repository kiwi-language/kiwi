package tech.metavm.entity;

import java.util.List;

public interface ModelStore<T extends Model> {

    void batchInsert(List<T> entities);

    void batchDelete(List<T> entities);

    Class<T> getType();

}
