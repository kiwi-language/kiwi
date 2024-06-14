package tech.metavm.util;

import tech.metavm.entity.HttpCookie;
import tech.metavm.entity.HttpRequest;
import tech.metavm.entity.HttpResponse;
import tech.metavm.http.HttpRequestImpl;
import tech.metavm.http.HttpResponseImpl;
import tech.metavm.object.instance.ApiService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final ApiService apiService;

    private final List<HttpCookie> cookies = new ArrayList<>();

    public ApiClient(ApiService apiService) {
        this.apiService = apiService;
    }

    public String saveInstance(String className, Map<String, Object> map) {
        var uri = "/api/" + className.replace('.', '/');
        var req = makeRequest("PUT", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.saveInstance(className, map, req, resp);
        processResponse(resp);
        return rs;
    }

    public @Nullable Object callInstanceMethod(String id, String methodName, List<Object> arguments) {
        var uri = "/api/" + id + "/" + methodName;
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.handleInstanceMethodCall(id, methodName, arguments, req, resp);
        processResponse(resp);
        return rs;
    }

    public @Nullable Object callStaticMethod(String className, String methodName, List<Object> arguments) {
        var uri = "/api/" + className.replace('.', '/') + "/" + methodName;
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs =  apiService.handleStaticMethodCall(className, methodName, arguments, req, resp);
        processResponse(resp);
        return rs;
    }

    public @Nullable String newInstance(String className, List<Object> arguments) {
        var uri = "/api/" + className.replace('.', '/') + "/new";
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.handleNewInstance(className, arguments, req, resp);
        processResponse(resp);
        return rs;
    }

    private HttpRequest makeRequest(String method, String uri) {
        return new HttpRequestImpl(
                method,
                uri,
                List.of(),
                Collections.unmodifiableList(cookies)
        );
    }

    private void processResponse(HttpResponse response) {
        response.getCookies().forEach(this::addCookie);
    }

    private void addCookie(HttpCookie cookie) {
        cookies.removeIf(c -> cookie.name().equals(c.name()));
        cookies.add(cookie);
    }

}
