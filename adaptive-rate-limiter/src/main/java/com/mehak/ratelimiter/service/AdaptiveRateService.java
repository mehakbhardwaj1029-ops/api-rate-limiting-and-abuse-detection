package com.mehak.ratelimiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class AdaptiveRateService {

    private final StringRedisTemplate redisTemplate;

    private static final int BURST_WINDOW = 5; //how many request client send in 2 sec
    private static final int MAX_BURST_REQUESTS = 2; //if client can send 5 request in burst_window it is considered to be a normal behaviour
    private static final int SCORE_TTL = 120;  //controls how long suspicious behaviour is remembered

    public AdaptiveRateService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int evaluateRisk(String fingerprint){
        long now = System.currentTimeMillis();

        String burstKey = "rate:adaptive:count:"+fingerprint;
        String lastReqKey = "rate:adaptive:last:"+fingerprint;
        String scoreKey = "rate:adaptive:score:"+fingerprint;

        //increment burst counter
        Long burstCount = redisTemplate.opsForValue().increment(burstKey);
        if(burstCount!=null && burstCount==1){
            redisTemplate.expire(burstKey,BURST_WINDOW,TimeUnit.SECONDS);
        }

        Long lastRequestTime = getLong(lastReqKey);
        int scoreIncrease = 0;

        //burst detection
        if(burstCount!=null && burstCount>MAX_BURST_REQUESTS){
            scoreIncrease +=2;
        }

        //fast repeated calls
        if(lastRequestTime!=null && (now-lastRequestTime)<100){
            scoreIncrease+=1;
        }

        //apply score if needed
        if(scoreIncrease >0){
            redisTemplate.opsForValue().increment(scoreKey,scoreIncrease);
            redisTemplate.expire(scoreKey,SCORE_TTL,TimeUnit.SECONDS);
        }

        //update last request time
        redisTemplate.opsForValue().set(
                lastReqKey,
                String.valueOf(now),
                SCORE_TTL,
                TimeUnit.SECONDS
        );

        Long score = getLong(scoreKey);
        return score!=null ? score.intValue():0;
    }

    private Long getLong(String key){
        String v = redisTemplate.opsForValue().get(key);
        return v!=null?Long.parseLong(v):0L;
    }
}
