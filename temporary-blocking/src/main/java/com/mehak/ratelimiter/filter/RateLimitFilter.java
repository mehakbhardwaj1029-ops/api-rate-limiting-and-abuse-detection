package com.mehak.ratelimiter.filter;

import com.mehak.ratelimiter.config.BlockConfig;
import com.mehak.ratelimiter.service.ClientBlockService;
import com.mehak.ratelimiter.service.RateLimitService;
import com.mehak.ratelimiter.service.ViolationService;
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
    private final ClientBlockService clientBlockService;
    private final ViolationService violationService;


    public RateLimitFilter(RateLimitService rateLimitService, ClientBlockService clientBlockService, ViolationService violationService) {
        this.rateLimitService = rateLimitService;
        this.clientBlockService = clientBlockService;
        this.violationService = violationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String fingerprint = RequestContext.getFingerprint(request);

        if(fingerprint==null) {

            filterChain.doFilter(request,response);
            return;
        }

        //check block
        if(clientBlockService.isBlocked(fingerprint)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Client temporarily blocked");
            return;
        }

        //rate limit check
              boolean allowed = rateLimitService.allowRequest(fingerprint);


            if(!allowed){

                long violations = violationService.recordViolation(fingerprint);
                //block client
                if(violations >= BlockConfig.MAX_VIOLATIONS){
                    clientBlockService.blockClient(fingerprint);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    violationService.clearViolation(fingerprint);

                }
                response.setStatus(429);
                response.getWriter().write("Too many requests");
                return;
            }
//            violationService.clearViolation(fingerprint);

        filterChain.doFilter(request,response);
    }
}

