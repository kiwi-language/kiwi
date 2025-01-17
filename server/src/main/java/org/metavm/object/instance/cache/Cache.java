package org.metavm.object.instance.cache;

import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.util.ChangeList;
import org.metavm.util.KeyValue;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Cache {

    default void save(ChangeList<InstancePO> change) {
        var entries = new ArrayList<KeyValue<Long, byte[]>>();
        change.forEachInsertOrUpdate(i -> entries.add(new KeyValue<>(i.getId(), i.getData())));
        if(!entries.isEmpty())
            batchAdd(entries);
        if(!change.deletes().isEmpty())
            batchRemove(Utils.map(change.deletes(), InstancePO::getId));
    }

    default byte[] get(Long id) {
        return Utils.first(batchGet(List.of(id)));
    }

    default void add(Long id, byte[] value) {
        batchAdd(List.of(new KeyValue<>(id, value)));
    }

    default void remove(long id) {
        batchRemove(List.of(id));
    }

    List<byte[]> batchGet(Collection<Long> ids);

    void batchAdd(List<KeyValue<Long, byte[]>> entries);

    void clear();

    void batchRemove(List<Long> ids);

}
