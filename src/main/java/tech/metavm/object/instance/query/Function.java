package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.StdTypeConstants;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ValueUtil;

import java.util.Arrays;
import java.util.List;

public enum Function {
    IS_BLANK(StdTypeConstants.BOOL),
    MAX,
    MIN,
    SUM,
    IF(types -> ValueUtil.getConvertibleType(types.get(1), types.get(2))),

    ;

    private final FunctionDesc desc;

    private final Long resultTypeId;

    private final java.util.function.Function<List<Type>, Type> resultTypeFunc;

    Function() {
        this(null, null);
    }

    Function(Long resultTypeId) {
        this(resultTypeId, null);
    }

    Function(java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        this(null, resultTypeFunc);
    }

    Function(Long resultTypeId, java.util.function.Function<List<Type>, Type> resultTypeFunc) {
        desc = new FunctionDesc(this);
        this.resultTypeId = resultTypeId;
        this.resultTypeFunc = resultTypeFunc;
    }

    public Type getResultType(List<Type> argumentTypes, EntityContext context) {
        if(resultTypeId != null) {
            return context.getType(resultTypeId);
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
