package org.manul.object.instance.rest;

import org.manul.api.entity.HttpHeader;
import org.manul.api.entity.HttpRequest;
import org.manul.api.entity.HttpResponse;
import org.manul.context.http.*;
import org.manul.http.HttpHeaderImpl;
import org.manul.http.HttpRequestImpl;
import org.manul.http.HttpResponseImpl;
import org.manul.object.instance.ApiAdapter;
import org.manul.server.HttpMethod;
import org.manul.util.ContextUtil;

import java.util.*;
import java.util.List;

@Controller
@Mapping("/")
public class ApiController {

    public static final String REFRESH_POLICY_WAIT = "wait";

    private final ApiAdapter apiAdapter;

    public ApiController(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    @Get("/**")
    public Object get(@RequestPath String path, @RequestParams Map<String, List<String>> params) {
        return apiAdapter.handleGet(path, params);
    }

    @Post("/**")
    public ResponseEntity<Object> post(@Header("X-Refresh-Policy") String refreshPolicy,
                                       @Header("X-Return-Full-Object") String returnFullObject,
                                       @RequestPath String path,
                                       @Headers Map<String, List<String>> headers,
                                       @RequestBody Map<String, Object> body) {
        try {
            if (REFRESH_POLICY_WAIT.equalsIgnoreCase(refreshPolicy))
                ContextUtil.setWaitForSearchSync(true);
            var callReturnObject = "true".equalsIgnoreCase(returnFullObject);
            var httpReq = buildHttpRequest(HttpMethod.POST.name(), path, headers);
            var httpResp = new HttpResponseImpl();
            var rs = apiAdapter.handlePost(
                    path,
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

    @Patch("/**")
    public void patch(@Header("X-Refresh-Policy") String refreshPolicy,
                      @RequestPath String path,
                      @Headers Map<String, List<String>> headers,
                      @RequestBody Map<String, Object> body) {
        if (REFRESH_POLICY_WAIT.equalsIgnoreCase(refreshPolicy))
            ContextUtil.setWaitForSearchSync(true);
        var httpReq = buildHttpRequest(HttpMethod.POST.name(), path, headers);
        var httpResp = new HttpResponseImpl();
        apiAdapter.handlePatch(path, body, httpReq, httpResp);
    }


    @Delete("/**")
    public void delete(@RequestPath String path) {
        apiAdapter.handleDelete(path);
    }

    private static HttpRequest buildHttpRequest(String method, String path, Map<String, List<String>> h) {
        var headers = new ArrayList<HttpHeader>();
        h.forEach((name, value) -> headers.add(new HttpHeaderImpl(name, value.getFirst())));
        return new HttpRequestImpl(
                method,
                path,
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

}
