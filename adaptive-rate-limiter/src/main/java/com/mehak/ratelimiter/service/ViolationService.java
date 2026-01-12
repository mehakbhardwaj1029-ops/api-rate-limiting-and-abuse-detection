package com.mehak.ratelimiter.service;

import com.mehak.ratelimiter.config.BlockConfig;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ViolationService {

    private final StringRedisTemplate redisTemplate;

    public ViolationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    public long recordViolation(String fingerprint){
        String key = BlockConfig.VIOLATION_KEY_PREFIX + fingerprint;

        Long count = redisTemplate.opsForValue().increment(key);

        //set ttl on 1st violation
        if(count!=null && count==1){
            redisTemplate.expire(
                    key,
                    BlockConfig.VIOLATION_WINDOW_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    return count!=null ?count:0;
    }

    public void clearViolation(String fingerprint){
        redisTemplate.delete(BlockConfig.VIOLATION_KEY_PREFIX + fingerprint);
    }
}
