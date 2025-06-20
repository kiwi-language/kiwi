package org.metavm.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.metavm.util.Constants;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class Tokens {

    public static final Pattern TOKEN_COOKIE_NAME_PATTERN = Pattern.compile("__token_(\\d+)__");

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;


    public static List<Token> getAllTokens(HttpServletRequest request) {
        List<Token> tokens = new ArrayList<>();
        for (Cookie cookie : request.getCookies()) {
            var m = TOKEN_COOKIE_NAME_PATTERN.matcher(cookie.getName());
            if(m.matches()) {
                tokens.add(new Token(Long.parseLong(m.group(1)), cookie.getValue()));
            }
        }
        return tokens;
    }

    public static @Nullable Token getPlatformToken(HttpServletRequest request) {
        return getToken(Constants.PLATFORM_APP_ID, request);
    }

    public static void removeToken(long appId, HttpServletResponse response) {
        removeCookie(response, getTokenCookieName(appId));
    }

    public static void removeCookie(HttpServletResponse response, String name) {
        var removed = new Cookie(name, "_");
        removed.setMaxAge(0);
        removed.setPath("/");
        response.addCookie(removed);
    }

    public static @Nullable Token getToken(long appId, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, Cookie> cookieMap = Utils.toMap(cookies, Cookie::getName);
        String rawToken = Utils.safeCall(cookieMap.get(getTokenCookieName(appId)), Cookie::getValue);
        return Utils.safeCall(rawToken, t -> new Token(appId, t));
    }

    public static void setPlatformToken(HttpServletRequest request, HttpServletResponse response, Token token) {
        setToken(request, response, Constants.PLATFORM_APP_ID, token);
    }

    public static void setToken(HttpServletRequest request, HttpServletResponse servletResponse, long appId, Token token) {
        setTokenCookie(request, servletResponse, getTokenCookieName(appId), token);
    }

    public static String getTokenCookieName(long appId) {
        return String.format("__token_%d__", appId);
    }

    private static void setTokenCookie(HttpServletRequest request, HttpServletResponse servletResponse, String cookieName, Token token) {
        var cookie = new Cookie(cookieName, token.token());
        cookie.setMaxAge((int) (TOKEN_TTL / 1000));
        cookie.setPath("/");
        if (request.getHeader("Origin") !=null) {
            // Support cross-origin
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        }
        servletResponse.addCookie(cookie);
    }

}
