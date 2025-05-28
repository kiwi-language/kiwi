package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.SearchResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ApiClient {

    private final ApiService apiService;

    private final List<HttpCookie> cookies = new ArrayList<>();

    public ApiClient(ApiService apiService) {
        this.apiService = apiService;
    }

    public Object getStatic(String className, String fieldName) {
        return ApiObject.convertValue(apiService.getStatic(className, fieldName));
    }

    public ApiObject getObject(Id id) {
        return ApiObject.from(apiService.getInstance(id.toString()));
    }

    public void delete(Id id) {
        apiService.delete(id.toString());
    }

    public Id saveInstance(String className, Map<String, Object> arguments) {
        return saveInstance(className, arguments, Map.of());
    }

    public Id saveInstance(String className, Map<String, Object> arguments, Map<String, List<Map<String, Object>>> children) {
        var map = new HashMap<String, Object>();
        var fields = new HashMap<String, Object>();
        map.put("type", className);
        map.put("fields", fields);
        arguments.forEach((name, arg) -> fields.put(name, transformArgs(arg)));
        var children1 = new HashMap<String, List<Map<String, Object>>>();
        map.put("children", children1);
        children.forEach((name, c) -> children1.put(name, transformArgs(c)));
        var uri = "/object/" + NamingUtils.nameToPath(className);
        var req = makeRequest("PUT", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.saveInstance(map,req, resp);
        processResponse(resp);
        return Id.parse(rs);
    }

    public @Nullable Object callMethod(Object qualifier, String methodName, List<Object> arguments) {
        return callMethod0(qualifier, methodName, transformArgs(arguments));
    }

    public @Nullable Object callMethod(Object receiver, String methodName, Map<String, Object> arguments) {
        var transformedArgs = new HashMap<String, Object>();
        arguments.forEach((name, value) -> transformedArgs.put(name, transformArgs(value)));
        return callMethod0(receiver, methodName, transformedArgs);
    }

    private @Nullable Object callMethod0(Object receiver, String methodName, Object arguments) {
        var uri = "/object/invoke";
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.handleMethodCall(transformArgs(receiver), methodName, arguments, req, resp);
        processResponse(resp);
        return ApiObject.convertValue(rs);
    }

    public Id newInstance(String className, List<Object> arguments) {
        var uri = "/object/class/" + NamingUtils.nameToPath(className) + "/new";
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.handleNewInstance(className, transformArgs(arguments), req, resp);
        processResponse(resp);
        return Id.parse(rs);
    }

    public ApiSearchResult search(String className, Map<String, Object> query, int page, int pageSize) {
        var criteria = new HashMap<String, Object>();
        query.forEach((name, value) -> criteria.put(name, transformArgs(value)));
        return buildApiSearchResult(apiService.search(className, criteria, page, pageSize));
    }

    private ApiSearchResult buildApiSearchResult(SearchResult searchResult) {
        return new ApiSearchResult(
                Utils.map(searchResult.items(), e -> (ApiObject) ApiObject.convertValue(e)),
                searchResult.total()
        );
    }

    private HttpRequest makeRequest(String method, String uri) {
        return new HttpRequestImpl(
                method,
                uri,
                List.of(),
                cookies
        );
    }

    private void processResponse(HttpResponse response) {
        response.getCookies().forEach(this::addCookie);
    }

    private void addCookie(HttpCookie cookie) {
        cookies.removeIf(c -> cookie.name().equals(c.name()));
        cookies.add(cookie);
    }

    private <E> List<E> transformArgs(List<E> list) {
        //noinspection unchecked
        return Utils.map(list, e -> (E) transformArgs(e));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object transformArgs(Object object) {
        return switch (object) {
            case List<?> list -> transformArgs(list);
            case Map map -> transformArgs(map);
            case Id id -> Map.of("id", id.toString());
            case ApiNamedObject ec -> ec.toMap();
            case null, default -> object;
        };
    }

    private Map<String, Object> transformArgs(Map<String, Object> map) {
        var map1 = new HashMap<String, Object>();
        var fields = new HashMap<String, Object>();
        map1.put("fields", fields);
        map.forEach((k, v) -> {
            if (k.equals(ApiService.KEY_CLASS))
                map1.put("type", v);
            else
                fields.put(k, transformArgs(v));
        });
        return map1;
    }

}
