package tech.metavm.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    private final String hostName;
    private final int port;

    public RedisConfig(@Value("${spring.data.redis.host}") String hostName, @Value("${spring.data.redis.port}") int port) {
        this.hostName = hostName;
        this.port = port;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        var config = new RedisStandaloneConfiguration(hostName, port);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        var template = new RedisTemplate<Object, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
