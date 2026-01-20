package org.manul.object.instance;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.manul.api.entity.HttpRequest;
import org.manul.api.entity.HttpResponse;
import org.manul.application.Application;
import org.manul.common.ErrorCode;
import org.manul.entity.AttributeNames;
import org.manul.entity.EntityContextFactory;
import org.manul.entity.EntityContextFactoryAware;
import org.manul.flow.MethodRef;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.rest.SearchResult;
import org.manul.object.type.*;
import org.manul.util.*;
import org.manul.context.Component;

import javax.annotation.Nullable;
import java.util.*;

import static org.manul.util.NamingUtils.firstCharsToLowerCase;

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

    public Map<String, Object> handleGet(String path) {
        path = preprocessPath(path);
        var instPath = parseInstancePath(path);
        try (var context = newContext()) {
            return (Map<String, Object>) transformResultObject(apiService.getInstance(instPath.id()), context);
        }
    }

    @SneakyThrows
    public Object handlePost(String path, Map<String, Object> requestBody, boolean retFullObj, HttpRequest httpRequest, HttpResponse httpResponse) {
        path = preprocessPath(path);
        ClassType type;
        if ((type = parseClassPath(path)) != null) {
            var o = transformRequestObject(requestBody, type);
            return PersistenceUtil.doWithRetries(() ->
                            apiService.saveInstance(o, httpRequest, httpResponse)
                    );
        } else {
            var instPath = parseInstancePath(path);
            if (instPath.id.equals("_search")) {
                var searchReq = buildSearchRequest(requestBody, instPath.classType);
                var r = apiService.search(
                        instPath.classType.getTypeDesc(),
                        searchReq.criteria,
                        searchReq.page,
                        searchReq.pageSize,
                        Boolean.TRUE.equals(requestBody.get(KEY_INCLUDE_CHILDREN)),
                        searchReq.newlyCreated
                );
                try (var context = newContext()) {
                    return new SearchResult(
                            Utils.map(r.items(), i -> transformResultValue(i, context)),
                            r.total()
                    );
                }
            } else if (instPath.id.equals("_multi-get")) {
                if (requestBody.get("ids") instanceof List<?> ids) {
                    List<Map<String, Object>> objects = apiService.multiGet((List) ids, false, false);
                    try (var context = newContext()) {
                        return Utils.map(objects, m -> transformResultObject(m, context));
                    }
                } else
                    throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
            } else {
                var methodName = NamingUtils.pathToName(instPath.id);
                var method = resolveMethod(instPath.classType, methodName, requestBody);
                var invokeReq = buildInvokeRequest(requestBody, method);
                return PersistenceUtil.doWithRetries(() -> {
                    var r = apiService.handleMethodCall(
                            invokeReq.receiver,
                            methodName,
                            invokeReq.arguments,
                            retFullObj,
                            httpRequest,
                            httpResponse
                    );
                    try (var context = newContext()) {
                        return transformResultValue(r, context);
                    }
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

    public void handleDelete(String path) {
        path = preprocessPath(path);
        var instPath = parseInstancePath(path);
        PersistenceUtil.doWithRetries(() -> apiService.delete(instPath.id));
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
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "Missing receiver ID in request body");
        }
        var args = new HashMap<String, Object>();
        method.getParameters().forEach(param -> {
            var arg = requestBody.get(transformFieldName(param.getName(), param.getType()));
            if (arg != null)
                args.put(param.getName(), transformRequestValue(arg, param.getType()));
        });
        return new InvokeRequest(receiver, args);
    }

    private Object transformResultValue(Object value, IInstanceContext context) {
        return switch (value) {
            case Map map -> transformResultObject(map, context);
            case List list -> Utils.map(list, e -> transformResultValue(e, context));
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
        var fields = id == null ? transformRequestObjectArgs(map, type) : transformRequestObjectFields(map, type, false);
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

    private Object transformResultObject(Map<String, Object> result, IInstanceContext context) {
        if (result.get("name") instanceof String name)
            return name;
        var id = (String) result.get("id");
        var type = getKlass((String) result.get("type"), context);
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
                    transformed.put(fName, transformResultValue(value, context));
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
                                Utils.map(childObjects, c -> transformResultObject(c, context))
                        );
                    } else
                        transformed.put(childFieldName, List.of());
                }
            }
        }
        return transformed;
    }

    private ClassType getKlass(String classCode, IInstanceContext context) {
        ParserTypeDefProvider typeDefProvider = name -> context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
                Instances.stringInstance(name));
        var type = (ClassType) new TypeParserImpl(typeDefProvider).parseType(classCode);
        if (type == null)
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, classCode);
        return type;
    }

    private record SearchRequest(
            Map<String, Object> criteria,
            int page,
            int pageSize,
            @Nullable String newlyCreated
    ) {}

    private SearchRequest buildSearchRequest(Map<String, Object> requestBody, ClassType type) {
        var criteria = transformRequestObjectFields(requestBody, type, true);
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

    private Map<String, Object> transformRequestObjectFields(Map<String, Object> map, ClassType type, boolean forSearch) {
        var fields = new HashMap<String, Object>();
        type.forEachField(field -> {
            if (field.isPublic()) {
                var value = map.get(transformFieldName(field.getName(), field.getPropertyType()));
                var concreteFieldType = field.getPropertyType().getUnderlyingType();
                if (value != null) {
                    var t = concreteFieldType instanceof ClassType && value instanceof List ?
                            Types.getArrayType(field.getPropertyType()) : field.getPropertyType();
                    fields.put(field.getName(), transformRequestValue(value, t));
                } else if (forSearch && concreteFieldType.isNumber()) {
                    var minFieldName = "min" + NamingUtils.firstCharToUpperCase(field.getName());
                    var maxFieldName = "max" + NamingUtils.firstCharToUpperCase(field.getName());
                    if (map.containsKey(minFieldName) || map.containsKey(maxFieldName)) {
                        var min = map.get(minFieldName);
                        if (min == null)
                            min = getMinValue((PrimitiveType) concreteFieldType);
                        else if (!(min instanceof Number))
                            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                        var max = map.get(maxFieldName);
                        if (max == null)
                            max = getMaxValue((PrimitiveType) concreteFieldType);
                        else if (!(max instanceof Number))
                            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                        fields.put(field.getName(), List.of(min, max));
                    }
                }
            }
        });
        return fields;
    }

    private Object getMinValue(PrimitiveType type) {
        if (type == PrimitiveType.byteType)
            return Byte.MIN_VALUE;
        else if (type == PrimitiveType.shortType)
            return Short.MIN_VALUE;
        else if (type == PrimitiveType.intType)
            return Integer.MIN_VALUE;
        else if (type == PrimitiveType.longType)
            return Long.MIN_VALUE;
        else if (type == PrimitiveType.floatType)
            return Float.MIN_VALUE;
        else if (type == PrimitiveType.doubleType)
            return Double.MIN_VALUE;
        else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
    }

    private Object getMaxValue(PrimitiveType type) {
        if (type == PrimitiveType.byteType)
            return Byte.MAX_VALUE;
        else if (type == PrimitiveType.shortType)
            return Short.MAX_VALUE;
        else if (type == PrimitiveType.intType)
            return Integer.MAX_VALUE;
        else if (type == PrimitiveType.longType)
            return Long.MAX_VALUE;
        else if (type == PrimitiveType.floatType)
            return Float.MAX_VALUE;
        else if (type == PrimitiveType.doubleType)
            return Double.MAX_VALUE;
        else
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
    }

    private String preprocessPath(String path) {
        if (!path.startsWith("/api/"))
            throw invalidRequestPath();
        var idx = path.indexOf('/', 5);
        if (idx == -1)
            throw invalidRequestPath();
        var appName = path.substring(5, idx);
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.selectFirstByKey(Application.IDX_NAME, Instances.stringInstance(appName));
            if (app == null)
                throw invalidRequestPath();
            ContextUtil.setAppId(app.getTreeId());
        }
        return path.substring(idx);
    }

    private ClassType parseClassPath(String path) {
        if (!path.startsWith("/"))
            throw invalidRequestPath();
        var name = NamingUtils.pathToName(path.substring(1), true);
        try (var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(name));
            return klass != null ? klass.getType() : null;
        }
    }

    private BusinessException invalidRequestPath() {
        return new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
    }

    private InstancePath parseInstancePath(String path) {
        var idx = path.lastIndexOf('/');
        if (idx == -1 || idx == path.length() - 1)
            throw invalidRequestPath();
        var clasType = parseClassPath(path.substring(0, idx));
        if (clasType == null)
            throw invalidRequestPath();
        return new InstancePath(clasType, path.substring(idx + 1));
    }

    private record InstancePath(ClassType classType, String id) {}

}
