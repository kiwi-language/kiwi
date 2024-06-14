package org.metavm.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getClientIP(HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = servletRequest.getRemoteAddr();
        }
        return ipAddress;
    }
}
