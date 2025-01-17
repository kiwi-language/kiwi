package org.metavm.expression;

import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.Utils;

import java.util.Arrays;
import java.util.List;

public enum Func {

    STARTS_WITH(1, Boolean.class, Object.class, String.class),
    CONTAINS(2, Boolean.class, Object.class, String.class),
    ;

    private final int code;

    private final FunctionDesc desc;

    private final List<Class<?>> parameterTypes;

//    private final Class<?> resultType;


    Func(int code, Class<?> resultType, Class<?>...argumentTypes) {
        this(code, resultType, Arrays.asList(argumentTypes),null);
    }

    Func(int code, Class<?> resultType, List<Class<?>> parameterTypes, java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        this.code = code;
        this.parameterTypes = parameterTypes;
//        this.resultType = resultType;
//        this.resultTypeFunc = resultTypeFunc;
        desc = new FunctionDesc(this);
    }

    public static Func getByName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No func found for name: " + name));
    }

    public static Func fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public Type getReturnType(List<Type> argumentTypes) {
        return desc.getReturnType(argumentTypes);
    }

    public List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public Value evaluate(List<Value> arguments) {
        return desc.evaluate(arguments);
    }

    public int code() {
        return code;
    }
}
