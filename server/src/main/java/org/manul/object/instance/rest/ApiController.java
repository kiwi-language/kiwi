package org.manul.object.instance.rest;

import org.manul.api.entity.HttpHeader;
import org.manul.api.entity.HttpRequest;
import org.manul.api.entity.HttpResponse;
import org.manul.common.ErrorCode;
import org.manul.context.http.*;
import org.manul.http.HttpHeaderImpl;
import org.manul.http.HttpRequestImpl;
import org.manul.http.HttpResponseImpl;
import org.manul.object.instance.ApiAdapter;
import org.manul.server.HttpMethod;
import org.manul.util.BusinessException;
import org.manul.util.ContextUtil;

import java.util.*;

@Controller
@Mapping("/api")
public class ApiController {

    public static final String REFRESH_POLICY_NONE = "none";

    private final ApiAdapter apiAdapter;

    public ApiController(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    @Get("/**")
    public Map<String, Object> get(@Header("X-App-Id") String appId, @RequestURI String uri) {
        initContextAppId(appId);
        return apiAdapter.handleGet(uri);
    }

    @Post("/**")
    public ResponseEntity<Object> post(@Header("X-Refresh-Policy") String refreshPolicy,
                                       @Header("X-Return-Full-Object") String returnFullObject,
                                       @Header("X-App-ID") String appId,
                                       @RequestURI String uri,
                                       @Headers Map<String, List<String>> headers,
                                       @RequestBody Map<String, Object> body) {
        try {
            if (!REFRESH_POLICY_NONE.equalsIgnoreCase(refreshPolicy))
                ContextUtil.setWaitForSearchSync(true);
            var callReturnObject = "true".equalsIgnoreCase(returnFullObject);
            initContextAppId(appId);
            var httpReq = buildHttpRequest(HttpMethod.POST.name(), uri, headers);
            var httpResp = new HttpResponseImpl();
            var rs = apiAdapter.handlePost(
                    uri,
                    Objects.requireNonNullElse(body, Map.of()),
                    callReturnObject,
                    httpReq,
                    httpResp
            );
            return buildResp(rs, httpResp);
        } finally {
            ContextUtil.setWaitForSearchSync(false);
        }
    }

    @Delete("/**")
    public void delete(@Header("X-App-ID") String appId, @RequestURI String uri) {
        initContextAppId(appId);
        apiAdapter.handleDelete(uri);
    }

    private static HttpRequest buildHttpRequest(String method, String uri, Map<String, List<String>> h) {
        var headers = new ArrayList<HttpHeader>();
        h.forEach((name, value) -> headers.add(new HttpHeaderImpl(name, value.getFirst())));
        return new HttpRequestImpl(
                method,
                uri,
                headers,
                List.of()
        );
    }

    private static ResponseEntity<Object> buildResp(Object data, HttpResponse httpResponse) {
        var headers = new HashMap<String, String>();
        for (HttpHeader header : httpResponse.getHeaders()) {
            headers.put(header.name(), header.value());
        }
        return new ResponseEntity<>(data, headers);
    }

    private static void initContextAppId(String appIdStr) {
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
