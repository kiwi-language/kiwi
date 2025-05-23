package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.dto.ClassTypeDTO;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.instance.rest.dto.ArgumentDTO;
import org.metavm.object.instance.rest.dto.InvokeRequest;
import org.metavm.object.instance.rest.dto.SearchRequest;
import org.metavm.object.instance.rest.dto.SearchTerm;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.metavm.object.instance.ApiValueConverter.buildObject;
import static org.metavm.object.instance.ApiValueConverter.buildValue;

@Slf4j
public class ApiClient {

    private final ApiService apiService;

    private final List<HttpCookie> cookies = new ArrayList<>();

    public ApiClient(ApiService apiService) {
        this.apiService = apiService;
    }

    public Object getStatic(String className, String fieldName) {
        return apiService.getStatic(className, fieldName);
    }

    public ApiObject getObject(String id) {
        return ApiObject.from(apiService.getInstance(id));
    }

    public String saveInstance(String className, Map<String, Object> arguments) {
        var obj = buildObject(arguments, className);
        var uri = "/object/" + NamingUtils.nameToPath(className);
        var req = makeRequest("PUT", uri);
        var resp = new HttpResponseImpl();
        var rs = apiService.saveInstance(obj, req, resp);
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
        var rs = apiService.handleMethodCall(new InvokeRequest(
                buildValue(receiver),
                methodName,
                buildArguments(arguments)
        ), req, resp);
        processResponse(resp);
        return rs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ArgumentDTO> buildArguments(Object arguments) {
        var args = new ArrayList<ArgumentDTO>();
        if (arguments instanceof Map argMap)
            argMap.forEach((k, v) -> args.add(new ArgumentDTO((String) k, buildValue(v))));
        else if(arguments instanceof List argList) {
            int i = 0;
            for (Object o : argList) {
                args.add(new ArgumentDTO("$arg" + i++, buildValue(o)));
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
        var rs = apiService.handleNewInstance(className, buildArguments(arguments), req, resp);
        processResponse(resp);
        return rs;
    }

    public ApiSearchResult search(String className, Map<String, Object> query, int page, int pageSize) {
        var terms = new ArrayList<SearchTerm>();
        query.forEach((k, v) -> {
            var term = switch (v) {
                case List<?> list -> SearchTerm.ofRange(k, buildValue(list.getFirst()), buildValue(list.get(1)));
                default -> SearchTerm.of(k, buildValue(v));
            };
            terms.add(term);
        });
        var request = new SearchRequest(
                new ClassTypeDTO(className),
                terms,
                page,
                pageSize
        );
        return buildApiSearchResult(apiService.search(request));
    }

    private ApiSearchResult buildApiSearchResult(SearchResult searchResult) {
        return new ApiSearchResult(
                Utils.map(searchResult.items(), ApiObject::from),
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
