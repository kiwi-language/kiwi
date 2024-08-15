package org.metavm.system;

import org.metavm.object.instance.cache.LocalCache;
import org.metavm.util.BytesUtils;
import org.springframework.stereotype.Component;

@Component
public class CacheManager {

//    private final RedisCache cache;
    private final LocalCache cache;

    public CacheManager(LocalCache cache) {
        this.cache = cache;
    }

    public void invalidateCache(long id) {
        cache.remove(id);
    }

    public void clearCache() {
        cache.clear();
    }

    public void saveCacheBytes(long id) {
        var bytes = cache.get(id);
        BytesUtils.saveCacheBytes(Long.toString(id), bytes);
    }
}
