package com.mehak.ratelimiter.util;

import jakarta.servlet.http.HttpServletRequest;

//this class lets you avoid calling (Object) request.getAttribute(fingerprint) to access fingerprint atetribute that we set in dofilter method.
public class RequestContext {

    public static String getFingerprint(HttpServletRequest request){

        Object value = request.getAttribute("FINGERPRINT");
        return value!= null ?value.toString() : null;
    }
}
