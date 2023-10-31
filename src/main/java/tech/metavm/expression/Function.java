package tech.metavm.expression;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ValueUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public enum Function {

    MAX$_INT(Integer.class, Integer.class, Integer.class),
    MAX$_LONG(Long.class, Long.class, Long.class),
    MAX$_DOUBLE(Double.class, Double.class, Double.class),

    MIN$_INT(Integer.class, Integer.class, Integer.class),
    MIN$_LONG(Long.class, Long.class, Long.class),
    MIN$_DOUBLE(Double.class, Double.class, Double.class),

    SUM$_INT(Integer.class, Integer.class, Integer.class),
    SUM$_LONG(Long.class, Long.class, Long.class),
    SUM$_DOUBLE(Double.class, Double.class, Double.class),

    IF(Object.class, List.of(Boolean.class, Object.class, Object.class),
            types -> ValueUtil.getConvertibleType(types.get(1), types.get(2))),

    IS_BLANK(Boolean.class, String.class),

    NOW(Date.class),

    LEN(Boolean.class, Object.class),

    HAS_NEXT(Boolean.class, Object.class),

//    IS_ASSIGNABLE(Boolean.class, Type.class, Type.class),

    ;

    private final FunctionDesc desc;

    private final String code;

    private final List<Class<?>> parameterTypes;

//    private final Class<?> resultType;


    Function(Class<?> resultType, Class<?>...argumentTypes) {
        this(resultType, Arrays.asList(argumentTypes),null);
    }

    Function(Class<?> resultType, List<Class<?>> parameterTypes, java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        this.parameterTypes = parameterTypes;
//        this.resultType = resultType;
//        this.resultTypeFunc = resultTypeFunc;
        code = extractCodeFromName(name());
        desc = new FunctionDesc(this);
    }

    private static String extractCodeFromName(String name) {
        int dollarIndex = name.indexOf('$');
        return dollarIndex == -1 ? name : name.substring(0 ,dollarIndex);
    }

    public static Function getByNameRequired(String name) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No func found for name: " + name));
    }

    public Type getReturnType(List<Type> argumentTypes) {
        return desc.getReturnType(argumentTypes);
    }

    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public Instance evaluate(List<Instance> arguments) {
        return desc.evaluate(arguments);
    }

    public String code() {
        return code;
    }
}
