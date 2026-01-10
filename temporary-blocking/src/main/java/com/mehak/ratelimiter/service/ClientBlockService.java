package com.mehak.ratelimiter.service;

import com.mehak.ratelimiter.config.BlockConfig;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ClientBlockService {

    private final StringRedisTemplate redisTemplate;


    public ClientBlockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlocked(String fingerprint){
        String key = BlockConfig.BLOCK_KEY_PREFIX + fingerprint;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void blockClient(String fingerprint){
        String key = BlockConfig.BLOCK_KEY_PREFIX + fingerprint;
        redisTemplate.opsForValue().set(
                key,
                "1",
                BlockConfig.BLOCK_DURATION_SECONDS,
                TimeUnit.SECONDS
        );
    }
}
