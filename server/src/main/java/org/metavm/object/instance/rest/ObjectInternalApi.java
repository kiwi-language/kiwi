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
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpHeaderImpl;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.user.LoginService;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.PersistenceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal-api/object")
@Slf4j
public class ObjectInternalApi {

//    public static final int MAX_RETRIES = 5;

    private final ApiService apiService;

    private final LoginService loginService;

    private final boolean verify;

    public ObjectInternalApi(ApiService apiService, LoginService loginService, @Value("${metavm.api.verify}") boolean verify) {
        this.apiService = apiService;
        this.loginService = loginService;
        this.verify = verify;
    }

    @PostMapping("/save")
    public String save(@RequestBody SaveRequest request, HttpServletRequest servletRequest) {
        verify(request.appId());
        var httpRequest = createRequest(servletRequest);
        var response = new HttpResponseImpl();
        return PersistenceUtil.doWithRetries(() -> apiService.saveInstance(request.object(), httpRequest, response));
    }

    @PostMapping("/get")
    public Map<String, Object> get(@RequestBody GetRequest request) {
        verify(request.appId());
        return apiService.getInstance(request.id());
    }

    @PostMapping("/multi-get")
    public List<Map<String, Object>> multiGet(@RequestBody MultiGetRequest request) {
        verify(request.appId());
        return apiService.multiGet(request.ids(), request.excludeChildren(), request.excludeFields());
    }


    @PostMapping("/search")
    public SearchResult search(@RequestBody SearchRequest request) {
        verify(request.appId());
        try {
            var criteria = request.criteria();
            var page = request.page();
            if (page <= 0)
                page = 1;
            var pageSize = request.pageSize();
            if (pageSize <= 0)
                pageSize = 20;
            var newlyCreatedId = request.newlyCreatedId();
            return apiService.search(request.type(), criteria, page, pageSize, false, newlyCreatedId);
        }
        catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "bad search request");
        }
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteRequest request) {
        verify(request.appId());
        PersistenceUtil.doWithRetries(() -> apiService.delete(request.id()));
    }

    @PostMapping("/invoke")
    public Object invoke(@RequestBody InvokeRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        verify(request.appId());
        var httpRequest = createRequest(servletRequest);
        var response = new HttpResponseImpl();
        var args = request.arguments();
        var receiver = request.receiver();
        var r = PersistenceUtil.doWithRetries(() -> apiService.handleMethodCall(receiver, request.method(), args, false, httpRequest, response));
        saveResponse(response, servletResponse);
        return r;
    }

    private void verify(long appId) {
//        if (verify) {
//            var secret = request.getHeader(Headers.SECRET);
//            if (!loginService.verifySecret(appId, secret))
//                throw new BusinessException(ErrorCode.AUTH_FAILED);
//        }
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
