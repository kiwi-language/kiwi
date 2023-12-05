package tech.metavm.util;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisRepository(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<byte[]> batchGet(List<byte[]> keys) {
        if (keys.isEmpty())
            return List.of();
        try (var ignored = ContextUtil.getProfiler().enter("RedisRepository.batchGet")) {
            //noinspection unchecked,rawtypes
            return (List) redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                keys.forEach(connection.stringCommands()::get);
                return null;
            }, new Serializer());
        }
    }

    public void batchSet(List<KeyValue<byte[], byte[]>> entries) {
        if (entries.isEmpty())
            return;
        try (var ignored = ContextUtil.getProfiler().enter("RedisRepository.batchSet")) {
            redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                entries.forEach(e -> connection.stringCommands().set(e.key(), e.value()));
                return null;
            }, new Serializer());
        }
    }

    public void batchDelete(List<byte[]> keys) {
        if (keys.isEmpty())
            return;
        try (var ignored = ContextUtil.getProfiler().enter("RedisRepository.batchDelete")) {
            redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                keys.forEach(key -> connection.keyCommands().del(key));
                return null;
            });
        }
    }

    public void flushDB() {
        redisTemplate.execute((RedisCallback<?>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }

    public static class Serializer implements RedisSerializer<byte[]> {
        @Override
        public byte[] serialize(byte[] value) throws SerializationException {
            return value;
        }

        @Override
        public byte[] deserialize(byte[] bytes) throws SerializationException {
            return bytes;
        }
    }


}
