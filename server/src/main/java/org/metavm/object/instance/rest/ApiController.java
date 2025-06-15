package org.metavm.object.instance.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.metavm.object.instance.ApiAdapter;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class ApiController {

    private final ApiAdapter apiAdapter;

    public ApiController(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    @GetMapping("/api/**")
    public Result<Map<String, Object>> handleGet(HttpServletRequest servletRequest) {
        initContextAppId(servletRequest);
        return Result.success(apiAdapter.handleGet(servletRequest.getRequestURI()));
    }

    @PostMapping("/api/**")
    public Result<Object> handlePost(HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse,
                                     @RequestBody Map<String, Object> requestBody) {
        initContextAppId(servletRequest);
        var httpReq = buildHttpRequest(servletRequest);
        var httpResp = new HttpResponseImpl();
        var r =  Result.success(apiAdapter.handlePost(servletRequest.getRequestURI(), requestBody, httpReq, httpResp));
        writeHttpResponse(httpResp, servletResponse);
        return r;
    }

    @DeleteMapping("/api/**")
    public Result<Void> handleDelete(HttpServletRequest servletRequest) {
        initContextAppId(servletRequest);
        apiAdapter.handleDelete(servletRequest.getRequestURI());
        return Result.voidSuccess();
    }

    private HttpRequest buildHttpRequest(HttpServletRequest servletRequest) {
        var headers = new ArrayList<HttpHeader>();
        var headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            var name = headerNames.nextElement();
            var value = servletRequest.getHeader(name);
            headers.add(new HttpHeaderImpl(name, value));
        }
        var cookies = new ArrayList<HttpCookie>();
        if (servletRequest.getCookies() != null) {
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

    private void writeHttpResponse(HttpResponse httpResponse, HttpServletResponse servletResponse) {
        for (HttpHeader header : httpResponse.getHeaders()) {
            servletResponse.setHeader(header.name(), header.value());
        }
        for (HttpCookie cookie : httpResponse.getCookies()) {
            servletResponse.addCookie(new Cookie(cookie.name(), cookie.value()));
        }
    }

    private void initContextAppId(HttpServletRequest servletRequest) {
        var appIdStr = servletRequest.getHeader("X-App-ID");
        if (appIdStr == null)
            throw new BusinessException(ErrorCode.MISSING_X_APP_ID);
        try {
            ContextUtil.setAppId(Long.parseLong(appIdStr));
        }
        catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_APP_ID);
        }

    }

}
