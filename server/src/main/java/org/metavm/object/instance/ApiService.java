package org.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.ListNative;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.*;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

@Service
public class ApiService extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    public static final String KEY_ID = "$id";
    public static final String KEY_CLASS = "$class";

    private final MetaContextCache metaContextCache;
    private JdbcTemplate jdbcTemplate;

    public ApiService(EntityContextFactory entityContextFactory, MetaContextCache metaContextCache) {
        super(entityContextFactory);
        this.metaContextCache = metaContextCache;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String handleNewInstance(String classCode, List<Object> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, null, rawArguments, false, true, context);
            var self = ClassInstanceBuilder.newBuilder(klass).build();
            context.getInstanceContext().bind(self);
            var result = execute(r.method, self, r.arguments, request, response, context);
            context.finish();
            return (String) formatInstance(result, false);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Object handleMethodCall(String qualifier, String methodCode, List<Object> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            Value result;
            if (qualifier.startsWith("0")) {
                var self = (ClassInstance) context.getInstanceContext().get(Id.parse(qualifier));
                result = executeInstanceMethod(self, methodCode, rawArguments, request, response, context);
            } else {
                var registry = BeanDefinitionRegistry.getInstance(context);
                var bean = registry.tryGetBean(qualifier);
                if (bean != null)
                    result = executeInstanceMethod(bean, methodCode, rawArguments, request, response, context);
                else {
                    var klassName = qualifier.contains(".") ? qualifier : NamingUtils.firstCharToUpperCase(qualifier);
                    var klass = getKlass(klassName, context);
                    var r = resolveMethod(klass, methodCode, rawArguments, true, false, context);
                    result = execute(r.method, null, r.arguments, request, response, context);
                }
            }
            context.finish();
            return formatInstance(result, false);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Object handleBeanMethodCall(String beanName, String methodCode, List<Object> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var registry = BeanDefinitionRegistry.getInstance(context);
            var bean = registry.getBean(beanName);
            var result = executeInstanceMethod(bean, methodCode, rawArguments, request, response, context);
            context.finish();
            return formatInstance(result, false);
        }
    }

    private Value executeInstanceMethod(ClassInstance self,
                                        String methodCode,
                                        List<Object> rawArguments,
                                        HttpRequest request,
                                        HttpResponse response,
                                        IEntityContext context) {
        var r = resolveMethod(self.getType(), methodCode, rawArguments, false, false, context);
        return execute(r.method, self, r.arguments, request, response, context);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Object handleStaticMethodCall(String classCode, String methodCode, List<Object> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, methodCode, rawArguments, true, false, context);
            var inst = execute(r.method, null, r.arguments, request, response, context);
            context.finish();
            return formatInstance(inst, false);
        }
    }

    private Value execute(MethodRef method,
                          @Nullable ClassInstance self,
                          List<Value> arguments,
                          HttpRequest request,
                          HttpResponse response,
                          IEntityContext context) {
        return doIntercepted(
                () -> handleExecutionResult(Flows.execute(method, self, arguments, context)),
                request,
                response,
                context
        );
    }

    private Value doIntercepted(Supplier<Value> action, HttpRequest request, HttpResponse response, IEntityContext context) {
        var registry = BeanDefinitionRegistry.getInstance(context);
        var beforeMethod = StdMethod.interceptorBefore.get();
        var afterMethod = StdMethod.interceptorAfter.get();
        var interceptors = registry.getInterceptors();
        var reqInst = context.getInstance(request);
        var respInst = context.getInstance(response);
        for (ClassInstance interceptor : interceptors) {
            Flows.invokeVirtual(beforeMethod.getRef(), interceptor, List.of(reqInst.getReference(), respInst.getReference()), context);
        }
        var result = action.get();
        for (ClassInstance interceptor : interceptors) {
            result = Objects.requireNonNull(Flows.invokeVirtual(afterMethod.getRef(), interceptor, List.of(reqInst.getReference(), respInst.getReference(), result), context));
        }
        return result;
    }

    private void ensureSuccessful(FlowExecResult result) {
        if (result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
    }

    private ClassType getKlass(String classCode, IEntityContext context) {
        ParserTypeDefProvider typeDefProvider = name -> context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, name);
        var type = (ClassType) new TypeParserImpl(typeDefProvider).parseType(classCode);
        if (type == null)
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, classCode);
        return type;
    }

    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Object getInstance(String id) {
        try (var context = newContext()) {
            return formatInstance(context.getInstanceContext().get(Id.parse(id)).getReference(), true);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Object getStatic(String className, String fieldName) {
        try(var context = newContext()) {
            var klass = getKlass(className, context);
            var sft = StaticFieldTable.getInstance(klass, context);
            return formatInstance(sft.getByName(fieldName), false);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteInstance(String id) {
        try (var context = newContext()) {
            var instanceContext = context.getInstanceContext();
            instanceContext.remove(instanceContext.get(Id.parse(id)));
            context.finish();
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String saveInstance(String classCode, Map<String, Object> object, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
//            logTxId();
            ClassType klass;
            if(object.containsKey(KEY_ID)) {
                var inst0 = (ClassInstance) context.getInstanceContext().get(Id.parse((String) object.get(KEY_ID)));
                klass = inst0.getType();
            }
            else
                klass = getKlass(classCode, context);
            var inst = doIntercepted(() -> {
                var r = tryResolveValue(object, klass, true, null, context);
                if (r.successful()) {
                    var i = (Reference) r.resolved();
                    var instanceContext = context.getInstanceContext();
                    if (!instanceContext.containsInstance(i.resolve()))
                        instanceContext.bind(i.resolve());
                    return i;
                } else
                    return null;
            }, request, response, context);
            if (inst != null) {
                context.finish();
                return inst.getStringId();
            } else
                throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, object);
        }
    }

    private Value handleExecutionResult(FlowExecResult result) {
        if (result.exception() != null) {
            var throwableNative = new ThrowableNative(result.exception());
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, throwableNative.getMessage().getTitle());
        } else
            return Objects.requireNonNullElseGet(result.ret(), Instances::nullInstance);
    }

    private Object formatInstance(@Nullable Value instance, boolean asValue) {
        return switch (instance) {
            case null -> null;
            case PrimitiveValue primitiveValue -> primitiveValue.getValue();
            case Reference reference -> {
                if(asValue || reference.isValueReference()) {
                    var resolved = reference.resolve();
                    if(resolved instanceof ClassInstance clsInst) {
                        if(clsInst.isList())
                            yield formatList(clsInst);
                        else
                            yield formatValueObject(clsInst);
                    }
                    else if(resolved instanceof ArrayInstance array)
                        yield formatArray(array);
                    else
                        throw new IllegalStateException("Unrecognized DurableInstance: " + resolved);
                }
                else
                    yield reference.getStringId();
            }
            default -> throw new BusinessException(ErrorCode.FAILED_TO_FORMAT_VALUE, instance);
        };
    }

    private Map<String, Object> formatValueObject(ClassInstance instance) {
        var map = new HashMap<String, Object>();
        var id = instance.getStringId();
        if (id != null)
            map.put(KEY_ID, id);
        map.put(KEY_CLASS, instance.getType().getKlass().getQualifiedName());
        instance.forEachField((field, value) -> map.put(field.getName(), formatInstance(value, field.isChild())));
        return map;
    }

    private List<Object> formatArray(ArrayInstance arrayInstance) {
        var list = new ArrayList<>();
        var isChildArray = arrayInstance.isChildArray();
        arrayInstance.forEach(e -> list.add(formatInstance(e, isChildArray)));
        return list;
    }

    private List<Object> formatList(ClassInstance instance) {
        var list = new ArrayList<>();
        var isChildList = instance.isChildList();
        var listNative = new ListNative(instance);
        listNative.toArray().forEach(e -> list.add(formatInstance(e, isChildList)));
        return list;
    }

    private ResolutionResult resolveMethod(@NotNull ClassType klass, String methodCode, List<Object> rawArguments, boolean _static, boolean constructor, IEntityContext context) {
        var methodRef = methodCode != null ?
                TypeParser.parseSimpleMethodRef(methodCode, name -> getKlass(name, context).getKlass()) : null;
        var queue = new LinkedList<ClassType>();
        klass.foreachSuperClass(queue::offer);
        do {
            var k = Objects.requireNonNull(queue.poll());
            for (MethodRef method : k.getMethods()) {
                if (method.isPublic() && !method.isAbstract() && (methodRef == null || methodRef.name().equals(method.getName())) && method.getParameterCount() == rawArguments.size()
                        && _static == method.isStatic() && constructor == method.isConstructor()) {
                    method = methodRef != null ? method.getParameterized(methodRef.typeArguments()) : method;
                    var resolvedArgs = tryResolveArguments(method, rawArguments, context);
                    if (resolvedArgs != null)
                        return new ResolutionResult(method, resolvedArgs);
                }
            }
            k.getInterfaces().forEach(queue::offer);
        } while (!queue.isEmpty());
        throw new BusinessException(ErrorCode.METHOD_RESOLUTION_FAILED, klass.getQualifiedName() + "." + methodCode,
                rawArguments);
    }

    private List<Value> tryResolveArguments(MethodRef method, List<Object> rawArguments, IEntityContext context) {
        var resolvedArgs = new ArrayList<Value>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            var r = tryResolveValue(rawArguments.get(i), method.getParameterType(i), false, null, context);
            if (!r.successful())
                return null;
            resolvedArgs.add(r.resolved());
        }
        return resolvedArgs;
    }

    private Value resolveValue(Object rawValue, Type type, @SuppressWarnings("SameParameterValue") boolean asValue, @Nullable Value currentValue, IEntityContext context) {
        var r = tryResolveValue(rawValue, type, asValue, currentValue, context);
        if (r.successful())
            return r.resolved();
        else
            throw new InternalException("Failed to resolve value " + rawValue + " for type " + type.getTypeDesc());
    }

    private ValueResolutionResult tryResolveValue(Object rawValue, Type type, boolean asValue, @Nullable Value currentValue, IEntityContext context) {
        return switch (type) {
            case PrimitiveType primitiveType -> tryResolvePrimitive(rawValue, primitiveType);
            case ClassType classType -> switch (rawValue) {
                case String s -> asValue ? ValueResolutionResult.failed : tryResolveReference(s, classType, context);
                case List<?> list -> tryResolveList(list, classType, currentValue, context);
                case Map<?, ?> map -> tryResolveObject(map, classType, context);
                case null, default -> ValueResolutionResult.failed;
            };
            case ArrayType arrayType -> tryResolveArray(rawValue, arrayType, currentValue, context);
            case UnionType unionType -> {
                for (Type member : unionType.getMembers()) {
                    var r = tryResolveValue(rawValue, member, asValue, currentValue, context);
                    if (r.successful())
                        yield r;
                }
                yield ValueResolutionResult.failed;
            }
            case AnyType ignored -> ValueResolutionResult.of(resolveAny(rawValue, context));
            default -> throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE_OF_TYPE, type.toExpression());
        };
    }

    private ValueResolutionResult tryResolvePrimitive(Object rawValue, PrimitiveType type) {
        return switch (type.getKind()) {
            case NULL ->
                    rawValue == null ? ValueResolutionResult.of(Instances.nullInstance()) : ValueResolutionResult.failed;
            case LONG -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.longInstance(((Number) rawValue).longValue())) :
                    ValueResolutionResult.failed;
            case INT -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.intInstance(((Number) rawValue).intValue())) :
                    ValueResolutionResult.failed;
            case DOUBLE -> rawValue instanceof Number n ?
                    ValueResolutionResult.of(Instances.doubleInstance(n.doubleValue())) : ValueResolutionResult.failed;
            case FLOAT -> rawValue instanceof Number n ?
                    ValueResolutionResult.of(Instances.floatInstance(n.floatValue())) : ValueResolutionResult.failed;
            case BOOLEAN -> rawValue instanceof Boolean b ?
                    ValueResolutionResult.of(Instances.booleanInstance(b)) : ValueResolutionResult.failed;
            case STRING -> rawValue instanceof String s ?
                    ValueResolutionResult.of(Instances.stringInstance(s)) : ValueResolutionResult.failed;
            case PASSWORD -> rawValue instanceof String s ?
                    ValueResolutionResult.of(Instances.passwordInstance(s)) : ValueResolutionResult.failed;
            case CHAR -> rawValue instanceof Character c ?
                    ValueResolutionResult.of(Instances.charInstance(c)) : ValueResolutionResult.failed;
            case TIME -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.timeInstance(((Number) rawValue).longValue())) : ValueResolutionResult.failed;
            case VOID -> throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE_OF_TYPE, "void");
        };
    }

    private ValueResolutionResult tryResolveObject(Map<?, ?> map, ClassType type, IEntityContext context) {
        return tryResolveValueObject(map, type, context);
    }

    private ValueResolutionResult tryResolveValueObject(Object rawValue, ClassType type, IEntityContext context) {
        if (rawValue instanceof Map<?, ?> map) {
            var classCode = (String) map.get(KEY_CLASS);
            ClassType actualType;
            if (classCode != null) {
                actualType = getKlass(classCode, context);
                if (!type.isAssignableFrom(actualType))
                    return ValueResolutionResult.failed;
            } else
                actualType = type;
            var instance = saveObject(map, actualType, context);
            return instance != null ? ValueResolutionResult.of(instance.getReference()) : ValueResolutionResult.failed;
        } else
            return ValueResolutionResult.failed;
    }

    private @Nullable ClassInstance saveObject(Map<?, ?> map, ClassType type, IEntityContext context) {
        var id = (String) map.get(KEY_ID);
        if (id != null) {
            var inst = (ClassInstance) context.getInstanceContext().get(Id.parse(id));
            if (!type.isInstance(inst.getReference()))
                return null;
            updateObject(inst, map, context);
            return inst;
        } else {
            return createObject(map, type, context);
        }
    }

    private ValueResolutionResult tryResolveArray(Object rawValue, ArrayType type, @Nullable Value currentValue, IEntityContext context) {
        if (!type.isValue() && rawValue instanceof String s)
            return tryResolveReference(s, type, context);
        else if (rawValue instanceof List<?> list) {
            var elements = new ArrayList<Value>();
            var isChildArray = type.isChildArray();
            for (Object o : list) {
                var r = tryResolveValue(o, type.getElementType(), isChildArray, null, context);
                if (r.successful())
                    elements.add(r.resolved());
                else
                    return ValueResolutionResult.failed;
            }
            if (currentValue != null && currentValue.isArray() && type.isInstance(currentValue)) {
                var currentArray = currentValue.resolveArray();
                currentArray.clear();
                currentArray.addAll(elements);
                return ValueResolutionResult.of(currentArray.getReference());
            } else {
                var actualType = new ArrayType(type.getElementType().getUpperBound2(), type.getKind());
                return ValueResolutionResult.of(new ArrayInstance(actualType, elements).getReference());
            }
        } else
            return ValueResolutionResult.failed;
    }

    private ValueResolutionResult tryResolveList(List<?> list, ClassType type, @Nullable Value currentValue, IEntityContext context) {
        ListNative listNative;
        if (currentValue != null && currentValue.isObject() && type.isInstance(currentValue)) {
            listNative = new ListNative(currentValue.resolveObject());
            listNative.clear();
        } else {
            ClassType concreteType;
            if (type.isInterface() || type.isAbstract()) {
                var iterableType = type.findAncestorByKlass(StdKlass.iterable.get());
                if (iterableType != null) {
                    var elementType = iterableType.getTypeArguments().get(0).getUpperBound2();
                    concreteType = ClassType.create(StdKlass.arrayList.get(), List.of(elementType));
                    if (!type.isAssignableFrom(concreteType))
                        return ValueResolutionResult.failed;
                } else
                    return ValueResolutionResult.failed;
            } else if (type.isList())
                concreteType = type;
            else
                return ValueResolutionResult.failed;
            listNative = new ListNative(ClassInstance.allocate(concreteType));
            listNative.List();
        }
        var elements = new ArrayList<Value>();
        var actualType = listNative.getInstance().getType();
        var isChildList = actualType.getKlass().isChildList();
        var elementType = actualType.getFirstTypeArgument();
        for (Object o : list) {
            var r = tryResolveValue(o, elementType, isChildList, null, context);
            if (r.successful())
                elements.add(r.resolved());
            else
                return ValueResolutionResult.failed;
        }
        elements.forEach(listNative::add);
        return ValueResolutionResult.of(listNative.getInstance().getReference());
    }

    private ValueResolutionResult tryResolveReference(Object rawValue, Type type, IEntityContext context) {
        if (rawValue instanceof String str) {
            var id = Id.tryParse(str);
            if (id != null) {
                var inst = context.getInstanceContext().get(id);
                return type.isInstance(inst.getReference()) ? ValueResolutionResult.of(inst.getReference()) : ValueResolutionResult.failed;
            }
        }
        return ValueResolutionResult.failed;
    }

    private Value resolveAny(Object rawValue, IEntityContext context) {
        if (rawValue == null)
            return Instances.nullInstance();
        if (rawValue instanceof String str) {
            if (str.startsWith("0")) {
                var id = Id.tryParse(str);
                if (id != null)
                    return context.getInstanceContext().get(id).getReference();
            }
            return Instances.stringInstance(str);
        }
        if(rawValue instanceof Character c)
            return Instances.charInstance(c);
        if(ValueUtils.isLong(rawValue))
            return Instances.longInstance(((Number) rawValue).longValue());
        if (ValueUtils.isInteger(rawValue))
            return Instances.intInstance(((Number) rawValue).intValue());
        if (ValueUtils.isDouble(rawValue))
            return Instances.doubleInstance(((Number) rawValue).doubleValue());
        if (ValueUtils.isFloat(rawValue))
            return Instances.floatInstance(((Number) rawValue).floatValue());
        if (rawValue instanceof Boolean b)
            return Instances.booleanInstance(b);
        if (rawValue instanceof List<?> list) {
            var listType = ClassType.create(StdKlass.arrayList.get(), List.of(Types.getAnyType()));
            return Instances.createList(listType,
                    NncUtils.map(list, e -> resolveAny(e, context))).getReference();
        }
        throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, NncUtils.toJSONString(rawValue));
    }

    private ClassInstance createObject(Map<?, ?> map, ClassType type, IEntityContext context) {
        var klassCode = map.get("$class");
        var actualType = klassCode != null ?
                Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, klassCode)).getType() : type;
        var r = resolveConstructor(actualType, map, context);
        var self = ClassInstance.allocate(actualType);
        context.getInstanceContext().bind(self);
        var result = Flows.execute(r.method, self, r.arguments, context);
        if (result.exception() != null)
            throw new InternalException("Failed to instantiate " + type.getTypeDesc() + " with value " + map
                    + ": " + ThrowableNative.getMessage(result.exception()));
        var updateMap = new HashMap<String, Object>();
        map.forEach((k, v) -> {
            if (k instanceof String s)
                updateMap.put(s, v);
        });
        r.method.getRawFlow().getParameters().forEach(p -> updateMap.remove(p.getName()));
        updateObject(self, updateMap, context);
        return self;
    }

    private void updateObject(ClassInstance instance, Object json, IEntityContext context) {
        //noinspection unchecked
        var map = (Map<String, Object>) json;
        var klass = instance.getKlass();
        instance.setDirectlyModified(true);
        map.forEach((k, v) -> {
            var field = klass.findFieldByName(k);
            if (field != null) {
                if (!field.isReadonly())
                    instance.setField(field, resolveValue(v, field.getType(), false, instance.getField(field), context));
            } else {
                var setter = klass.findSetterByPropertyName(k);
                if (setter != null) {
                    var getter = klass.findGetterByPropertyName(k);
                    var existing = getter != null ? Flows.invokeGetter(getter.getRef(), instance, context) : null;
                    Flows.invokeSetter(setter.getRef(), instance, resolveValue(v, setter.getParameterTypes().get(0), false, existing, context), context);
                }
            }
        });
    }

    private ResolutionResult resolveConstructor(ClassType klass, Map<?, ?> map, IEntityContext context) {
        for (var method : klass.getMethods()) {
            if (method.isConstructor()) {
                var args = tryResolveConstructor(method, map, context);
                if (args != null)
                    return new ResolutionResult(method, args);
            }
        }
        throw new InternalException("Can not resolve method in klass " + klass.getName() + " for map " + map);
    }

    private List<Value> tryResolveConstructor(MethodRef method, Map<?, ?> map, IEntityContext context) {
        var arguments = new ArrayList<Value>();
        for (var parameter : method.getParameters()) {
            var v = map.get(parameter.getName());
            var r = tryResolveValue(v, parameter.getType(), false, null, context);
            if (r.successful)
                arguments.add(r.resolved);
            else
                return null;
        }
        return arguments;
    }

    private record ValueResolutionResult(boolean successful, Value resolved) {

        static ValueResolutionResult failed = new ValueResolutionResult(false, null);

        static ValueResolutionResult of(Value resolved) {
            return new ValueResolutionResult(true, resolved);
        }

    }

    private record ResolutionResult(MethodRef method, List<Value> arguments) {
    }

    private void logTxId() {
        if(jdbcTemplate != null) {
            var txId = jdbcTemplate.queryForObject("SELECT pg_catalog.txid_current();", Long.class);
            logger.info("Transaction ID: {}", txId);
        }
    }

    @Override
    public IEntityContext newContext() {
        var appId = ContextUtil.getAppId();
        return entityContextFactory.newContext(appId, metaContextCache.get(appId));
    }

    @Autowired
    public void setTransactionTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
