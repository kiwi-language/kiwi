package org.metavm.object.instance.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@RestController
public class ApiController {

    private final ApiAdapter apiAdapter;

    public static final String HEADER_REFRESH_POLICY = "X-Refresh-Policy";
    public static final String REFRESH_POLICY_NONE = "none";

    public ApiController(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    @GetMapping("/api/**")
    public Map<String, Object> handleGet(HttpServletRequest servletRequest) {
        initContextAppId(servletRequest);
        return apiAdapter.handleGet(servletRequest.getRequestURI());
    }

    @PostMapping("/api/**")
    public ResponseEntity<Object> handlePost(HttpServletRequest servletRequest,
                                             @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            var refreshPolicy = servletRequest.getHeader(HEADER_REFRESH_POLICY);
            if (!REFRESH_POLICY_NONE.equalsIgnoreCase(refreshPolicy))
                ContextUtil.setWaitForSearchSync(true);
            initContextAppId(servletRequest);
            var httpReq = buildHttpRequest(servletRequest);
            var httpResp = new HttpResponseImpl();
            var r = Result.success(apiAdapter.handlePost(
                    servletRequest.getRequestURI(),
                    Objects.requireNonNullElse(requestBody, Map.of()),
                    httpReq,
                    httpResp
            ));
            return toResponse(r, httpResp);
        } finally {
            ContextUtil.setWaitForSearchSync(false);
        }
    }

    private  <T> ResponseEntity<Object> toResponse(Result<T> result, HttpResponse response) {
        if (result.isSuccessful()) {
            var headers = getHeaders(response);
            if (result.getData() == null)
                return ResponseEntity.noContent().headers(headers).build();
            else
                return new ResponseEntity<>(result.getData(), headers, 200);
        } else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
    }

    @DeleteMapping("/api/**")
    public void handleDelete(HttpServletRequest servletRequest) {
        initContextAppId(servletRequest);
        apiAdapter.handleDelete(servletRequest.getRequestURI());
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

    private HttpHeaders getHeaders(HttpResponse httpResponse) {
        var headers = new HttpHeaders();
        for (HttpHeader header : httpResponse.getHeaders()) {
            headers.add(header.name(), header.value());
        }
        for (HttpCookie cookie : httpResponse.getCookies()) {
            headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from(cookie.name(), cookie.value()).toString());
        }
        return headers;
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
