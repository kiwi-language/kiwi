package tech.metavm.object.instance.rest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryAware;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.ListNative;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

@Service
public class ApiService extends EntityContextFactoryAware {

    public ApiService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public String handleNewInstance(String classCode, List<Object> rawArguments) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, null, rawArguments, false, true, context);
            var self = ClassInstanceBuilder.newBuilder(klass.getType()).build();
            context.getInstanceContext().bind(self);
            var result = r.method().execute(self, r.arguments(), context.getInstanceContext());
            context.finish();
            return (String) handleExecutionResult(result);
        }
    }

    @Transactional
    public Object handleInstanceMethodCall(String id, String methodCode, List<Object> rawArguments) {
        try (var context = newContext()) {
            var self = (ClassInstance) context.getInstanceContext().get(Id.parse(id));
            var r = resolveMethod(self.getKlass(), methodCode, rawArguments, false, false, context);
            var result = r.method().execute(self, r.arguments(), context.getInstanceContext());
            context.finish();
            return handleExecutionResult(result);
        }
    }

    @Transactional
    public Object handleStaticMethodCall(String classCode, String methodCode, List<Object> rawArguments) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = resolveMethod(klass, methodCode, rawArguments, true, false, context);
            var result = r.method().execute(null, r.arguments(), context.getInstanceContext());
            context.finish();
            return handleExecutionResult(result);
        }
    }

    private Klass getKlass(String classCode, IEntityContext context) {
        var klass = context.selectFirstByKey(Klass.UNIQUE_CODE, classCode);
        if (klass == null)
            throw new BusinessException(ErrorCode.CLASS_NOT_FOUND, classCode);
        return klass;
    }

    public Object getInstance(String id) {
        try (var context = newContext()) {
            return formatInstance(context.getInstanceContext().get(Id.parse(id)), true);
        }
    }

    @Transactional
    public void deleteInstance(String id) {
        try (var context = newContext()) {
            var instanceContext = context.getInstanceContext();
            instanceContext.remove(instanceContext.get(Id.parse(id)));
            context.finish();
        }
    }

    @Transactional
    public String createInstance(String classCode, Object object) {
        try (var context = newContext()) {
            var klass = getKlass(classCode, context);
            var r = tryResolveValue(object, klass.getType(), true, context);
            if (r.successful()) {
                var inst = (DurableInstance) r.resolved();
                var instanceContext = context.getInstanceContext();
                if(!instanceContext.containsInstance(inst))
                    instanceContext.bind(inst);
                context.finish();
                return inst.getStringId();
            } else
                throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, object);
        }
    }

    private Object handleExecutionResult(FlowExecResult result) {
        if (result.exception() != null) {
            var throwableNative = new ThrowableNative(result.exception());
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, throwableNative.getMessage());
        } else
            return result.ret() != null ? formatInstance(result.ret(), false) : null;
    }

    private @Nullable Id tryResolveId(Object rawValue) {
        if (rawValue instanceof String s && s.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            try {
                return Id.parse(Constants.removeConstantIdPrefix(s));
            } catch (Exception ignored) {
                return null;
            }
        } else
            return null;
    }

    private Object formatInstance(Instance instance, boolean asValue) {
        return switch (instance) {
            case PrimitiveInstance primitiveInstance -> primitiveInstance.getValue();
            case ClassInstance classInstance -> {
                if (classInstance.isList())
                    yield formatList(classInstance);
                else
                    yield classInstance.isValue() || asValue ? formatValueObject(classInstance) : classInstance.getStringId();
            }
            case ArrayInstance arrayInstance -> formatArray(arrayInstance);
            default -> throw new BusinessException(ErrorCode.FAILED_TO_FORMAT_VALUE, instance);
        };
    }

    private Map<String, Object> formatValueObject(ClassInstance classInstance) {
        var map = new HashMap<String, Object>();
        classInstance.forEachField((field, value) -> map.put(field.getCode(), formatInstance(value, field.isChild())));
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

    private ResolutionResult resolveMethod(Klass klass, String methodCode, List<Object> rawArguments, boolean _static, boolean constructor, IEntityContext context) {
        for (Method method : klass.getAllMethods()) {
            if (method.isPublic() && (methodCode == null || methodCode.equals(method.getCode())) && method.getParameters().size() == rawArguments.size()
                    && _static == method.isStatic() && constructor == method.isConstructor()) {
                var resolvedArgs = tryResolveArguments(method, rawArguments, context);
                if (resolvedArgs != null)
                    return new ResolutionResult(method, resolvedArgs);
            }
        }
        throw new BusinessException(ErrorCode.METHOD_RESOLUTION_FAILED, klass.getCode() + "." + methodCode);
    }

    private List<Instance> tryResolveArguments(Method method, List<Object> rawArguments, IEntityContext context) {
        var resolvedArgs = new ArrayList<Instance>();
        for (int i = 0; i < method.getParameters().size(); i++) {
            var r = tryResolveValue(rawArguments.get(i), method.getParameters().get(i).getType(), false, context);
            if (!r.successful())
                return null;
            resolvedArgs.add(r.resolved());
        }
        return resolvedArgs;
    }

    private ValueResolutionResult tryResolveValue(Object rawValue, Type type, boolean asValue, IEntityContext context) {
        return switch (type) {
            case PrimitiveType primitiveType -> tryResolvePrimitive(rawValue, primitiveType);
            case ClassType classType -> {
                if (classType.getKlass().isList())
                    yield tryResolveList(rawValue, classType, context);
                else
                    yield tryResolveObject(rawValue, classType, asValue, context);
            }
            case ArrayType arrayType -> tryResolveArray(rawValue, arrayType, context);
            case UnionType unionType -> {
                for (Type member : unionType.getMembers()) {
                    var r = tryResolveValue(rawValue, member, asValue, context);
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
            case DOUBLE -> rawValue instanceof Number n ?
                    ValueResolutionResult.of(Instances.doubleInstance(n.doubleValue())) : ValueResolutionResult.failed;
            case BOOLEAN -> rawValue instanceof Boolean b ?
                    ValueResolutionResult.of(Instances.booleanInstance(b)) : ValueResolutionResult.failed;
            case STRING -> rawValue instanceof String s ?
                    ValueResolutionResult.of(Instances.stringInstance(s)) : ValueResolutionResult.failed;
            case PASSWORD -> rawValue instanceof String s ?
                    ValueResolutionResult.of(Instances.passwordInstance(s)) : ValueResolutionResult.failed;
            case TIME -> ValueUtils.isInteger(rawValue) ?
                    ValueResolutionResult.of(Instances.timeInstance(((Number) rawValue).longValue())) : ValueResolutionResult.failed;
            case VOID -> throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE_OF_TYPE, "void");
        };
    }

    private ValueResolutionResult tryResolveObject(Object rawValue, ClassType type, boolean asValue, IEntityContext context) {
        if ((type.isValue() || asValue)) {
            if (type.isStruct())
                return tryResolveValueObject(rawValue, type, context);
            else
                return ValueResolutionResult.failed;
        } else
            return tryResolveReference(rawValue, type, context);
    }

    private ValueResolutionResult tryResolveValueObject(Object rawValue, ClassType type, IEntityContext context) {
        if (rawValue instanceof Map<?, ?> map) {
            var klass = type.resolve();
            if (klass.isStruct()) {
                var mapping = Objects.requireNonNull(klass.getBuiltinMapping());
                var view = createObject(map, mapping.getTargetKlass(), context);
                return view != null ?
                        ValueResolutionResult.of(mapping.unmap(view, context.getInstanceContext()))
                        : ValueResolutionResult.failed;
            } else {
                var instance = createObject(map, klass, context);
                return instance != null ? ValueResolutionResult.of(instance) : ValueResolutionResult.failed;
            }
        } else
            return ValueResolutionResult.failed;
    }

    private @Nullable ClassInstance createObject(Map<?, ?> map, Klass klass, IEntityContext context) {
        var data = new HashMap<Field, Instance>();
        for (Field field : klass.getAllFields()) {
            var r = tryResolveValue(map.get(field.getCode()), field.getType(), field.isChild(), context);
            if (r.successful())
                data.put(field, r.resolved());
            else
                return null;
        }
        return ClassInstance.create(data, klass.getType());
    }

    private ValueResolutionResult tryResolveArray(Object rawValue, ArrayType type, IEntityContext context) {
        if (!type.isValue() && rawValue instanceof String s)
            return tryResolveReference(s, type, context);
        else if (rawValue instanceof List<?> list) {
            var elements = new ArrayList<Instance>();
            var isChildArray = type.isChildArray();
            for (Object o : list) {
                var r = tryResolveValue(o, type.getElementType(), isChildArray, context);
                if (r.successful())
                    elements.add(r.resolved());
                else
                    return ValueResolutionResult.failed;
            }
            return ValueResolutionResult.of(new ArrayInstance(type, elements));
        } else
            return ValueResolutionResult.failed;
    }

    private ValueResolutionResult tryResolveList(Object rawValue, ClassType type, IEntityContext context) {
        if (!type.isValue() && rawValue instanceof String s)
            return tryResolveReference(s, type, context);
        else if (rawValue instanceof List<?> list) {
            var elements = new ArrayList<Instance>();
            var klass = type.resolve();
            var isChildList = klass.isChildList();
            for (Object o : list) {
                var r = tryResolveValue(o, type.getListElementType(), isChildList, context);
                if (r.successful())
                    elements.add(r.resolved());
                else
                    return ValueResolutionResult.failed;
            }
            if (type.getKlass() == StandardTypes.getListKlass()) {
                type = type.isParameterized() ? new ClassType(StandardTypes.getReadWriteListKlass(), type.getTypeArguments())
                        : StandardTypes.getReadWriteListKlass().getType();
            }
            var listInst = ClassInstance.allocate(type);
            var listNative = new ListNative(listInst);
            listNative.List();
            elements.forEach(listNative::add);
            return ValueResolutionResult.of(listInst);
        } else
            return ValueResolutionResult.failed;
    }

    private ValueResolutionResult tryResolveReference(Object rawValue, Type type, IEntityContext context) {
        if (rawValue instanceof String str) {
            var id = tryResolveId(str);
            if (id != null) {
                var inst = context.getInstanceContext().get(id);
                return type.isInstance(inst) ? ValueResolutionResult.of(inst) : ValueResolutionResult.failed;
            }
        }
        return ValueResolutionResult.failed;
    }

    private Instance resolveAny(Object rawValue, IEntityContext context) {
        if (rawValue == null)
            return Instances.nullInstance();
        if (rawValue instanceof String str) {
            var id = tryResolveId(str);
            if (id != null)
                return context.getInstanceContext().get(id);
            return Instances.stringInstance(str);
        }
        if (ValueUtils.isInteger(rawValue))
            return Instances.longInstance(((Number) rawValue).longValue());
        if (ValueUtils.isFloat(rawValue))
            return Instances.doubleInstance(((Number) rawValue).doubleValue());
        if (rawValue instanceof Boolean b)
            return Instances.booleanInstance(b);
        throw new BusinessException(ErrorCode.FAILED_TO_RESOLVE_VALUE, NncUtils.toJSONString(rawValue));
    }

    private record ValueResolutionResult(boolean successful, Instance resolved) {

        static ValueResolutionResult failed = new ValueResolutionResult(false, null);

        static ValueResolutionResult of(Instance resolved) {
            return new ValueResolutionResult(true, resolved);
        }

    }

    private record ResolutionResult(Method method, List<Instance> arguments) {
    }

}
