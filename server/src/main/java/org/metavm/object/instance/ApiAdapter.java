package org.metavm.object.instance;

import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class ApiAdapter extends EntityContextFactoryAware {

    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_PAGE = "_page";
    private static final String KEY_PAGE_SIZE = "_pageSize";
    private static final String KEY_TYPE = "_type";
    private static final String KEY_NEWLY_CREATED = "_newlyCreated";


    private final ApiService apiService;

    public ApiAdapter(EntityContextFactory entityContextFactory, ApiService apiService) {
        super(entityContextFactory);
        this.apiService = apiService;
    }

    public Map<String, Object> handleGet(String uri) {
        var path = parsePath(uri);
        return (Map<String, Object>) transformResultObject(apiService.getInstance(path.suffix()));
    }

    public Object handlePost(String uri, Map<String, Object> requestBody, HttpRequest httpRequest, HttpResponse httpResponse) {
        ClassType type;
        if ((type = truGetClassType(uri)) != null)
            return apiService.saveInstance(transformRequestObject(requestBody, type), httpRequest, httpResponse);
        else {
            var path = parsePath(uri);
            if (path.suffix.equals("_search")) {
                var searchReq = buildSearchRequest(requestBody, path.classType);
                var r = apiService.search(
                        path.classType.getTypeDesc(),
                        searchReq.criteria,
                        searchReq.page,
                        searchReq.pageSize,
                        searchReq.newlyCreated
                );
                return new SearchResult(
                        Utils.map(r.items(), this::transformResultValue),
                        r.total()
                );
            } else {
                var methodName = NamingUtils.pathToName(path.suffix);
                var method = resolveMethod(path.classType, methodName, requestBody);
                var invokeReq = buildInvokeRequest(requestBody, method);
                var r = apiService.handleMethodCall(
                        invokeReq.receiver,
                        methodName,
                        invokeReq.arguments,
                        httpRequest,
                        httpResponse
                );
                return transformResultValue(r);
            }
        }
    }

    private MethodRef resolveMethod(ClassType type, String name, Map<String, Object> requestBody) {
        var method = type.findMethod(m -> m.isPublic() && !m.isStatic() && m.getName().equals(name));
        if (method == null)
            throw new BusinessException(ErrorCode.METHOD_RESOLUTION_FAILED, name, requestBody);
        return method;
    }

    public void handleDelete(String uri) {
        var path = parsePath(uri);
        apiService.delete(path.suffix);
    }

    private record InvokeRequest(
            Map<String, Object> receiver,
            Map<String, Object> arguments
    ) {}

    private InvokeRequest buildInvokeRequest(Map<String, Object> requestBody, MethodRef method) {
        Map<String, Object> receiver;
        if (requestBody.get(KEY_ID) instanceof String id)
            receiver = Map.of("id", id);
        else if (requestBody.get(KEY_NAME) instanceof String name) {
            if (requestBody.get(KEY_TYPE) instanceof String type)
                receiver = Map.of("type", type, "name", name);
            else
                receiver = Map.of("name", name);
        }
        else if (method.getDeclaringType().isBean()) {
            receiver = Map.of(
                    "name",
                    Objects.requireNonNull(method.getDeclaringType().getKlass().getAttribute(AttributeNames.BEAN_NAME))
            );
        }
        else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
        var args = new HashMap<String, Object>();
        method.getParameters().forEach(param -> {
            var arg = requestBody.get(param.getName());
            if (arg != null)
                args.put(param.getName(), transformRequestValue(arg, param.getType()));
        });
        return new InvokeRequest(receiver, args);
    }

    private Object transformResultValue(Object value) {
        return switch (value) {
            case Map map -> transformResultObject(map);
            case List list -> Utils.map(list, this::transformResultValue);
            case null, default -> value;
        };
    }

    private Object transformRequestValue(Object value, Type type) {
        return switch (value) {
            case Map map -> {
                if (type instanceof ClassType ct)
                    yield transformRequestObject(map, ct);
                else
                    throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
            }
            case List list -> {
                if (type instanceof ArrayType arrayType)
                    yield transformRequestList(list, arrayType);
                else
                    yield transformRequestList(list, Types.getArrayType(type));
            }
            case String s -> transformRequestString(s, type);
            default -> value;
        };
    }

    private Object transformRequestString(String s, Type type) {
        if (type instanceof StringType)
            return s;
        if (type instanceof ClassType) {
            if (type.isEnum())
                return Map.of("type", type.getTypeDesc(), "name", s);
            else if (type.isValueType())
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
            else
                return Map.of("id", s);
        }
        else
            return s;
    }


    private List<Object> transformRequestList(List<Object> list, ArrayType type) {
        return Utils.map(list, item -> transformRequestValue(item, type.getElementType()));
    }

    private Map<String, Object> transformRequestObject(Map<String, Object> map, ClassType type) {
        var id = map.get(KEY_ID) instanceof String s ? s : null;
        var fields = id == null ? transformRequestObjectArgs(map, type) : transformRequestObjectFields(map, type);
        var children = new HashMap<String, Object>();
        for (ClassType ik : type.getInnerClassTypes()) {
            if (map.get(ik.getName()) instanceof List<?> list) {
                var list1 = new ArrayList<Map<String, Object>>();
                for (var item : list) {
                    if (item instanceof Map map1)
                        list1.add(transformRequestObject(map1, ik));
                    else
                        throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                }
                children.put(ik.getName(), list1);
            }
        }
        var result = new HashMap<String, Object>();
        if (id != null)
            result.put("id", id);
        result.put("type", type.getTypeDesc());
        result.put("fields", fields);
        result.put("children", children);
        return result;
    }

    private Object transformResultObject(Map<String, Object> result) {
        if (result.get("name") instanceof String name)
            return name;
        var type = (String) result.get("type");
        var id = (String) result.get("id");
        var fields = (Map<String, Object>) result.get("fields");
        if (fields == null)
            return Objects.requireNonNull(id);
        var transformed = new LinkedHashMap<String, Object>();
        transformed.put(KEY_ID, id);
        transformed.put(KEY_TYPE, type);
        fields.forEach((fieldName, value) -> transformed.put(fieldName, transformResultValue(value)));
        var children = (Map<String, List<Map<String, Object>>>) result.get("children");
        if (children != null)
            children.forEach((childName, list) -> transformed.put(childName, Utils.map(list, this::transformResultObject)));
        return transformed;
    }

    private record SearchRequest(
            Map<String, Object> criteria,
            int page,
            int pageSize,
            @Nullable String newlyCreated
    ) {}

    private SearchRequest buildSearchRequest(Map<String, Object> requestBody, ClassType type) {
        var criteria = transformRequestObjectFields(requestBody, type);
        var page = requestBody.get(KEY_PAGE) instanceof Integer p ? p : 1;
        var pageSize = requestBody.get(KEY_PAGE_SIZE) instanceof Integer p ? p : 20;
        var newlyCreated = requestBody.get(KEY_NEWLY_CREATED) instanceof String s ? s : null;
        return new SearchRequest(criteria, page, pageSize, newlyCreated);
    }

    private Map<String, Object> transformRequestObjectArgs(Map<String, Object> map, ClassType type) {
        var fields = new HashMap<String, Object>();
        var constructor = type.getConstructor();
        constructor.getParameters().forEach(param -> {
            var value = map.get(param.getName());
            if (value != null)
                fields.put(param.getName(), transformRequestValue(value, param.getType()));
        });
        return fields;
    }

    private Map<String, Object> transformRequestObjectFields(Map<String, Object> map, ClassType type) {
        var fields = new HashMap<String, Object>();
        type.forEachField(field -> {
            if (field.isPublic()) {
                var value = map.get(field.getName());
                if (value != null)
                    fields.put(field.getName(), transformRequestValue(value, field.getPropertyType()));
            }
        });
        return fields;
    }

    private ClassType truGetClassType(String uri) {
        if (!uri.startsWith("/api/"))
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
        var name = NamingUtils.pathToName(uri.substring(5), true);
        try (var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(name));
            return klass != null ? klass.getType() : null;
        }
    }

    private Path parsePath(String uri) {
        var idx = uri.lastIndexOf('/');
        if (idx == -1 || idx == uri.length() - 1)
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
        var clasType = truGetClassType(uri.substring(0, idx));
        if (clasType == null)
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
        return new Path(clasType, uri.substring(idx + 1));
    }

    private record Path(ClassType classType, String suffix) {}

}
