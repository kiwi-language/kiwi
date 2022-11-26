package tech.metavm.flow;

import java.util.Arrays;

public enum ValueKind {
    CONSTANT(1),
    REFERENCE(2),
    EXPRESSION(3)

    ;

    private final int code;

    ValueKind(int code) {
        this.code = code;
    }

    public static ValueKind getByCodeRequired(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Flow node category " + code + " not found"));
    }

    public int code() {
        return code;
    }
}
