package com.mehak.ratelimiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;


@Service
public class RateLimitService {

    class TokenBucketState{
        long tokens;
        long lastRefillTime;

        TokenBucketState(long tokens,long lastRefillTime){
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }

    private static final int MAX_TOKENS = 60;
    private static final int REFILL_INTERVAL_SECONDS = 6;

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String fingerprint){

         TokenBucketState bucket = loadBucket(fingerprint);

         refillBucket(bucket);

         if(!hasToken(bucket)){
             saveBucket(fingerprint,bucket);
             return false;
         }

         consumeToken(bucket);
         saveBucket(fingerprint,bucket);
         return true;

    }

    private TokenBucketState loadBucket(String fingerprint) {

        String tokenKey = tokenKey(fingerprint);
        String timeKey = timeKey(fingerprint);

        Long tokens = getLongFromRedis(tokenKey);
        Long lastRefill = getLongFromRedis(timeKey);

        if (tokens == null || lastRefill == null) {
            // First request → full bucket
            return new TokenBucketState(MAX_TOKENS,Instant.now().getEpochSecond());
        }

        return new TokenBucketState(tokens, lastRefill);

    }

    private Long getLongFromRedis(String key) {

        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : null;

    }

    private void refillBucket(TokenBucketState bucket) {

        long now = Instant.now().getEpochSecond();
        long elapsedSeconds = now - bucket.lastRefillTime;

        long tokensToAdd = elapsedSeconds / REFILL_INTERVAL_SECONDS;
        //if the user is idle for long time and time between last and current request keeps increasing ,user will still receive the same amount of tokens as the user who was continuously sending request
        if (tokensToAdd > 0) {
            bucket.tokens = Math.min(MAX_TOKENS, bucket.tokens + tokensToAdd);
            bucket.lastRefillTime += tokensToAdd * REFILL_INTERVAL_SECONDS;
        }
    }

    private boolean hasToken(TokenBucketState bucket) {

        return bucket.tokens > 0;
    }

    private void consumeToken(TokenBucketState bucket) {

        bucket.tokens--;

    }

    private void saveBucket(String fingerprint, TokenBucketState bucket) {

        redisTemplate.opsForValue().set(tokenKey(fingerprint), String.valueOf(bucket.tokens));
        redisTemplate.opsForValue().set(timeKey(fingerprint), String.valueOf(bucket.lastRefillTime));

        redisTemplate.expire(tokenKey(fingerprint), 2, TimeUnit.MINUTES);
        redisTemplate.expire(timeKey(fingerprint), 2, TimeUnit.MINUTES);
    }
    //separating tokens and last refill information to create 2 different independent address in redis ,so redis can operate on them atomically and safely
    private String timeKey(String fingerprint) {
        return "bucket:" +fingerprint+ ":tokens";
    }

    private String tokenKey(String fingerprint) {
        return "bucket:" +fingerprint+ ":lastRefill";
    }
}

