package tech.metavm.object.instance.cache;

import tech.metavm.util.KeyValue;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public interface Cache {

    default byte[] get(Long id) {
        return NncUtils.first(batchGet(List.of(id)));
    }

    default void add(Long id, byte[] value) {
        batchAdd(List.of(new KeyValue<>(id, value)));
    }

    default void remove(long id) {
        batchRemove(List.of(id));
    }

    List<byte[]> batchGet(Collection<Long> ids);

    void batchAdd(List<KeyValue<Long, byte[]>> entries);

    void batchRemove(List<Long> ids);

}
