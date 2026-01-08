package com.mehak.ratelimiter.filter;

import com.mehak.ratelimiter.service.RateLimitService;
import com.mehak.ratelimiter.util.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String fingerprint = RequestContext.getFingerprint(request);

        if(fingerprint!=null){
            boolean allowed = rateLimitService.isAllowed(fingerprint);

            if(!allowed){
                response.setStatus(429);
                response.getWriter().write("Too many requests");
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}

//Why RateLimitFilter ?
//1. to apply rate limiting to all endpoints
//2. to execute before controllers
//3. to be invisible to business logic

