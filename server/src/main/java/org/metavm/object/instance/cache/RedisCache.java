package org.metavm.object.instance.cache;

import org.metavm.util.KeyValue;
import org.metavm.util.Utils;
import org.metavm.util.RedisRepository;

import java.util.Collection;
import java.util.List;

//@Component
public class RedisCache implements Cache {

    private final RedisRepository redisRepository;

    public RedisCache(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public List<byte[]> batchGet(Collection<Long> ids) {
        return redisRepository.batchGet(Utils.map(ids, Utils::toBytes));
    }

    @Override
    public void batchAdd(List<KeyValue<Long, byte[]>> entries) {
        redisRepository.batchSet(
                Utils.map(
                        entries,
                        entry -> new KeyValue<>(Utils.toBytes(entry.key()), entry.value())
                )
        );
    }

    @Override
    public void clear() {
        redisRepository.flushDB();
    }

    @Override
    public void batchRemove(List<Long> ids) {
        redisRepository.batchDelete(Utils.map(ids, Utils::toBytes));
    }
}
