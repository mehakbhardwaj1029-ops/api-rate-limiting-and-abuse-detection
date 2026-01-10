package com.mehak.ratelimiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class RateLimitService {

    private final DefaultRedisScript<Long> tokenBucketScript;
    private static final int MAX_TOKENS = 5;
    private static final int REFILL_INTERVAL_SECONDS = 10;

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate, DefaultRedisScript<Long> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    public boolean allowRequest(String fingerprint){

        String tokenKey = "rate:bucket:" +fingerprint+":tokens";
        String timeKey = "rate:bucket:"+fingerprint+":time";

        Long result = redisTemplate.execute(tokenBucketScript,
                                             List.of(tokenKey,timeKey),
                                             String.valueOf(MAX_TOKENS),
                                             String.valueOf(REFILL_INTERVAL_SECONDS),
                                             String.valueOf(Instant.now().getEpochSecond()));
        return result!=null && result==1;
    }

}

