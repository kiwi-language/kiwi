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
import org.metavm.object.instance.rest.dto.CreateRequest;
import org.metavm.object.instance.rest.dto.InvokeRequest;
import org.metavm.object.instance.rest.dto.SearchRequest;
import org.metavm.user.LoginService;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.Headers;
import org.metavm.util.ValueUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/object")
@Slf4j
public class ApiController {

//    public static final int MAX_RETRIES = 5;

    private final ApiService apiService;

    private final LoginService loginService;

    private final boolean verify;

    public ApiController(ApiService apiService, LoginService loginService, @Value("${metavm.api.verify}") boolean verify) {
        this.apiService = apiService;
        this.loginService = loginService;
        this.verify = verify;
    }

    @PutMapping
    public Result<String> save(HttpServletRequest servletRequest, @RequestBody CreateRequest request) {
        verify(servletRequest);
        var httpRequest = createRequest(servletRequest);
        var httResponse = new HttpResponseImpl();
        return Result.success(apiService.saveInstance(request.object(), httpRequest, httResponse));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        verify(servletRequest);
        return Result.success(apiService.getInstance(id));
    }

    @PostMapping("/search")
    public Result<SearchResult> search(HttpServletRequest servletRequest, @RequestBody SearchRequest request) {
        verify(servletRequest);
        return Result.success(apiService.search(request));
    }

    @PostMapping("/invoke")
    public Result<Object> invoke(HttpServletRequest servletRequest, HttpServletResponse servletResponse, @RequestBody InvokeRequest request) {
        verify(servletRequest);
        var httpRequest = createRequest(servletRequest);
        var httpResponse = new HttpResponseImpl();
        var r = apiService.handleMethodCall(request, httpRequest, httpResponse);
        saveResponse(httpResponse, servletResponse);
        return Result.success(r);
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
