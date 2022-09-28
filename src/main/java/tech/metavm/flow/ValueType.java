package tech.metavm.flow;

import java.util.Arrays;

public enum ValueType {
    CONSTANT(1),
    REFERENCE(2),
    EXPRESSION(3)

    ;

    private final int code;

    ValueType(int code) {
        this.code = code;
    }

    public static ValueType getByCodeRequired(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Flow node category " + code + " not found"));
    }

    public int code() {
        return code;
    }
}
