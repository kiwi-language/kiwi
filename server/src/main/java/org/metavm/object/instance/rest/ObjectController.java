package org.metavm.object.instance.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.common.Result;
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpHeaderImpl;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.user.LoginService;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.Headers;
import org.metavm.util.ValueUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/object")
@Slf4j
public class ObjectController {

//    public static final int MAX_RETRIES = 5;

    private final ApiService apiService;

    private final LoginService loginService;

    private final boolean verify;

    public ObjectController(ApiService apiService, LoginService loginService, @Value("${metavm.api.verify}") boolean verify) {
        this.apiService = apiService;
        this.loginService = loginService;
        this.verify = verify;
    }

    @PostMapping
    public Result<String> save(HttpServletRequest servletRequest, @RequestBody Map<String, Object> requestBody) {
        verify(servletRequest);
        var request = createRequest(servletRequest);
        var response = new HttpResponseImpl();
        if(requestBody.getOrDefault("object", Map.of()) instanceof Map<?,?> object) {
            //noinspection unchecked
            return Result.success(apiService.saveInstance((Map<String, Object>) object, request, response));
        }
        else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "missing 'object' field");
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        verify(servletRequest);
        return Result.success(apiService.getInstance(id));
    }

    @PostMapping("/search")
    public Result<SearchResult> search(HttpServletRequest servletRequest, @RequestBody Map<String, Object> requestBody) {
        verify(servletRequest);
        if (!(requestBody.get("type") instanceof String type))
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "missing 'type' field");
        try {
            //noinspection unchecked
            var criteria = (Map<String, Object>) requestBody.getOrDefault("criteria", Map.of());
            var page = (int) requestBody.getOrDefault("page", 1);
            var pageSize = (int) requestBody.getOrDefault("pageSize", 20);
            var newlyCreatedId = (String) requestBody.get("newlyCreatedId");
            return Result.success(apiService.search(type, criteria, page, pageSize, newlyCreatedId));
        }
        catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "bad search request");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        verify(servletRequest);
        apiService.delete(id);
        return Result.voidSuccess();
    }

    @PostMapping("/invoke")
    public Result<Object> invoke(HttpServletRequest servletRequest, HttpServletResponse servletResponse, @RequestBody Map<String, Object> requestBody) {
        verify(servletRequest);
        var request = createRequest(servletRequest);
        var response = new HttpResponseImpl();
        var args = requestBody.getOrDefault("arguments", Map.of());
        var receiver = requestBody.get("receiver");
        if (receiver == null)
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "missing 'receiver' field");
        if (!(requestBody.get("method") instanceof String methodName))
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "missing or incorrect method");
        if (args instanceof List<?> || args instanceof Map<?,?>) {
            var r = apiService.handleMethodCall(receiver, methodName, args, request, response);
            saveResponse(response, servletResponse);
            return Result.success(r);
        } else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "missing or incorrect arguments");
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
            headers.add(new HttpHeaderImpl(name, servletRequest.getHeader(name)));
        }
        var cookies = new ArrayList<HttpCookie>();
        if(servletRequest.getCookies() != null) {
            for (Cookie cookie : servletRequest.getCookies()) {
                cookies.add(new HttpCookieImpl(cookie.getName(), cookie.getValue()));
            }
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
