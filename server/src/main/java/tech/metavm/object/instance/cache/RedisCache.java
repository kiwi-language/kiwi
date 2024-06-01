package tech.metavm.object.instance.cache;

import org.springframework.stereotype.Component;
import tech.metavm.util.KeyValue;
import tech.metavm.util.NncUtils;
import tech.metavm.util.RedisRepository;

import java.util.Collection;
import java.util.List;

@Component
public class RedisCache implements Cache {

    private final RedisRepository redisRepository;

    public RedisCache(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public List<byte[]> batchGet(Collection<Long> ids) {
        return redisRepository.batchGet(NncUtils.map(ids, NncUtils::toBytes));
    }

    @Override
    public void batchAdd(List<KeyValue<Long, byte[]>> entries) {
        redisRepository.batchSet(
                NncUtils.map(
                        entries,
                        entry -> new KeyValue<>(NncUtils.toBytes(entry.key()), entry.value())
                )
        );
    }

    public void flushDB() {
        redisRepository.flushDB();
    }

    @Override
    public void batchRemove(List<Long> ids) {
        redisRepository.batchDelete(NncUtils.map(ids, NncUtils::toBytes));
    }
}
