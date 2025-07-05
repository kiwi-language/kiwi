package org.metavm.object.instance;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.common.ErrorCode;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.util.NamingUtils.firstCharsToLowerCase;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
@Slf4j
public class ApiAdapter extends EntityContextFactoryAware {

    private static final String KEY_ID = "id";
    private static final String KEY_INCLUDE_CHILDREN = "includeChildren";
    private static final String KEY_PAGE = "page";
    private static final String KEY_PAGE_SIZE = "pageSize";
    private static final String KEY_NEWLY_CHANGED_ID = "newlyChangedId";

    private final ApiService apiService;

    public ApiAdapter(EntityContextFactory entityContextFactory, ApiService apiService) {
        super(entityContextFactory);
        this.apiService = apiService;
    }

    public Map<String, Object> handleGet(String uri) {
        var path = parsePath(uri);
        return (Map<String, Object>) transformResultObject(apiService.getInstance(path.suffix()), path.classType);
    }

    @SneakyThrows
    public Object handlePost(String uri, Map<String, Object> requestBody, HttpRequest httpRequest, HttpResponse httpResponse) {
        ClassType type;
        if ((type = parseGetClassType(uri)) != null) {
            var o = transformRequestObject(requestBody, type);
            return PersistenceUtil.doWithRetries(() ->
                            apiService.saveInstance(o, httpRequest, httpResponse)
                    );
        } else {
            var path = parsePath(uri);
            if (path.suffix.equals("_search")) {
                var searchReq = buildSearchRequest(requestBody, path.classType);
                var r = apiService.search(
                        path.classType.getTypeDesc(),
                        searchReq.criteria,
                        searchReq.page,
                        searchReq.pageSize,
                        Boolean.TRUE.equals(requestBody.get(KEY_INCLUDE_CHILDREN)),
                        searchReq.newlyCreated
                );
                return new SearchResult(
                        Utils.map(r.items(), i -> transformResultValue(i, path.classType)),
                        r.total()
                );
            } else {
                var methodName = NamingUtils.pathToName(path.suffix);
                var method = resolveMethod(path.classType, methodName, requestBody);
                var invokeReq = buildInvokeRequest(requestBody, method);
                return PersistenceUtil.doWithRetries(() -> {
                    var r = apiService.handleMethodCall(
                            invokeReq.receiver,
                            methodName,
                            invokeReq.arguments,
                            httpRequest,
                            httpResponse
                    );
                    return transformResultValue(r, method.getReturnType());
                });
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
        PersistenceUtil.doWithRetries(() -> apiService.delete(path.suffix));
    }

    private record InvokeRequest(
            Map<String, Object> receiver,
            Map<String, Object> arguments
    ) {}

    private InvokeRequest buildInvokeRequest(Map<String, Object> requestBody, MethodRef method) {
        Map<String, Object> receiver;
        if (method.getDeclaringType().isBean()) {
            receiver = Map.of(
                    "name",
                    Objects.requireNonNull(method.getDeclaringType().getKlass().getAttribute(AttributeNames.BEAN_NAME))
            );
        } else {
            var idField = firstCharsToLowerCase(method.getDeclaringType().getKlass().getName()) + "Id";
            if (requestBody.get(idField) instanceof String id)
                receiver = Map.of("id", id);
            else
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
        }
        var args = new HashMap<String, Object>();
        method.getParameters().forEach(param -> {
            var arg = requestBody.get(transformFieldName(param.getName(), param.getType()));
            if (arg != null)
                args.put(param.getName(), transformRequestValue(arg, param.getType()));
        });
        return new InvokeRequest(receiver, args);
    }

    private Object transformResultValue(Object value, Type type) {
        return switch (value) {
            case Map map -> transformResultObject(map, (ClassType) type.getUnderlyingType());
            case List list -> Utils.map(list, e -> transformResultValue(e, ((ArrayType) type.getUnderlyingType()).getElementType()));
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
        var ut = type.getUnderlyingType();
        if (ut instanceof StringType)
            return s;
        if (ut instanceof ClassType) {
            if (s.isEmpty())
                return null;
            if (ut.isEnum())
                return Map.of("type", ut.getTypeDesc(), "name", s);
            else if (ut.isValueType())
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
            else
                return Map.of("id", s);
        }
        else if (ut instanceof PrimitiveType pt) {
            try {
                return switch (pt.getKind()) {
                    case BYTE -> Byte.parseByte(s);
                    case SHORT -> Short.parseShort(s);
                    case INT -> Integer.parseInt(s);
                    case LONG -> Long.parseLong(s);
                    case FLOAT -> Float.parseFloat(s);
                    case DOUBLE -> Double.parseDouble(s);
                    case BOOLEAN -> Boolean.parseBoolean(s);
                    case CHAR -> s;
                    default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                };
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
            }
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
            var childFieldName = getChildFieldName(ik.getName());
            if (map.get(childFieldName) instanceof List<?> list) {
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

    private Object transformResultObject(Map<String, Object> result, ClassType type) {
        if (result.get("name") instanceof String name)
            return name;
        var id = (String) result.get("id");
        var fields = (Map<String, Object>) result.get("fields");
        if (fields == null)
            return Objects.requireNonNull(id);
        var transformed = new LinkedHashMap<String, Object>();
        transformed.put(KEY_ID, id);
        type.forEachField(f -> {
            if (!f.isStatic() && f.isPublic()) {
                var value = fields.get(f.getName());
                var fName = transformFieldName(f.getName(), f.getPropertyType());
                if (value != null) {
                    transformed.put(fName, transformResultValue(value, f.getPropertyType()));
                    if (isEntityType(f.getPropertyType())) {
                        var cls = ((ClassType) f.getPropertyType().getUnderlyingType()).getKlass();
                        if (cls.getTitleField() != null) {
                            var nameF = f.getName() + NamingUtils.firstCharToUpperCase(cls.getTitleField().getName());
                            transformed.put(nameF, ((Map) value).get("summary"));
                        }
                    }
                }
            }
        });
        var children = (Map<String, List<Map<String, Object>>>) result.get("children");
        if (children != null) {
            for (ClassType childType : type.getInnerClassTypes()) {
                var childObjects = children.get(childType.getName());
                var childFieldName = getChildFieldName(childType.getName());
                if (!transformed.containsKey(childFieldName)) {
                    if (childObjects != null) {
                        transformed.put(
                                childFieldName,
                                Utils.map(childObjects, c -> transformResultObject(c, childType))
                        );
                    } else
                        transformed.put(childFieldName, List.of());
                }
            }
        }
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
        var newlyCreated = requestBody.get(KEY_NEWLY_CHANGED_ID) instanceof String s ? s : null;
        return new SearchRequest(criteria, page, pageSize, newlyCreated);
    }

    private Map<String, Object> transformRequestObjectArgs(Map<String, Object> map, ClassType type) {
        var fields = new HashMap<String, Object>();
        var constructor = type.getConstructor();
        constructor.getParameters().forEach(param -> {
            var fName = transformFieldName(param.getName(), param.getType());
            var value = map.get(fName);
            if (value != null)
                fields.put(param.getName(), transformRequestValue(value, param.getType()));
        });
        return fields;
    }

    private String transformFieldName(String name, Type type) {
        if (isEntityType(type))
            return name + "Id";
        else if (isEntityArrayType(type))
            return InflectUtil.singularize(name) + "Ids";
        else
            return name;
    }

    private boolean isEntityType(Type type) {
        return type.getUnderlyingType() instanceof ClassType ct && !ct.isValueType() && !ct.isEnum();
    }

    private boolean isEntityArrayType(Type type) {
        return type.getUnderlyingType() instanceof ArrayType arrayType && isEntityType(arrayType.getElementType());
    }

    private String getChildFieldName(String childTypeName) {
        return InflectUtil.pluralize(firstCharsToLowerCase(childTypeName));
    }

    private Map<String, Object> transformRequestObjectFields(Map<String, Object> map, ClassType type) {
        var fields = new HashMap<String, Object>();
        type.forEachField(field -> {
            if (field.isPublic()) {
                var value = map.get(transformFieldName(field.getName(), field.getPropertyType()));
                if (value != null)
                    fields.put(field.getName(), transformRequestValue(value, field.getPropertyType()));
            }
        });
        return fields;
    }

    private ClassType parseGetClassType(String uri) {
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
        var clasType = parseGetClassType(uri.substring(0, idx));
        if (clasType == null)
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
        return new Path(clasType, uri.substring(idx + 1));
    }

    private record Path(ClassType classType, String suffix) {}

}
