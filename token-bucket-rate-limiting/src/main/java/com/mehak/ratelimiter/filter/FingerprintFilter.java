package com.mehak.ratelimiter.filter;

import com.mehak.ratelimiter.fingerprint.FingerprintGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//this class intercept every request before it reaches the controller to verify client's authentication
//Once filter is used because it is executed only once per request,
//component will register the filter implementation as a bean
@Component
public class FingerprintFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FingerprintFilter.class);
    private final FingerprintGenerator fingerprintGenerator;

    public FingerprintFilter(FingerprintGenerator fingerprintGenerator) {
        this.fingerprintGenerator = fingerprintGenerator;
    }

    //this method takes the request, does something and decided wether the client should continue or not
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String fingerprint = fingerprintGenerator.generate(request);

        request.setAttribute("FINGERPRINT",fingerprint);

        log.info(
                "[REQUEST] fingerprint={} method={} path={}",
                fingerprint,
                request.getMethod(),
                request.getRequestURI()
        );

        filterChain.doFilter(request,response);   // this means that dofilter has completed its execution , now the request can be passed to next filter
    }
}
