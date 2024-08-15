package org.metavm.object.instance.cache;

import com.google.common.cache.CacheBuilder;
import org.metavm.util.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class LocalCache implements Cache {

    public static final Logger logger = LoggerFactory.getLogger(LocalCache.class);

    public static final long MAX_SIZE = 1000000;

    private final com.google.common.cache.Cache<Long, byte[]> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

    @Override
    public List<byte[]> batchGet(Collection<Long> ids) {
        var hits = cache.getAllPresent(ids);
        var result = new ArrayList<byte[]>();
        for (Long id : ids) {
            result.add(hits.get(id));
        }
        return result;
    }

    @Override
    public void batchAdd(List<KeyValue<Long, byte[]>> entries) {
        entries.forEach(e -> cache.put(e.key(), e.value()));
//        cache.invalidateAll(NncUtils.map(entries, KeyValue::key));
    }

    @Override
    public void batchRemove(List<Long> ids) {
        cache.invalidateAll(ids);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
