package tech.metavm.object.instance.cache;

import junit.framework.TestCase;
import org.junit.Assert;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;
import tech.metavm.util.RedisRepository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class RedisCacheTest extends TestCase {

    private RedisCache cache;

    @Override
    protected void setUp() throws Exception {
        var config = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        var poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(8);
        poolConfig.setMaxTotal(8);
        poolConfig.setTestOnBorrow(true);
        var clientConfig = JedisClientConfiguration.builder()
                .usePooling()
                .poolConfig(poolConfig)
                .and()
                .connectTimeout(Duration.ofMillis(10000))
                .build();
        var jedisConnectionFactory = new JedisConnectionFactory(config, clientConfig);
        jedisConnectionFactory.afterPropertiesSet();
        var redisTemplate= new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        var repository = new RedisRepository(redisTemplate);
        cache = new RedisCache(repository);
    }

    public void test() throws InterruptedException {
        cache.add(1L, "Leen".getBytes(StandardCharsets.UTF_8));
        cache.add(3L, "Lyq".getBytes(StandardCharsets.UTF_8));
        var values = cache.batchGet(List.of(1L, 2L, 3L));
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("Leen", new String(values.get(0), StandardCharsets.UTF_8));
        Assert.assertNull(values.get(1));
        Assert.assertEquals("Lyq", new String(values.get(2), StandardCharsets.UTF_8));
    }

}