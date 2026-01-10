package com.mehak.ratelimiter.controller;

import com.mehak.ratelimiter.util.RequestContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    //return the fingerprint of client
    @GetMapping("/api/test")
    public String test(HttpServletRequest request){
        return "Fingerprint: "+ RequestContext.getFingerprint(request);
    }
}
