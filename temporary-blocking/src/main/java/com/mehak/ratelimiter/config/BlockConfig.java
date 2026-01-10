package com.mehak.ratelimiter.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockConfig {

    //violation tracking
    public static final int VIOLATION_WINDOW_SECONDS = 60;
    public static final int MAX_VIOLATIONS = 3;

    //block duration
    public static final int BLOCK_DURATION_SECONDS = 30;

    //redis key prefixes
    public static final String BLOCK_KEY_PREFIX = "rate:blocked:";
    public static final String VIOLATION_KEY_PREFIX = "rate:violations:";

    public BlockConfig(){}
}
