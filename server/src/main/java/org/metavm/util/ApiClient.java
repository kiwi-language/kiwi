package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiValueConverter;
import org.metavm.object.instance.ObjectService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.instance.rest.dto.ArgumentDTO;
import org.metavm.object.instance.rest.dto.InvokeRequest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.metavm.object.instance.ApiValueConverter.*;

@Slf4j
public class ApiClient {

    private final ObjectService objectService;

    private final List<HttpCookie> cookies = new ArrayList<>();

    public ApiClient(ObjectService objectService) {
        this.objectService = objectService;
    }

    public Object getStatic(String className, String fieldName) {
        return ApiValueConverter.toRaw(objectService.getStatic(className, fieldName));
    }

    public ApiObject getObject(String id) {
        return ApiObject.from(ApiValueConverter.toMap(objectService.getInstance(id)));
    }

    public String saveInstance(String className, Map<String, Object> arguments) {
        var obj = buildObject(arguments, className);
        var uri = "/object/" + NamingUtils.nameToPath(className);
        var req = makeRequest("PUT", uri);
        var resp = new HttpResponseImpl();
        var rs = objectService.saveInstance(obj, req, resp);
        processResponse(resp);
        return rs;
    }

    public @Nullable Object callMethod(Object qualifier, String methodName, List<Object> arguments) {
        return callMethod0(qualifier, methodName, arguments);
    }

    public @Nullable Object callMethod(Object receiver, String methodName, Map<String, Object> arguments) {
        return callMethod0(receiver, methodName, arguments);
    }

    private @Nullable Object callMethod0(Object receiver, String methodName, Object arguments) {
        var uri = "/object/invoke";
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = objectService.handleMethodCall(new InvokeRequest(
                buildValue(receiver),
                methodName,
                buildArguments(arguments)
        ), req, resp);
        processResponse(resp);
        return ApiValueConverter.toRaw(rs);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ArgumentDTO> buildArguments(Object arguments) {
        var args = new ArrayList<ArgumentDTO>();
        if (arguments instanceof Map argMap)
            argMap.forEach((k, v) -> args.add(new ArgumentDTO((String) k, buildValue(v))));
        else if(arguments instanceof List argList) {
            int i = 0;
            for (Object o : argList) {
                args.add(new ArgumentDTO(ObjectService.ARG_PREFIX + i++, buildValue(o)));
            }
        }
        else
            throw new InternalException("Invalid arguments: " + arguments);
        return args;
    }

    public String newInstance(String className, List<Object> arguments) {
        var uri = "/object/class/" + NamingUtils.nameToPath(className) + "/new";
        var req = makeRequest("POST", uri);
        var resp = new HttpResponseImpl();
        var rs = objectService.handleNewInstance(className, buildArguments(arguments), req, resp);
        processResponse(resp);
        return rs;
    }

    public ApiSearchResult search(String className, Map<String, Object> criteria, int page, int pageSize) {
        return buildApiSearchResult(objectService.search(buildSearchRequest(className, criteria, page, pageSize)));
    }

    private ApiSearchResult buildApiSearchResult(SearchResult searchResult) {
        return new ApiSearchResult(
                Utils.map(searchResult.items(), item -> ApiObject.from(ApiValueConverter.toMap(item))),
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

}
