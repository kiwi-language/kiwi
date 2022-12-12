package tech.metavm.object.instance.query;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ValueUtil;

import java.util.Arrays;
import java.util.List;

public enum Function {
    IS_BLANK(Boolean.class),
    MAX,
    MIN,
    SUM,
    IF(types -> ValueUtil.getConvertibleType(types.get(1), types.get(2))),

    ;

    private final FunctionDesc desc;

//    private final Type resultType;

    private final Class<?> resultJavaType;

    private final java.util.function.Function<List<Type>, Type> resultTypeFunc;

    Function() {
        this(null, null);
    }

    Function(Class<?> resultJavaType) {
        this(resultJavaType, null);
    }

    Function(java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        this(null, resultTypeFunc);
    }

    Function(Class<?> resultJavaType, java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        desc = new FunctionDesc(this);
//        this.resultType = resultType;
        this.resultJavaType = resultJavaType;
        this.resultTypeFunc = resultTypeFunc;
    }

    public Type getResultType(List<Type> argumentTypes/*, InstanceContext context*/) {
        if(resultJavaType != null) {
            return ModelDefRegistry.getType(resultJavaType);
        }
        if(resultTypeFunc != null) {
            return resultTypeFunc.apply(argumentTypes);
        }
        return ValueUtil.getConvertibleType(argumentTypes);
    }

    public static Function getByNameRequired(String name) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No func found for name: " + name));
    }

    public List<Class<?>> getParameterTypes() {
        return Arrays.asList(desc.getParamTypes());
    }

    public Object evaluate(List<Object> arguments) {
        return desc.evaluate(arguments.toArray());
    }

}
