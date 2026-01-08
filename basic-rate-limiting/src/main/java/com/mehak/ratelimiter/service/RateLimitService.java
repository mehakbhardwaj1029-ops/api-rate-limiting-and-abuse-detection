package com.mehak.ratelimiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private static final int WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS = 10;

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String fingerprint){

        long windowStart = Instant.now().getEpochSecond()/WINDOW_SECONDS; //creates fixed window size like: 12:00:00 – 12:00:59
        String key = "rate:"+fingerprint+":"+windowStart;

        Long count = redisTemplate.opsForValue().increment(key); //redis provides atomic, thread safe, cluster safe increment

        //ensures redis auto-cleans old windows
        if(count!=null && count==1){
            //first request in this window -> set TTL
            redisTemplate.expire(key,WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return count!=null && count <= MAX_REQUESTS;
    }
}
//This class decides that is the request allowed for this fingerprint.
//This class is the brain of rate limiting i.e; it consists of the business logic to implement rate limiting.
//RedisTemplate.opsForValue().increment(key) - multiple servers can increment the same key safely without worrying about race conditions and synchronization.

