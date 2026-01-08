package com.mehak.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate redisTemplate(
            RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }

}

//Spring does not know how you want to interact with redis.
//So this class:
//1. tells spring how to create redis related beans
//2. centralizes redis configuration
//3. keeps redis implementations out of business logic

//Since our implementation of redis does not involve JSON, entity mapping so no serialization overhead that's why we have used "StringRedisTemplate" to implement Redis.
//StringRedisTemplate is fast, simple