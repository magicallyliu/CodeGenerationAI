package com.liuh.codegenerationbackend.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@SpringBootTest
@Slf4j
public class RedisConnectionTest {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConnectionTest.class);
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;
    
    @Test
    public void testRedisConnection() {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            if (redisPassword != null && !redisPassword.isEmpty()) {
                jedis.auth(redisPassword);
            }
            String result = jedis.ping();
            log.info("Redis connection test: {}", result);
        } catch (Exception e) {
            log.error("Redis connection failed: {}", e.getMessage());
        }
    }
}