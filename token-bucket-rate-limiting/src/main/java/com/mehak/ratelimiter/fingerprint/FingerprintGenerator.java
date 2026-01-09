package com.mehak.ratelimiter.fingerprint;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

//HttpServletRequest is a raw http request object created by tomcat and contains - headers, method, path, remote address, body stream
@Component
public class FingerprintGenerator {

    public String generate(HttpServletRequest request){

        String ip = extractClientIp(request);
        String apiKey = extractApiKey(request);
        String method = request.getMethod();
        String path = request.getRequestURI();

        String rawFingerPrint = ip + "|" + apiKey + "|" + method + "|" +path;

        return "fpt_" + sha256(rawFingerPrint);
    }

    private String extractClientIp(HttpServletRequest request){
        // in real deployments -> request goes from client -> load balancer -> app , so when we try to getRemoteAddr() ip of load balancer is returned , so to get ip of client proxies add X-Forwarded-For: real-client-ip , since there can be multiple proxies but the 1st ip = original client
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded!=null && !forwarded.isBlank()){

            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractApiKey(HttpServletRequest request){
        // extracting api-key from headers
        String apiKey = request.getHeader("X-API-KEY");
        return apiKey != null ?apiKey:"anonymous";
    }
    //sha256 is used for hashing to make sure that original data is unrecoverable
    private String sha256(String input){

        try{

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));  //StandardCharsets.UTF_8 avoids platform-dependent encoding

            StringBuilder hex = new StringBuilder();
            for(byte b: hash){
                hex.append(String.format("%02x",b));
            }
            return hex.toString();

        }catch (Exception e){
            throw new RuntimeException("Failed to generate fingerprint");
        }
    }

}
