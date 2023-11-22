package tech.metavm.management;

import org.springframework.stereotype.Component;
import tech.metavm.object.instance.cache.RedisCache;
import tech.metavm.util.ByteUtils;
import tech.metavm.util.NncUtils;

@Component
public class CacheManager {

    private final RedisCache cache;

    public CacheManager(RedisCache cache) {
        this.cache = cache;
    }

    public void invalidateCache(long id) {
        cache.remove(id);
    }

    public void clearCache() {
        cache.flushDB();
    }

    public void saveCacheBytes(long id) {
        var bytes = cache.get(id);
        ByteUtils.saveCacheBytes(Long.toString(id), bytes);
    }
}
