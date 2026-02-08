package org.manul.object.instance;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.manul.api.entity.HttpRequest;
import org.manul.api.entity.HttpResponse;
import org.manul.application.Application;
import org.manul.beans.BeanDefinitionRegistry;
import org.manul.common.ErrorCode;
import org.manul.context.Component;
import org.manul.entity.EntityContextFactory;
import org.manul.entity.EntityContextFactoryAware;
import org.manul.flow.MethodRef;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.rest.SearchResult;
import org.manul.object.type.*;
import org.manul.util.*;

import javax.annotation.Nullable;
import java.util.*;

import static org.manul.util.NamingUtils.firstCharsToLowerCase;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
@Slf4j
public class ApiAdapter extends EntityContextFactoryAware {

    private static final String KEY_ID = "id";
    private static final String KEY_INCLUDE_CHILDREN = "includeChildren";
    private static final String KEY_PAGE = "_page";
    private static final String KEY_PAGE_SIZE = "_pageSize";
    private static final String KEY_NEWLY_CHANGED_ID = "newlyChangedId";

    private final ApiService apiService;

    public ApiAdapter(EntityContextFactory entityContextFactory, ApiService apiService) {
        super(entityContextFactory);
        this.apiService = apiService;
    }

    public Object handleGet(String path, Map<String, List<String>> params) {
        path = preprocessPath(path);
        var lastSlashIdx = path.lastIndexOf('/');
        if (lastSlashIdx != -1 && isId(path.substring(lastSlashIdx + 1))) {
            // Request path ends with an ID
            var instPath = parseInstancePath(path);
            try (var context = newContext()) {
                return transformResultObject(apiService.getInstance(instPath.id()), context);
            }
        } else {
            var type = parseClassPath(path);
            if (type == null)
                throw invalidRequestPath();
            var queryParams = new QueryParams(params);
            var searchReq = buildSearchRequest(queryParams, type);
            if (searchReq.ids != null && searchReq.criteria.isEmpty()) {
                // Pure ID query - optimize
                var objects = apiService.multiGet(searchReq.ids, false, false);
                try (var context = newContext()) {
                    var items = Utils.map(objects, m -> transformResultObject(m, context));
                    return new SearchResult(items, items.size());
                }
            }
            var r = apiService.search(
                    type.getTypeDesc(),
                    Utils.safeCall(searchReq.ids, _ids -> Utils.map(_ids, Id::parse)),
                    searchReq.criteria,
                    searchReq.page,
                    searchReq.pageSize,
                    "true".equals(queryParams.getFirst(KEY_INCLUDE_CHILDREN)),
                    searchReq.newlyCreated
            );
            try (var context = newContext()) {
                return new SearchResult(
                        Utils.map(r.items(), i -> transformResultValue(i, context)),
                        r.total()
                );
            }
        }
    }

