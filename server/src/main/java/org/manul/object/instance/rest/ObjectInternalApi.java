package org.manul.object.instance.rest;

import lombok.extern.slf4j.Slf4j;
import org.manul.api.entity.HttpCookie;
import org.manul.api.entity.HttpHeader;
import org.manul.api.entity.HttpRequest;
import org.manul.common.ErrorCode;
import org.manul.context.http.*;
import org.manul.http.HttpHeaderImpl;
import org.manul.http.HttpRequestImpl;
import org.manul.http.HttpResponseImpl;
import org.manul.object.instance.ApiService;
import org.manul.server.HttpMethod;
import org.manul.util.BusinessException;
import org.manul.util.ContextUtil;
import org.manul.util.PersistenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Mapping("/internal-api/object")
@Slf4j
public class ObjectInternalApi {

    private final ApiService apiService;

    public ObjectInternalApi(ApiService apiService) {
        this.apiService = apiService;
    }

    @Post("/save")
    public String save(@RequestPath String uri, @Headers Map<String, List<String>> headers, @RequestBody SaveRequest request) {
        ContextUtil.setAppId(request.appId());
        var httpRequest = createRequest(HttpMethod.POST, uri, headers);
        var response = new HttpResponseImpl();
        return PersistenceUtil.doWithRetries(() -> apiService.saveInstance(request.object(), httpRequest, response));
    }

    @Post("/get")
    public Map<String, Object> get(@RequestBody GetRequest request) {
        ContextUtil.setAppId(request.appId());
        return apiService.getInstance(request.id());
    }

    @Post("/multi-get")
    public List<Map<String, Object>> multiGet(@RequestBody MultiGetRequest request) {
        ContextUtil.setAppId(request.appId());
        return apiService.multiGet(request.ids(), request.excludeChildren(), request.excludeFields());
    }

    @Post("/search")
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

    @Post("/delete")
    public void delete(@RequestBody DeleteRequest request) {
        verify(request.appId());
        PersistenceUtil.doWithRetries(() -> apiService.delete(request.id()));
    }

    @Post("/invoke")
    public Object invoke(@RequestPath String uri, @Headers Map<String, List<String>> headers, @RequestBody InvokeRequest request) {
        verify(request.appId());
        var httpRequest = createRequest(HttpMethod.POST, uri, headers);
        var response = new HttpResponseImpl();
        var args = request.arguments();
        var receiver = request.receiver();
        return PersistenceUtil.doWithRetries(() -> apiService.handleMethodCall(receiver, request.method(), args, false, httpRequest, response));
    }

    private static void verify(long appId) {
        ContextUtil.setAppId(appId);
    }

    private static HttpRequest createRequest(HttpMethod method, String uri, Map<String, List<String>> h) {
        var headers = new ArrayList<HttpHeader>();
        h.forEach((name, values) -> headers.add(new HttpHeaderImpl(name, values.getFirst())));
        var cookies = new ArrayList<HttpCookie>();
        return new HttpRequestImpl(
                method.name(),
                uri,
                headers,
                cookies
        );
    }

}
