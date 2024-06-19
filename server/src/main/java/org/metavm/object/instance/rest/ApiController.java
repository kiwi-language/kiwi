package org.metavm.object.instance.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.user.LoginService;
import org.metavm.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    private final LoginService loginService;

    private final boolean verify;

    public ApiController(ApiService apiService, LoginService loginService, @Value("${metavm.api.verify}") boolean verify) {
        this.apiService = apiService;
        this.loginService = loginService;
        this.verify = verify;
    }

    @RequestMapping("/**")
    public Object handle(HttpServletRequest servletRequest, HttpServletResponse servletResponse, @RequestBody(required = false) Object requestBody) {
        verify(servletRequest);
        var request = createRequest(servletRequest);
        var response = new HttpResponseImpl();
        var method = servletRequest.getMethod();
        var path = servletRequest.getRequestURI().substring(5);
        var result = switch (method) {
            case "POST" -> {
                var idx = path.lastIndexOf('/');
                if (idx == -1)
                    throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
                var qualifier = NamingUtils.pathToName(path.substring(0, idx));
                var methodCode = NamingUtils.hyphenToCamel(path.substring(idx + 1));
                //noinspection unchecked
                var arguments = (List<Object>) requestBody;
                if (methodCode.equals("new"))
                    yield apiService.handleNewInstance(qualifier, arguments, request, response);
                else
                    yield apiService.handleMethodCall(qualifier, methodCode, arguments, request, response);
            }
            case "GET" -> apiService.getInstance(path);
            case "PUT" -> {
                var klassName = NamingUtils.pathToName(path);
                //noinspection unchecked
                yield apiService.saveInstance(klassName, (Map<String, Object>) requestBody, request, response);
            }
            case "DELETE" -> {
                apiService.deleteInstance(path);
                yield null;
            }
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_METHOD);
        };
        saveResponse(response, servletResponse);
        return result;
    }

    private void verify(HttpServletRequest request) {
        var appIdStr = request.getHeader(Headers.APP_ID);
        if (appIdStr == null || !ValueUtils.isIntegerStr(appIdStr))
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        var appId = Long.parseLong(appIdStr);
        if (verify) {
            var secret = request.getHeader(Headers.SECRET);
            if (!loginService.verifySecret(appId, secret))
                throw new BusinessException(ErrorCode.AUTH_FAILED);
        }
        ContextUtil.setAppId(appId);
    }

    private HttpRequest createRequest(HttpServletRequest servletRequest) {
        var headers = new ArrayList<HttpHeader>();
        var names = servletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            var name = names.nextElement();
            headers.add(new HttpHeader(name, servletRequest.getHeader(name)));
        }
        var cookies = new ArrayList<HttpCookie>();
        for (Cookie cookie : servletRequest.getCookies()) {
            cookies.add(new HttpCookie(cookie.getName(), cookie.getValue()));
        }
        return new HttpRequestImpl(
                servletRequest.getMethod(),
                servletRequest.getRequestURI(),
                headers,
                cookies
        );
    }

    private void saveResponse(HttpResponse response, HttpServletResponse servletResponse) {
        for (HttpCookie cookie : response.getCookies()) {
            var servletCookie = new Cookie(cookie.name(), cookie.value());
            servletCookie.setPath("/");
            servletResponse.addCookie(servletCookie);
        }
        for (HttpHeader header : response.getHeaders()) {
            servletResponse.addHeader(header.name(), header.value());
        }
    }

}