    @SneakyThrows
    public Object handlePost(String path, Map<String, Object> requestBody, boolean retFullObj, HttpRequest httpRequest, HttpResponse httpResponse) {
        path = preprocessPath(path);
        ClassType type;
        if ((type = parseClassPath(path)) != null) {
            var o = transformRequestObject(requestBody, type, false);
            return PersistenceUtil.doWithRetries(() ->
                            apiService.saveInstance(o, httpRequest, httpResponse)
                    );
        } else {
            var methPath = parseMethodPath(path);
            var methodName = methPath.methodName;
            var method = resolveMethod(methPath.type, methodName, requestBody);
            var invokeReq = buildInvokeRequest(methPath.receiver, requestBody, method);
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

    public void handlePatch(String path, Map<String, Object> requestBody, HttpRequest httpRequest, HttpResponse httpResponse) {
        path = preprocessPath(path);
        var instPath = parseInstancePath(path);
        var o = transformRequestObject(requestBody, instPath.type, true);
        o.put(KEY_ID, instPath.id);
        PersistenceUtil.doWithRetries(() ->
                apiService.saveInstance(o, httpRequest, httpResponse)
        );
    }

    private MethodRef resolveMethod(ClassType type, String name, Map<String, Object> requestBody) {
        var method = type.findMethod(m -> m.isPublic() && !m.isStatic() && m.getName().equals(name));
        if (method == null)
            throw new BusinessException(ErrorCode.METHOD_NOT_FOUND, name, requestBody);
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

    private InvokeRequest buildInvokeRequest(Map<String, Object> receiver, Map<String, Object> requestBody, MethodRef method) {
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
                    yield transformRequestObject(map, ct, map.containsKey(KEY_ID));
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

    private Map<String, Object> transformRequestObject(Map<String, Object> map, ClassType type, boolean forUpdate) {
        var fields = forUpdate ? transformUpdateFields(map, type) : transformRequestObjectArgs(map, type);
        var children = new HashMap<String, Object>();
        for (ClassType ik : type.getInnerClassTypes()) {
            var childFieldName = getChildFieldName(ik.getName());
            if (map.get(childFieldName) instanceof List<?> list) {
                var list1 = new ArrayList<Map<String, Object>>();
                for (var item : list) {
                    if (item instanceof Map map1)
                        list1.add(transformRequestObject(map1, ik, map1.containsKey(KEY_ID)));
                    else
                        throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                }
                children.put(ik.getName(), list1);
            }
        }
        var result = new HashMap<String, Object>();
        if (map.containsKey(KEY_ID))
            result.put(KEY_ID, map.get(KEY_ID));
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
            List<String> ids,
            Map<String, Object> criteria,
            int page,
            int pageSize,
            @Nullable String newlyCreated
    ) {}

    private SearchRequest buildSearchRequest(QueryParams queryParams, ClassType type) {
        var criteria = transformSearchRequest(queryParams, type);
        var page = queryParams.getInt(KEY_PAGE, 1);
        var pageSize = queryParams.getInt(KEY_PAGE_SIZE, 20);
        var newlyCreated = queryParams.getFirst(KEY_NEWLY_CHANGED_ID);
        var ids = queryParams.get("id");
        return new SearchRequest(ids, criteria, page, pageSize, newlyCreated);
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

    private Map<String, Object> transformUpdateFields(Map<String, Object> map, ClassType type) {
        var fields = new HashMap<String, Object>();
        type.foreachField(field -> {
            if (!field.isPublic() && !field.isStatic())
                return;
            var fName = transformFieldName(field.getName(), field.getPropertyType());
            var value = map.get(fName);
            if (value != null)
                fields.put(field.getName(), transformRequestValue(value, field.getPropertyType()));
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

    private Map<String, Object> transformSearchRequest(QueryParams queryParams, ClassType type) {
        var fields = new HashMap<String, Object>();
        type.forEachField(field -> {
            if (field.isPublic()) {
                var value = queryParams.getAll(transformFieldName(field.getName(), field.getPropertyType()));
                var concreteFieldType = field.getPropertyType().getUnderlyingType();
                if (!value.isEmpty()) {
                    if (value.size() == 1) {
                        fields.put(field.getName(), transformRequestValue(value.getFirst(), field.getPropertyType()));
                    } else {
                        fields.put(field.getName(), transformRequestValue(value, Types.getArrayType(field.getPropertyType())));
                    }
                } else if (concreteFieldType.isNumber()) {
                    var minFieldName = field.getName() + ".ge";
                    var maxFieldName = field.getName() + ".le";
                    var hasMin = queryParams.contains(minFieldName);
                    var hasMax = queryParams.contains(maxFieldName);
                    if (hasMin || hasMax) {
                        var numberType = (PrimitiveType) concreteFieldType;
                        var min = hasMin ? queryParams.getNumber(minFieldName) : getMinValue(numberType);
                        var max = hasMax ? queryParams.getNumber(maxFieldName) : getMaxValue(numberType);
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
        var idx = path.indexOf('/', 1);
        if (idx == -1)
            throw invalidRequestPath();
        var appName = path.substring(1, idx);
        try (var platformCtx = newPlatformContext()) {
            var app = platformCtx.selectFirstByKey(Application.IDX_NAME, Instances.stringInstance(appName));
            if (app == null)
                throw invalidRequestPath();
            ContextUtil.setAppId(app.getTreeId());
        }
        return path.substring(idx);
    }

    private BeanPath parseBeanPath(String path) {
        if (path.contains("/"))
            throw invalidRequestPath();
        var beanName = NamingUtils.hyphenToCamel(path);
        try (var context = newContext()) {
            var bean = BeanDefinitionRegistry.getInstance(context).tryGetBean(beanName);
            if (bean != null)
                return new BeanPath(bean.getInstanceType(), beanName);
            else
                throw invalidRequestPath();
        }
    }

    private ClassType parseClassPath(String path) {
        if (!path.startsWith("/"))
            throw invalidRequestPath();
        var name = NamingUtils.pathToName(path.substring(1), true);
        try (var context = newContext()) {
            var klass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(name));
            return klass != null && getClassPath(klass).equals(path) ? klass.getType() : null;
        }
    }

    private String getClassPath(Klass klass) {
        return "/" + NamingUtils.nameToPath(klass.getQualifiedName(), true);
    }

    private BusinessException invalidRequestPath() {
        return new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
    }

    private MethodPath parseMethodPath(String path) {
        var idx = path.lastIndexOf('/');
        if (idx == -1)
            throw invalidRequestPath();
        var prefix = path.substring(0 , idx);
        var idx2 = prefix.lastIndexOf('/');
        ClassType type;
        Map<String, Object> receiver;
        if (idx2 != -1 && prefix.substring(idx2 + 1).startsWith("0")) {
            // starting with 0 indicating an ID
            var instance = parseInstancePath(prefix);
            type = instance.type;
            receiver = Map.of("id", instance.id);
        } else {
            var bean = parseBeanPath(prefix.substring(1));
            type = bean.type;
            receiver = Map.of("name", bean.name);
        }
        var methodName = NamingUtils.hyphenToCamel(path.substring(idx + 1));
        return new MethodPath(type, receiver, methodName);
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

    private boolean isId(String s) {
        return s.startsWith("0");
    }

    private record MethodPath(ClassType type, Map<String, Object> receiver, String methodName) {}

    private record InstancePath(ClassType type, String id) {}

    private record BeanPath(ClassType type, String name) {}

    private record QueryParams(Map<String, List<String>> queryParams) {

        public List<String> getAll(String name) {
            return queryParams.getOrDefault(name, List.of());
        }

        public List<String> get(String name) {
            var values = queryParams.get(name);
            return values != null && !values.isEmpty() ? values : null;
        }

        public @Nullable String getFirst(String name) {
            var values = getAll(name);
            return values.isEmpty() ? null : values.getFirst();
        }

        public int getInt(String name, int defaultValue) {
            var value = getFirst(name);
            if (value == null)
                return defaultValue;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public boolean contains(String name) {
            return !getAll(name).isEmpty();
        }

        public Number getNumber(String name) {
            var value = getFirst(name);
            if (value == null)
                throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "Missing query parameter " + name);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException ignored1) {
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException ignored2) {
                        throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, "Invalid query parameter " + name + ", expecting a number");
                    }
                }
            }
        }
    }

}
