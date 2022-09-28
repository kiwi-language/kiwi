package tech.metavm.object.instance.query;

import java.util.Arrays;
import java.util.List;

public enum Function {
    IS_BLANK(),
    MAX(),
    MIN(),
    IF(),
    SUM();

    private final FunctionDesc desc;

    Function() {
        desc = new FunctionDesc(this);
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
