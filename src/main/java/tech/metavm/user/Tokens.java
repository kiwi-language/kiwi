package tech.metavm.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.metavm.common.ErrorCode;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Pattern;


public class Tokens {

    public static final String TOKEN_COOKIE_NAME = "__token__";

    private static final Pattern RAW_TOKEN_PATTERN = Pattern.compile("(.+?)-(.+)");

    public static final long TOKEN_TTL = 7 * 24 * 60 * 60 * 1000L;

    public static @Nullable Token getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, Cookie> cookieMap = NncUtils.toMap(cookies, Cookie::getName);
        String rawToken = NncUtils.get(cookieMap.get(TOKEN_COOKIE_NAME), Cookie::getValue);
        return NncUtils.get(rawToken, Tokens::parse);
    }

    public static void setToken(HttpServletResponse servletResponse, Token token) {
        var cookie = new Cookie(Tokens.TOKEN_COOKIE_NAME, getRawToken(token));
        cookie.setMaxAge((int) (TOKEN_TTL / 1000));
        cookie.setPath("/");
        servletResponse.addCookie(cookie);
    }

    private static String getRawToken(Token token) {
        return NncUtils.encondeBase64(token.tenantId()) + "-" + token.token();
    }

    private static Token parse(String rawToken) {
        var m = RAW_TOKEN_PATTERN.matcher(rawToken);
        if (m.matches()) {
            long tenantId = NncUtils.decodeBase64(m.group(1));
            String token = m.group(2);
            return new Token(tenantId, token);
        } else
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

}
