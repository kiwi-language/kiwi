package org.metavm.object.instance.cache;

import org.metavm.util.KeyValue;
import org.metavm.util.NncUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockCache implements Cache {

    private final Map<Long, byte[]> map = new HashMap<>();

    @Override
    public List<byte[]> batchGet(Collection<Long> ids) {
        return NncUtils.map(ids, map::get);
    }

    @Override
    public void batchAdd(List<KeyValue<Long, byte[]>> entries) {
        for (var entry : entries) {
            map.put(entry.key(), entry.value());
        }
    }

    @Override
    public void batchRemove(List<Long> ids) {
        ids.forEach(map::remove);
    }
}
