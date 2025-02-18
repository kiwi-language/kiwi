package org.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.ArrayListNative;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.MethodRef;
import org.metavm.flow.ParameterRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.SearchResult;
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
    private final InstanceQueryService instanceQueryService;

    public ApiService(EntityContextFactory entityContextFactory, MetaContextCache metaContextCache,  InstanceQueryService instanceQueryService) {
        super(entityContextFactory);
        this.metaContextCache = metaContextCache;
        this.instanceQueryService = instanceQueryService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String handleNewInstance(String classCode, List<Object> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, null, rawArguments, false, true, context);
            var self = ClassInstanceBuilder.newBuilder(klass, context.allocateRootId(klass)).build();
            var result = execute(r.method, self, r.arguments, request, response, context);
            context.bind(self);
            context.finish();
            return (String) formatInstance(result, false);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Object handleMethodCall(String qualifier, String methodCode, Object rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            Value result;
            if (qualifier.startsWith("0")) {
                var self = (ClassInstance) context.get(Id.parse(qualifier));
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

    private Value executeInstanceMethod(ClassInstance self,
                                        String methodCode,
                                        Object rawArguments,
                                        HttpRequest request,
                                        HttpResponse response,
                                        IInstanceContext context) {
        var r = resolveMethod(self.getInstanceType(), methodCode, rawArguments, false, false, context);
        var method = r.method;
        return execute(method, self, r.arguments, request, response, context);
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
                          IInstanceContext context) {
        return doIntercepted(
                () -> handleExecutionResult(Flows.execute(method, self, Utils.map(arguments, Value::toStackValue), context)),
                request,
                response,
                method.getReturnType(),
                context
        );
    }

    private Value doIntercepted(Supplier<Value> action, HttpRequest request, HttpResponse response, Type returnType, IInstanceContext context) {
        var registry = BeanDefinitionRegistry.getInstance(context);
        var beforeMethod = StdMethod.interceptorBefore.get();
        var afterMethod = StdMethod.interceptorAfter.get();
        var interceptors = registry.getInterceptors();
        var reqInst = (Instance) request;
        var respInst = (Instance) response;
        for (ClassInstance interceptor : interceptors) {
            Flows.invokeVirtual(beforeMethod.getRef(), interceptor, List.of(reqInst.getReference(), respInst.getReference()), context);
        }
        var result = action.get();
        for (ClassInstance interceptor : interceptors) {
            result = Objects.requireNonNull(Flows.invokeVirtual(afterMethod.getRef(), interceptor, List.of(reqInst.getReference(), respInst.getReference(), result), context));
        }
        return returnType.fromStackValue(result);
    }

    private void ensureSuccessful(FlowExecResult result) {
        if (result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
    }

    private ClassType getKlass(String classCode, IInstanceContext context) {
        ParserTypeDefProvider typeDefProvider = name -> context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
                Instances.stringInstance(name));
        var type = (ClassType) new TypeParserImpl(typeDefProvider).parseType(classCode);
        if (type == null)
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, classCode);
        return type;
    }

    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Object getInstance(String id) {
        try (var context = newContext()) {
            return formatInstance(context.get(Id.parse(id)).getReference(), true);
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
    public String saveInstance(String classCode, Map<String, Object> object, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
//            logTxId();
            ClassType type;
            if(object.containsKey(KEY_ID)) {
                var inst0 = (ClassInstance) context.get(Id.parse((String) object.get(KEY_ID)));
                type = inst0.getInstanceType();
            }
            else
                type = getKlass(classCode, context);
            var inst = doIntercepted(() -> {
                var r = tryResolveValue(object, type, true, null, context);
                if (r.successful()) {
                    var i = (Reference) r.resolved();
                    if (!context.containsInstance(i.get()))
                        context.bind(i.get());
                    return i;
                } else
                    return null;
            }, request, response, type, context);
            if (inst != null) {
                context.finish();
                return inst.getStringId();
            } else
                throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, object);
        }
    }


    public SearchResult search(String className, Map<String, Object> query, int page, int pageSize, boolean returnObjects) {
        try (var entityContext = newContext()) {
            var klass = entityContext.getKlassByQualifiedName(className);
            var classType = klass.getType();
            var fields = new ArrayList<InstanceQueryField>();
            query.forEach((name, value) -> {
                var field = klass.findFieldByName(name);
                if (field != null) {
                    if (field.getType().isNumber() && value instanceof List<?> list && list.size() == 2) {
                        var min = resolveValue(list.getFirst(), field.getType(), false, null, entityContext);
                        var max = resolveValue(list.get(1), field.getType(), false, null, entityContext);
                        fields.add(new InstanceQueryField(field, null, min, max));
                    }
                    else {
                        fields.add(new InstanceQueryField(
                                field,
                                resolveValue(value, field.getType(), false, null, entityContext),
                                null,
                                null
                        ));
                    }
                }
            });
            var internalQuery = InstanceQueryBuilder.newBuilder(classType.getKlass())
//                    .searchText(searchText)
//                    .newlyCreated(NncUtils.map(query.createdIds(), Id::parse))
                    .fields(fields)
//                    .expression(query.expression())
                    .page(page)
                    .pageSize(pageSize)
                    .build();
            var dataPage1 = instanceQueryService.query(internalQuery, entityContext);
            if (returnObjects) {
              return new SearchResult(
                      Utils.map(dataPage1.data(), i -> formatInstance(i, true)),
                      dataPage1.total()
              );
            } else {
                return new SearchResult(
                        Utils.map(dataPage1.data(), Reference::getStringId),
                        dataPage1.total()
                );
            }
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
            case NullValue ignored -> null;
            case PrimitiveValue primitiveValue -> primitiveValue.getValue();
            case StringReference s -> s.getValue();
            case Reference reference -> {
                var resolved = reference.get();
                switch (resolved) {
                    case Entity entity -> {
                        yield entity.toJson();
                    }
                    case ClassInstance clsInst -> {
                        var klass = clsInst.getInstanceKlass();
                        if (klass == StdKlass.byte_.get())
                            yield Instances.toJavaByte(reference);
                        if (klass == StdKlass.short_.get())
                            yield Instances.toJavaShort(reference);
                        if (klass == StdKlass.integer.get())
                            yield Instances.toJavaInt(reference);
                        if (klass == StdKlass.long_.get())
                            yield Instances.toJavaLong(reference);
                        if (klass == StdKlass.float_.get())
                            yield Instances.toJavaFloat(reference);
                        if (klass == StdKlass.double_.get())
                            yield Instances.toJavaDouble(reference);
                        if (klass == StdKlass.character.get())
                            yield Instances.toJavaChar(reference);
                        if (klass == StdKlass.boolean_.get())
                            yield Instances.toJavaBoolean(reference);
                        if (clsInst.isEnum())
                            yield Instances.toJavaString(clsInst.getField(StdField.enumName.get()));
                        if (clsInst.isList())
                            yield formatList(clsInst);
                        else if (asValue || reference.isValueReference())
                            yield formatValueObject(clsInst);
                        else
                            yield reference.getStringId();
                    }
                    case ArrayInstance array -> {
                        yield formatArray(array);
                    }
                    case null, default -> throw new IllegalStateException("Unrecognized DurableInstance: " + resolved);
                }
            }
            default -> throw new BusinessException(ErrorCode.FAILED_TO_FORMAT_VALUE, instance);
        };
    }

    private Map<String, Object> formatValueObject(ClassInstance instance) {
        var map = new LinkedHashMap<String, Object>();
        var id = instance.getStringId();
        if (id != null)
            map.put(KEY_ID, id);
        map.put(KEY_CLASS, instance.getInstanceType().getKlass().getQualifiedName());
        instance.forEachField((field, value) -> map.put(field.getName(), formatInstance(value, false)));
        return map;
    }

    private List<Object> formatArray(ArrayInstance arrayInstance) {
        var list = new ArrayList<>();
        arrayInstance.forEach(e -> list.add(formatInstance(e, false)));
        return list;
    }

    private List<Object> formatList(ClassInstance instance) {
        var list = new ArrayList<>();
        Instances.toJavaList(instance).forEach(e -> list.add(formatInstance(e, false)));
        return list;
    }

    private ResolutionResult resolveMethod(@NotNull ClassType klass, String methodCode, Object rawArguments, boolean _static, boolean constructor, IInstanceContext context) {
        var methodRef = methodCode != null ?
                TypeParser.parseSimpleMethodRef(methodCode, name -> getKlass(name, context).getKlass()) : null;
        var queue = new LinkedList<ClassType>();
        klass.foreachSuperClass(queue::offer);
        var argCount = getArgumentCount(rawArguments);
        do {
            var k = Objects.requireNonNull(queue.poll());
            for (MethodRef method : k.getMethods()) {
                if (method.isPublic() && !method.isAbstract() && (methodRef == null || methodRef.name().equals(method.getName())) && method.getParameterCount() == argCount
                        && _static == method.isStatic() && constructor == method.isConstructor()) {
                    method = methodRef != null ? method.getParameterized(methodRef.typeArguments()) : method;
                    var resolvedArgs = tryResolveArguments(method, rawArguments, context);
                    if (resolvedArgs != null)
                        return new ResolutionResult(method, resolvedArgs);
                }
            }
            k.getInterfaces().forEach(queue::offer);
        } while (!queue.isEmpty());
        if (DebugEnv.traceMethodResolution) {
            logger.trace("Failed to resolve method '{}' for arguments '{}' in class '{}'",
                    methodCode, rawArguments, klass.getTypeDesc());
            klass.foreachMethod(m -> logger.trace("Method: {}", m.getQualifiedSignature()));
        }
        throw new BusinessException(ErrorCode.METHOD_RESOLUTION_FAILED, klass.getQualifiedName() + "." + methodCode,
                rawArguments);
    }

    private int getArgumentCount(Object rawArgument) {
        return rawArgument instanceof List<?> list ? list.size() : ((Map<?,?>) rawArgument).size();
    }

    private List<Value> tryResolveArguments(MethodRef method, Object rawArguments, IInstanceContext context) {
        var resolvedArgs = new ArrayList<Value>();
        if (rawArguments instanceof List<?> rawArgList) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                var r = tryResolveValue(rawArgList.get(i), method.getParameterType(i), false, null, context);
                if (!r.successful())
                    return null;
                resolvedArgs.add(r.resolved());
            }
        }
        else if (rawArguments instanceof Map<?,?> rawArgMap) {
            for (ParameterRef parameter : method.getParameters()) {
                var rawArg = rawArgMap.get(parameter.getName());
                if (rawArg == null)
                    return null;
                var r = tryResolveValue(rawArg, parameter.getType(), false, null, context);
                if (!r.successful())
                    return null;
                resolvedArgs.add(r.resolved());
            }
        }
        return resolvedArgs;
    }

    private Value resolveValue(Object rawValue, Type type, @SuppressWarnings("SameParameterValue") boolean asValue, @Nullable Value currentValue, IInstanceContext context) {
        var r = tryResolveValue(rawValue, type, asValue, currentValue, context);
        if (r.successful())
            return r.resolved();
        else
            throw new InternalException("Failed to resolve value " + rawValue + " for type " + type.getTypeDesc());
    }

    private ValueResolutionResult tryResolveValue(Object rawValue, Type type, boolean asValue, @Nullable Value currentValue, IInstanceContext context) {
        return switch (type) {
            case NullType ignored ->
                    rawValue == null ? ValueResolutionResult.of(Instances.nullInstance()) : ValueResolutionResult.failed;
            case PrimitiveType primitiveType -> tryResolvePrimitive(rawValue, primitiveType);
            case KlassType classType -> switch (rawValue) {
                case String s -> {
                    if (asValue)
                        yield ValueResolutionResult.failed;
                    if (classType.isAssignableFrom(StdKlass.string.type()))
                        yield ValueResolutionResult.of(Instances.stringInstance(s));
                    var r = tryResolveReference(s, classType, context);
                    if (r.successful)
                        yield r;
                    if (classType.isEnum())
                        yield tryResolveEnumConstant(s, classType, context);
                    yield ValueResolutionResult.failed;
                }
                case Long l->  {
                    if (classType.isAssignableFrom(StdKlass.long_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedLongInstance(l));
                    else if (classType.isAssignableFrom(StdKlass.double_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(l));
                    else if (classType.isAssignableFrom(StdKlass.float_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedFloatInstance(l));
                    else
                        yield ValueResolutionResult.failed;
                }
                case Double d->  {
                  if (classType.isAssignableFrom(StdKlass.double_.type()))
                      yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(d));
                  else if (classType.isAssignableFrom(StdKlass.float_.type()))
                      yield ValueResolutionResult.of(Instances.wrappedFloatInstance(d.floatValue()));
                  else
                      yield ValueResolutionResult.failed;
                }
                case Integer i->  {
                    if (classType.isAssignableFrom(StdKlass.byte_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedByteInstance(i.byteValue()));
                    else if (classType.isAssignableFrom(StdKlass.short_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedShortInstance(i.shortValue()));
                    else if (classType.isAssignableFrom(StdKlass.integer.type()))
                        yield ValueResolutionResult.of(Instances.wrappedIntInstance(i));
                    else if (classType.isAssignableFrom(StdKlass.long_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedLongInstance(i));
                    else if (classType.isAssignableFrom(StdKlass.float_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedFloatInstance(i));
                    else if (classType.isAssignableFrom(StdKlass.double_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(i));
                    else
                        yield ValueResolutionResult.failed;
                }
                case Float f->  classType.isAssignableFrom(StdKlass.float_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedFloatInstance(f)) :
                        ValueResolutionResult.failed;
                case Short s->  classType.isAssignableFrom(StdKlass.short_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedShortInstance(s)) :
                        ValueResolutionResult.failed;
                case Byte b->  classType.isAssignableFrom(StdKlass.byte_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedByteInstance(b)) :
                        ValueResolutionResult.failed;
                case Character c->  classType.isAssignableFrom(StdKlass.character.type()) ?
                        ValueResolutionResult.of(Instances.wrappedCharInstance(c)) :
                        ValueResolutionResult.failed;
                case Boolean z->  classType.isAssignableFrom(StdKlass.boolean_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedBooleanInstance(z)) :
                        ValueResolutionResult.failed;
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

    private ValueResolutionResult tryResolveEnumConstant(String name, ClassType classType, IInstanceContext context) {
        var ec = Utils.find(classType.getKlass().getEnumConstants(), f -> Objects.equals(f.getName(), name));
        return ec != null ? ValueResolutionResult.of(StaticFieldTable.getInstance(classType, context).get(ec))
                : ValueResolutionResult.failed;
    }

    private ValueResolutionResult tryResolvePrimitive(Object rawValue, PrimitiveType type) {
        return switch (type.getKind()) {
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
            case PASSWORD -> rawValue instanceof String s ?
                    ValueResolutionResult.of(Instances.passwordInstance(s)) : ValueResolutionResult.failed;
            case CHAR -> rawValue instanceof Character c ?
                    ValueResolutionResult.of(Instances.charInstance(c)) : ValueResolutionResult.failed;
            case SHORT -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.shortInstance(((Number) rawValue).shortValue())) : ValueResolutionResult.failed;
            case BYTE -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.byteInstance(((Number) rawValue).byteValue())) : ValueResolutionResult.failed;
            case TIME -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.timeInstance(((Number) rawValue).longValue())) : ValueResolutionResult.failed;
            case VOID -> throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE_OF_TYPE, "void");
        };
    }

    private ValueResolutionResult tryResolveObject(Map<?, ?> map, ClassType type, IInstanceContext context) {
        return tryResolveValueObject(map, type, context);
    }

    private ValueResolutionResult tryResolveValueObject(Object rawValue, Type type, IInstanceContext context) {
        if (rawValue instanceof Map<?, ?> map) {
            var classCode = (String) map.get(KEY_CLASS);
            ClassType actualType;
            if (classCode != null) {
                actualType = getKlass(classCode, context);
                if (!type.isAssignableFrom(actualType))
                    return ValueResolutionResult.failed;
            } else if(type instanceof ClassType ct)
                actualType = ct;
            else
                return ValueResolutionResult.failed;
            var instance = saveObject(map, actualType, context);
            return instance != null ? ValueResolutionResult.of(instance.getReference()) : ValueResolutionResult.failed;
        } else
            return ValueResolutionResult.failed;
    }

    private @Nullable ClassInstance saveObject(Map<?, ?> map, ClassType type, IInstanceContext context) {
        var id = (String) map.get(KEY_ID);
        if (id != null) {
            var inst = (ClassInstance) context.get(Id.parse(id));
            if (!type.isInstance(inst.getReference()))
                return null;
            updateObject(inst, map, context);
            return inst;
        } else {
            return createObject(map, type, context);
        }
    }

    private ValueResolutionResult tryResolveArray(Object rawValue, ArrayType type, @Nullable Value currentValue, IInstanceContext context) {
        if (!type.isValueType() && rawValue instanceof String s)
            return tryResolveReference(s, type, context);
        else if (rawValue instanceof List<?> list) {
            var elements = new ArrayList<Value>();
            for (Object o : list) {
                var r = tryResolveValue(o, type.getElementType(), false, null, context);
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

    private ValueResolutionResult tryResolveList(List<?> list, ClassType type, @Nullable Value currentValue, IInstanceContext context) {
        ArrayListNative listNative;
        if (currentValue != null && currentValue.isObject() && type.isInstance(currentValue)) {
            listNative = Instances.getListNative(currentValue.resolveObject());
            listNative.clear();
        } else {
            ClassType concreteType;
            if (type.isInterface() || type.isAbstract()) {
                var iterableType = type.asSuper(StdKlass.iterable.get());
                if (iterableType != null) {
                    var elementType = iterableType.getTypeArguments().getFirst().getUpperBound2();
                    concreteType = KlassType.create(StdKlass.arrayList.get(), List.of(elementType));
                    if (!type.isAssignableFrom(concreteType))
                        return ValueResolutionResult.failed;
                } else
                    return ValueResolutionResult.failed;
            } else if (type.isList())
                concreteType = type;
            else
                return ValueResolutionResult.failed;
            listNative = Instances.getListNative(Instances.newList(concreteType));
        }
        var elements = new ArrayList<Value>();
        var actualType = listNative.getInstance().getInstanceType();
        var elementType = actualType.getFirstTypeArgument();
        for (Object o : list) {
            var r = tryResolveValue(o, elementType, false, null, context);
            if (r.successful())
                elements.add(r.resolved());
            else
                return ValueResolutionResult.failed;
        }
        elements.forEach(listNative::add);
        return ValueResolutionResult.of(listNative.getInstance().getReference());
    }

    private ValueResolutionResult tryResolveReference(Object rawValue, Type type, IInstanceContext context) {
        if (rawValue instanceof String str) {
            var id = Id.tryParse(str);
            if (id != null) {
                var inst = Objects.requireNonNull(context.get(id), () -> "Instance not found for ID: " + id);
                return type.isInstance(inst.getReference()) ? ValueResolutionResult.of(inst.getReference()) : ValueResolutionResult.failed;
            }
        }
        return ValueResolutionResult.failed;
    }

    private Value resolveAny(Object rawValue, IInstanceContext context) {
        if (rawValue == null)
            return Instances.nullInstance();
        if (rawValue instanceof String str) {
            if (str.startsWith("0")) {
                var id = Id.tryParse(str);
                if (id != null)
                    return context.get(id).getReference();
            }
            return Instances.stringInstance(str);
        }
        if(rawValue instanceof Character c)
            return Instances.wrappedCharInstance(c);
        if(ValueUtils.isLong(rawValue))
            return Instances.wrappedLongInstance(((Number) rawValue).longValue());
        if (ValueUtils.isInteger(rawValue))
            return Instances.wrappedIntInstance(((Number) rawValue).intValue());
        if (ValueUtils.isDouble(rawValue))
            return Instances.wrappedDoubleInstance(((Number) rawValue).doubleValue());
        if (ValueUtils.isFloat(rawValue))
            return Instances.wrappedFloatInstance(((Number) rawValue).floatValue());
        if (rawValue instanceof Boolean b)
            return Instances.wrappedBooleanInstance(b);
        if (rawValue instanceof Map<?,?> map) {
            var r = tryResolveValueObject(map, Types.getAnyType(), context);
            if (r.successful) return r.resolved;
        }
        if (rawValue instanceof List<?> list) {
            var listType = KlassType.create(StdKlass.arrayList.get(), List.of(Types.getAnyType()));
            return Instances.createList(listType,
                    Utils.map(list, e -> resolveAny(e, context))).getReference();
        }
        throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, Utils.toJSONString(rawValue));
    }

    private ClassInstance createObject(Map<?, ?> map, ClassType type, IInstanceContext context) {
        var klassCode = map.get("$class");
        var actualType = klassCode != null ?
                Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance((String) klassCode))).getType() : type;
        var id = context.allocateRootId(actualType);
        var r = resolveConstructor(actualType, map, context);
        var self = ClassInstance.allocate(id, actualType);
        var result = Flows.execute(r.method, self, Utils.map(r.arguments, Value::toStackValue), context);
        context.bind(self);
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

    private void updateObject(ClassInstance instance, Object json, IInstanceContext context) {
        //noinspection unchecked
        var map = (Map<String, Object>) json;
        var type = instance.getInstanceType();
        map.forEach((k, v) -> {
            var fieldRef = type.findFieldByName(k);
            if (fieldRef != null && fieldRef.isPublic()) {
                var field = fieldRef.getRawField();
                if (!field.isReadonly())
                    instance.setField(field, resolveValue(v, fieldRef.getPropertyType(), false, instance.getField(field), context));
            } else {
                var setter = type.findSetterByPropertyName(k);
                if (setter != null) {
                    var getter = type.findGetterByPropertyName(k);
                    var existing = getter != null ? Flows.invokeGetter(getter, instance, context) : null;
                    Flows.invokeSetter(setter, instance, resolveValue(v, setter.getParameterTypes().getFirst(), false, existing, context), context);
                }
            }
        });
    }

    private ResolutionResult resolveConstructor(ClassType klass, Map<?, ?> map, IInstanceContext context) {
        ResolutionResult result = null;
        for (var method : klass.getMethods()) {
            if (method.isConstructor()) {
                var args = tryResolveConstructor(method, map, context);
                if (args != null) {
                    if (result == null || Utils.count(result.arguments, Value::isNotNull) < Utils.count(args, Value::isNotNull))
                        result = new ResolutionResult(method, args);
                }
            }
        }
        if (result == null)
            throw new InternalException("Can not resolve method in klass " + klass.getName() + " for map " + map);
        return result;
    }

    private List<Value> tryResolveConstructor(MethodRef method, Map<?, ?> map, IInstanceContext context) {
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

    @Override
    public IInstanceContext newContext() {
        var appId = ContextUtil.getAppId();
        var metaContext = metaContextCache.get(appId);
        return entityContextFactory.newContext(appId, metaContext);
    }

    @Autowired
    public void setTransactionTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
