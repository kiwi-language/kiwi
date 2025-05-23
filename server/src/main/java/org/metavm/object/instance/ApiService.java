package org.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.dto.ClassTypeDTO;
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
import org.metavm.object.instance.rest.dto.*;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class ApiService extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    public static final String KEY_ID = "$id";
    public static final String KEY_CLASS = "$class";

    private final MetaContextCache metaContextCache;
    private final InstanceQueryService instanceQueryService;

    public ApiService(EntityContextFactory entityContextFactory, MetaContextCache metaContextCache,  InstanceQueryService instanceQueryService) {
        super(entityContextFactory);
        this.metaContextCache = metaContextCache;
        this.instanceQueryService = instanceQueryService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String handleNewInstance(String classCode, List<ArgumentDTO> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, null, rawArguments, false, true, context);
            var self = ClassInstanceBuilder.newBuilder(klass, context.allocateRootId(klass)).build();
            execute(r.method, self, r.arguments, request, response, context);
            var result = self.getReference();
            context.bind(self);
            context.finish();
            var map = (ReferencedTO) formatValue(result, false, false);
            return map.id();
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ValueDTO handleMethodCall(InvokeRequest request, HttpRequest httpRequest, HttpResponse httpResponse) {
        try (var context = newContext()) {
            Value result;
             if (request.receiver() instanceof StringValueDTO s) {
                 ClassInstance bean;
                 if (s.value().indexOf('.') == -1 && (bean = tryResolveBean(s.value(), context)) != null)
                    result = executeInstanceMethod(bean, request.method(), request.arguments(), httpRequest, httpResponse, context);
                 else {
                     var klassName = s.value().contains(".") ? s.value() : NamingUtils.firstCharToUpperCase(s.value());
                     var klass = getKlass(klassName, context);
                     var r = resolveMethod(klass, request.method(), request.arguments(), true, false, context);
                     result = execute(r.method, null, r.arguments, httpRequest, httpResponse, context);
                 }
            } else  {
                 var r = tryResolveObject(request.receiver(), AnyType.instance, null, context);
                 if (!r.successful)
                     throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
                 result = executeInstanceMethod(r.resolved.resolveObject(), request.method(), request.arguments(), httpRequest, httpResponse, context);
             }
            context.finish();
            return formatValue(result, false, false);
        }
    }

    private ClassInstance resolveBean(String name, IInstanceContext context) {
        var bean = tryResolveBean(name, context);
        if (bean == null)
            throw new BusinessException(ErrorCode.BEAN_NOT_FOUND, name);
        return bean;
    }

    private @Nullable ClassInstance tryResolveBean(String name, IInstanceContext context) {
        var registry = BeanDefinitionRegistry.getInstance(context);
        return registry.tryGetBean(name);
    }

    private Value executeInstanceMethod(ClassInstance self,
                                        String methodCode,
                                        List<ArgumentDTO> rawArguments,
                                        HttpRequest request,
                                        HttpResponse response,
                                        IInstanceContext context) {
        var r = resolveMethod(self.getInstanceType(), methodCode, rawArguments, false, false, context);
        var method = r.method;
        return execute(method, self, r.arguments, request, response, context);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Object handleStaticMethodCall(String classCode, String methodCode, List<ArgumentDTO> rawArguments, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, methodCode, rawArguments, true, false, context);
            var inst = execute(r.method, null, r.arguments, request, response, context);
            context.finish();
            return formatValue(inst, false, false);
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
    public ObjectDTO getInstance(String id) {
        try (var context = newContext()) {
            return (ObjectDTO) formatObject((ClassInstance) context.get(Id.parse(id)), false, true, true);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Object getStatic(String className, String fieldName) {
        try(var context = newContext()) {
            var klass = getKlass(className, context);
            var sft = StaticFieldTable.getInstance(klass, context);
            return formatValue(sft.getByName(fieldName), false, false);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String saveInstance(ObjectDTO object, HttpRequest request, HttpResponse response) {
        try (var context = newContext()) {
//            logTxId();
            var inst = doIntercepted(() -> {
                var r = tryResolveValue(object, AnyType.instance, true, null, context);
                if (r.successful()) {
                    var i = (Reference) r.resolved();
                    if (!context.containsInstance(i.get()))
                        context.bind(i.get());
                    return i;
                } else
                    return null;
            }, request, response, AnyType.instance, context);
            if (inst != null) {
                context.finish();
                return ((EntityReference) inst).getStringId();
            } else
                throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, object);
        }
    }

    public SearchResult search(SearchRequest request) {
        try (var entityContext = newContext()) {
            var klass = entityContext.getKlassByQualifiedName(request.type().qualifiedName());
            var classType = klass.getType();
            var fields = new ArrayList<InstanceQueryField>();
            request.terms().forEach(term -> {
                var field = klass.findFieldByName(term.field());
                if (field != null) {
                    if (field.getType().isNumber() && term.min() != null) {
                        var min = resolveValue(term.min(), field.getType(), false, null, entityContext);
                        var max = resolveValue(term.max(), field.getType(), false, null, entityContext);
                        fields.add(new InstanceQueryField(field, null, min, max));
                    }
                    else {
                        fields.add(new InstanceQueryField(
                                field,
                                resolveValue(term.value(), field.getType(), false, null, entityContext),
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
                    .page(request.page())
                    .pageSize(request.pageSize())
                    .build();
            var dataPage1 = instanceQueryService.query(internalQuery, entityContext);
            return new SearchResult(
                    Utils.map(dataPage1.items(), i -> (ObjectDTO) formatValue(i, true, false)),
                    dataPage1.total()
            );
        }
    }

    private Value handleExecutionResult(FlowExecResult result) {
        if (result.exception() != null) {
            var throwableNative = new ThrowableNative(result.exception());
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, throwableNative.getMessage().getTitle());
        } else
            return Objects.requireNonNullElseGet(result.ret(), Instances::nullInstance);
    }

    private ValueDTO formatValue(@Nullable Value value, boolean asValue, boolean includeChildren) {
        return switch (value) {
            case null -> NullDTO.instance;
            case NullValue ignored -> NullDTO.instance;
            case ByteValue b -> new ByteValueDTO(b.value);
            case ShortValue s -> new ShortValueDTO(s.value);
            case IntValue i -> new IntValueDTO(i.value);
            case LongValue l -> new LongValueDTO(l.value);
            case FloatValue f -> new FloatValueDTO(f.value);
            case DoubleValue d -> new DoubleValueDTO(d.value);
            case CharValue c -> new CharValueDTO(c.getValue());
            case BooleanValue z -> new BoolValueDTO(z.getValue());
            case TimeValue t -> new LongValueDTO(t.getValue());
            case StringReference s -> new StringValueDTO(s.getValue());
            case Reference reference -> {
                var resolved = reference.get();
                switch (resolved) {
                    case Entity entity -> {
                        yield new ReferencedTO(entity.getStringId(), new ClassTypeDTO(entity.getInstanceType().getTypeDesc()), null);
                    }
                    case ClassInstance clsInst -> {
                        var klass = clsInst.getInstanceKlass();
                        if (klass == StdKlass.byte_.get())
                            yield new ByteValueDTO(Instances.toJavaByte(reference));
                        if (klass == StdKlass.short_.get())
                            yield new ShortValueDTO(Instances.toJavaShort(reference));
                        if (klass == StdKlass.integer.get())
                            yield new IntValueDTO(Instances.toJavaInt(reference));
                        if (klass == StdKlass.long_.get())
                            yield new LongValueDTO(Instances.toJavaLong(reference));
                        if (klass == StdKlass.float_.get())
                            yield new FloatValueDTO(Instances.toJavaFloat(reference));
                        if (klass == StdKlass.double_.get())
                            yield new DoubleValueDTO(Instances.toJavaDouble(reference));
                        if (klass == StdKlass.character.get())
                            yield new CharValueDTO(Instances.toJavaChar(reference));
                        if (klass == StdKlass.boolean_.get())
                            yield new BoolValueDTO(Instances.toJavaBoolean(reference));
                        if (clsInst.isList())
                            yield formatList(clsInst, includeChildren);
                        else if (asValue || reference instanceof ValueReference)
                            yield formatObject(clsInst, false, true, includeChildren);
                        else
                            yield formatObject(clsInst, true, false, false);
                    }
                    case ArrayInstance array -> {
                        yield formatArray(array, includeChildren);
                    }
                    case null, default -> throw new IllegalStateException("Unrecognized DurableInstance: " + resolved);
                }
            }
            default -> throw new BusinessException(ErrorCode.FAILED_TO_FORMAT_VALUE, value);
        };
    }

    private ValueDTO formatObject(ClassInstance instance,
                                             boolean includeSummary,
                                             boolean includeFields ,
                                             boolean includeChildren) {
        var type = instance.getInstanceType();
        if (type.isBean()) {
            var beanName = Objects.requireNonNull(type.getKlass().getAttribute(AttributeNames.BEAN_NAME),
                    () -> "Bean name not found for class: " + type.getKlass().getQualifiedName());
            if (resolveBean(beanName, instance.getContext()) == instance) {
                return new BeanDTO(beanName);
            }
        }
        if (instance.isEnum()) {
            var name = Instances.toJavaString(instance.getField(StdField.enumName.get()));
            return new EnumConstantDTO(type.getTypeDesc(), name);
        }
        var id = instance.getStringId();
        if (!includeFields && !includeChildren)
            return new ReferencedTO(id, new ClassTypeDTO(type.getTypeDesc()), includeSummary ? instance.getSummary() : null);
        var fields = new ArrayList<FieldDTO>();
        if (includeFields) {
            instance.forEachField((field, value) -> fields.add(new FieldDTO(field.getName(), formatValue(value, false, false))));
        }
        var children = new ArrayList<ObjectDTO>();
        if (includeChildren) {
            instance.forEachChild(c -> {
                var child = (ClassInstance) c;
                children.add((ObjectDTO) formatObject(child, includeSummary, includeFields, true));
            });
        }
        return new ObjectDTO(id, new ClassTypeDTO(type.getTypeDesc()), fields, children);
    }

    private ArrayDTO formatArray(ArrayInstance arrayInstance, boolean excludingChildren) {
        var list = new ArrayList<ValueDTO>();
        arrayInstance.forEach(e -> list.add(formatValue(e, false, excludingChildren)));
        return new ArrayDTO(list);
    }

    private ArrayDTO formatList(ClassInstance instance, boolean excludingChildren) {
        var list = new ArrayList<ValueDTO>();
        Instances.toJavaList(instance).forEach(e -> list.add(formatValue(e, false, excludingChildren)));
        return new ArrayDTO(list);
    }

    private ResolutionResult resolveMethod(@NotNull ClassType klass, String methodCode, List<ArgumentDTO> rawArguments, boolean _static, boolean constructor, IInstanceContext context) {
        var methodRef = methodCode != null ?
                TypeParser.parseSimpleMethodRef(methodCode, name -> getKlass(name, context).getKlass()) : null;
        var queue = new LinkedList<ClassType>();
        klass.foreachSuperClass(queue::offer);
        do {
            var k = Objects.requireNonNull(queue.poll());
            for (MethodRef method : k.getMethods()) {
                if (method.isPublic() && !method.isAbstract() && (methodRef == null || methodRef.name().equals(method.getName()))
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

    private List<Value> tryResolveArguments(MethodRef method, List<ArgumentDTO> rawArguments, IInstanceContext context) {
        var resolvedArgs = new ArrayList<Value>();
        var argMap = Utils.toMap(rawArguments, ArgumentDTO::name, ArgumentDTO::value);
        int i = 0;
        for (ParameterRef parameter : method.getParameters()) {
            var arg = argMap.get(parameter.getName());
            if (arg == null)
                arg = argMap.get("$arg" + i);
            i++;
            if (arg == null) {
                if (parameter.getType().isNullable()) {
                    resolvedArgs.add(Instances.nullInstance());
                    continue;
                }
                else
                    return null;
            }
            var r = tryResolveValue(arg, parameter.getType(), false, null, context);
            if (!r.successful())
                return null;
            resolvedArgs.add(r.resolved());
        }
        return resolvedArgs;
    }

    private Value resolveValue(ValueDTO rawValue, Type type, @SuppressWarnings("SameParameterValue") boolean asValue, @Nullable Value currentValue, IInstanceContext context) {
        var r = tryResolveValue(rawValue, type, asValue, currentValue, context);
        if (r.successful())
            return r.resolved();
        else
            throw new BusinessException(ErrorCode.VALUE_RESOLUTION_ERROR, rawValue, type.getTypeDesc());
    }

    private ValueResolutionResult tryResolveValue(ValueDTO rawValue, Type type, boolean asValue, @Nullable Value currentValue, IInstanceContext context) {
        return switch (type) {
            case NullType ignored ->
                    rawValue instanceof NullDTO ? ValueResolutionResult.of(Instances.nullInstance()) : ValueResolutionResult.failed;
            case PrimitiveType primitiveType -> tryResolvePrimitive(rawValue, primitiveType);
            case KlassType classType -> switch (rawValue) {
                case StringValueDTO s -> {
                    if (classType.isAssignableFrom(StdKlass.string.type()))
                        yield ValueResolutionResult.of(Instances.stringInstance(s.value()));
                    yield tryResolveObject(s, type, null, context);
//                    yield ValueResolutionResult.failed;
                }
                case LongValueDTO l->  {
                    if (classType.isAssignableFrom(StdKlass.long_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedLongInstance(l.value()));
                    else if (classType.isAssignableFrom(StdKlass.double_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(l.value()));
                    else if (classType.isAssignableFrom(StdKlass.float_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedFloatInstance(l.value()));
                    else
                        yield ValueResolutionResult.failed;
                }
                case DoubleValueDTO d->  {
                    if (classType.isAssignableFrom(StdKlass.double_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(d.value()));
                    else if (classType.isAssignableFrom(StdKlass.float_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedFloatInstance((float) d.value()));
                    else
                        yield ValueResolutionResult.failed;
                }
                case IntValueDTO i->  {
                    if (classType.isAssignableFrom(StdKlass.byte_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedByteInstance((byte) i.value()));
                    else if (classType.isAssignableFrom(StdKlass.short_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedShortInstance((short) i.value()));
                    else if (classType.isAssignableFrom(StdKlass.integer.type()))
                        yield ValueResolutionResult.of(Instances.wrappedIntInstance(i.value()));
                    else if (classType.isAssignableFrom(StdKlass.long_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedLongInstance(i.value()));
                    else if (classType.isAssignableFrom(StdKlass.double_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(i.value()));
                    else if (classType.isAssignableFrom(StdKlass.float_.type()))
                        yield ValueResolutionResult.of(Instances.wrappedFloatInstance(i.value()));
                    else
                        yield ValueResolutionResult.failed;
                }
                case ShortValueDTO s->  classType.isAssignableFrom(StdKlass.short_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedShortInstance(s.value())) :
                        ValueResolutionResult.failed;
                case ByteValueDTO b->  classType.isAssignableFrom(StdKlass.byte_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedByteInstance(b.value())) :
                        ValueResolutionResult.failed;
                case CharValueDTO c->  classType.isAssignableFrom(StdKlass.character.type()) ?
                        ValueResolutionResult.of(Instances.wrappedCharInstance(c.value())) :
                        ValueResolutionResult.failed;
                case FloatValueDTO f->  {
                  if (classType.isAssignableFrom(StdKlass.double_.type()))
                      yield ValueResolutionResult.of(Instances.wrappedDoubleInstance(f.value()));
                  else if (classType.isAssignableFrom(StdKlass.float_.type()))
                      yield ValueResolutionResult.of(Instances.wrappedFloatInstance(f.value()));
                  else
                      yield ValueResolutionResult.failed;
                }
                case BoolValueDTO b ->  classType.isAssignableFrom(StdKlass.boolean_.type()) ?
                        ValueResolutionResult.of(Instances.wrappedBooleanInstance(b.value())) :
                        ValueResolutionResult.failed;
                case ArrayDTO array -> tryResolveList(array, classType, currentValue, context);
                default -> tryResolveObject(rawValue, classType, null, context);
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

    private ValueResolutionResult tryResolvePrimitive(ValueDTO rawValue, PrimitiveType type) {
        return switch (type.getKind()) {
            case LONG -> rawValue instanceof IntegerValueDTO i ?
                    ValueResolutionResult.of(Instances.longInstance(i.longValue())) :
                    ValueResolutionResult.failed;
            case INT -> rawValue instanceof IntegerValueDTO i ?
                    ValueResolutionResult.of(Instances.intInstance(i.intValue())) :
                    ValueResolutionResult.failed;
            case DOUBLE -> rawValue instanceof NumberValueDTO f ?
                    ValueResolutionResult.of(Instances.doubleInstance(f.doubleValue())) :
                            ValueResolutionResult.failed;
            case FLOAT -> rawValue instanceof NumberValueDTO f ?
                    ValueResolutionResult.of(Instances.floatInstance(f.floatValue())) :
                    ValueResolutionResult.failed;
            case BOOLEAN -> rawValue instanceof BoolValueDTO b ?
                    ValueResolutionResult.of(Instances.booleanInstance(b.value())) : ValueResolutionResult.failed;
            case PASSWORD -> rawValue instanceof StringValueDTO s ?
                    ValueResolutionResult.of(Instances.passwordInstance(s.value())) : ValueResolutionResult.failed;
            case CHAR -> rawValue instanceof CharValueDTO c ?
                    ValueResolutionResult.of(Instances.charInstance(c.value())) : ValueResolutionResult.failed;
            case SHORT -> rawValue instanceof IntegerValueDTO i ?
                    ValueResolutionResult.of(Instances.shortInstance(i.shortValue())) : ValueResolutionResult.failed;
            case BYTE -> rawValue instanceof IntegerValueDTO i ?
                    ValueResolutionResult.of(Instances.byteInstance(i.byteValue())) : ValueResolutionResult.failed;
            case TIME -> rawValue instanceof IntegerValueDTO i ?
                    ValueResolutionResult.of(Instances.timeInstance(i.longValue())) : ValueResolutionResult.failed;
            case VOID -> throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE_OF_TYPE, "void");
        };
    }

    private ValueResolutionResult tryResolveObject(ValueDTO value, Type type, @Nullable ClassInstance parent, IInstanceContext context) {
        if (value instanceof ReferencedTO r)
            return tryResolveReference(r.id(), type, context);
        if (value instanceof BeanDTO b) {
            var bean = resolveBean(b.name(), context);
            if (!type.isAssignableFrom(bean.getInstanceType()))
                return ValueResolutionResult.failed;
            return ValueResolutionResult.of(bean.getReference());
        }
        if (value instanceof StringValueDTO s && type instanceof ClassType ct && ct.isEnum())
            return tryResolveEnumConstant(s.value(), ct, context);
        if (value instanceof EnumConstantDTO ec) {
            var t = getKlass(ec.type(), context);
            return tryResolveEnumConstant(ec.name(), t, context);
        }
        if (value instanceof ObjectDTO o) {
            var actualType = getKlass(o.type().qualifiedName(), context);
            var instance = saveObject(o, actualType, parent, context);
            return instance != null ? ValueResolutionResult.of(instance.getReference()) : ValueResolutionResult.failed;
        }
        else
            throw invalidRequestBody();
    }

    private @Nullable ClassInstance saveObject(ObjectDTO object, ClassType type, @Nullable ClassInstance parent, IInstanceContext context) {
        if (object.id() != null) {
            var inst = (ClassInstance) context.get(Id.parse(object.id()));
            if (!type.isInstance(inst.getReference()))
                return null;
            updateObject(inst, object, context);
            return inst;
        } else {
            return createObject(object, type, parent, context);
        }
    }

    private ValueResolutionResult tryResolveArray(Object rawValue, ArrayType type, @Nullable Value currentValue, IInstanceContext context) {
        if (rawValue instanceof ArrayDTO array) {
            var elements = new ArrayList<Value>();
            for (var e : array.elements()) {
                var r = tryResolveValue(e, type.getElementType(), false, null, context);
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

    private ValueResolutionResult tryResolveList(ArrayDTO list, ClassType type, @Nullable Value currentValue, IInstanceContext context) {
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
        for (var o : list.elements()) {
            var r = tryResolveValue(o, elementType, false, null, context);
            if (r.successful())
                elements.add(r.resolved());
            else
                return ValueResolutionResult.failed;
        }
        elements.forEach(listNative::add);
        return ValueResolutionResult.of(listNative.getInstance().getReference());
    }

    private ValueResolutionResult tryResolveReference(String stringId, Type type, IInstanceContext context) {
            var id = Id.parse(stringId);
            var inst = Objects.requireNonNull(context.get(id), () -> "Instance not found for ID: " + id);
            return type.isInstance(inst.getReference()) ? ValueResolutionResult.of(inst.getReference()) : ValueResolutionResult.failed;
    }

    private Value resolveAny(ValueDTO rawValue, IInstanceContext context) {
        if (rawValue == null)
            return Instances.nullInstance();
        if (rawValue instanceof StringValueDTO str)
            return Instances.stringInstance(str.value());
        if(rawValue instanceof ByteValueDTO b)
            return Instances.wrappedByteInstance(b.value());
        if(rawValue instanceof ShortValueDTO s)
            return Instances.wrappedShortInstance(s.value());
        if(rawValue instanceof IntValueDTO i)
            return Instances.wrappedIntInstance(i.value());
        if(rawValue instanceof LongValueDTO l)
            return Instances.wrappedLongInstance(l.value());
        if (rawValue instanceof FloatValueDTO f)
            return Instances.wrappedFloatInstance(f.value());
        if (rawValue instanceof DoubleValueDTO d)
            return Instances.wrappedDoubleInstance(d.value());
        if (rawValue instanceof BoolValueDTO b)
            return Instances.wrappedBooleanInstance(b.value());
        if (rawValue instanceof CharValueDTO c)
            return Instances.wrappedCharInstance(c.value());
        if (rawValue instanceof ArrayDTO array) {
            var listType = KlassType.create(StdKlass.arrayList.get(), List.of(Types.getAnyType()));
            return Instances.createList(listType,
                    Utils.map(array.elements(), e -> resolveAny(e, context))).getReference();
        }
        var r = tryResolveObject(rawValue, Types.getAnyType(), null, context);
        if (r.successful) return r.resolved;
        throw invalidRequestBody();
    }

    private ClassInstance createObject(ObjectDTO object, ClassType type, @Nullable ClassInstance parent, IInstanceContext context) {
        var actualType = getKlass(object.type().qualifiedName(), context);
        var id = parent != null ? parent.nextChildId() : context.allocateRootId(actualType);
        var r = resolveConstructor(actualType, object.fields(), context);
        var self = ClassInstance.allocate(id, actualType, parent);
        var result = Flows.execute(r.method, self, Utils.map(r.arguments, Value::toStackValue), context);
        context.bind(self);
        if (result.exception() != null)
            throw new BusinessException(ErrorCode.OBJECT_CREATION_ERROR, ThrowableNative.getMessage(result.exception()));
        saveChildren(object.children(), self, context);
        return self;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateObject(ClassInstance instance, ObjectDTO json, IInstanceContext context) {
        var type = instance.getInstanceType();
        json.fields().forEach(f -> {
            var name = f.name();
            var v = f.value();
            var fieldRef = type.findFieldByName(name);
            if (fieldRef != null && fieldRef.isPublic()) {
                var field = fieldRef.getRawField();
                if (!field.isReadonly())
                    instance.setField(field, resolveValue(v, fieldRef.getPropertyType(), false, instance.getField(field), context));
            } else {
                var setter = type.findSetterByPropertyName(name);
                if (setter != null) {
                    var getter = type.findGetterByPropertyName(name);
                    var existing = getter != null ? Flows.invokeGetter(getter, instance, context) : null;
                    Flows.invokeSetter(setter, instance, resolveValue(v, setter.getParameterTypes().getFirst(), false, existing, context), context);
                }
            }
        });
        saveChildren(json.children(), instance, context);
    }

    private BusinessException invalidRequestBody() {
        return new BusinessException(ErrorCode.INVALID_REQUEST_BODY);
    }

    private void saveChildren(List<ObjectDTO> children, ClassInstance instance, IInstanceContext context) {
        for (ObjectDTO child : children) {
            var childType = getKlass(child.type().qualifiedName(), context);
            if (childType.getOwner() instanceof ClassType encl && encl.isAssignableFrom(instance.getInstanceType())) {
                if (!(tryResolveObject(child, childType, instance, context).successful))
                    throw new BusinessException(ErrorCode.OBJECT_CREATION_ERROR, "error when creating child '" + Utils.toJSONString(child) + "'");
            }
            else
                throw new BusinessException(ErrorCode.INVALID_CHILD, child.type().qualifiedName(), instance.getInstanceType().getTypeDesc());
        }
    }

    private ResolutionResult resolveConstructor(ClassType klass, List<FieldDTO> fields, IInstanceContext context) {
        ResolutionResult result = null;
        for (var method : klass.getMethods()) {
            if (method.isConstructor()) {
                var args = tryResolveConstructor(method, fields, context);
                if (args != null) {
                    if (result == null || Utils.count(result.arguments, Value::isNotNull) < Utils.count(args, Value::isNotNull))
                        result = new ResolutionResult(method, args);
                }
            }
        }
        if (result == null)
            throw new BusinessException(ErrorCode.CONSTRUCTOR_NOT_FOUND, klass.getName(), fields);
        return result;
    }

    private List<Value> tryResolveConstructor(MethodRef method, List<FieldDTO> fields, IInstanceContext context) {
        var arguments = new ArrayList<Value>();
        var fieldMap = Utils.toMap(fields, FieldDTO::name, FieldDTO::value);
        for (var parameter : method.getParameters()) {
            var v = fieldMap.getOrDefault(parameter.getName(),  NullDTO.instance);
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

}
