package org.metavm.user;

import org.metavm.server.HttpRequest;

import javax.annotation.Nullable;


public class Tokens {

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;


    public static @Nullable String getToken(HttpRequest request) {
        return getToken(request.getHeader("Authorization"));
    }

    public static @Nullable String getToken(String auth) {
        if (auth != null && auth.toLowerCase().startsWith("bearer "))
            return auth.substring(7);
        else
            return null;
    }

    public static void setToken(HttpRequest request, String token) {
        request.addHeader("Authorization", "Bearer " + token);
    }

    public static String getTokenCookieName(long appId) {
        return String.format("__token_%d__", appId);
    }
}
