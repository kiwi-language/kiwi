package tech.metavm.flow;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

import java.util.Arrays;

@EntityType("值类别")
public enum ValueKind {

    @EnumConstant("常量")
    CONSTANT(1),
    @EnumConstant("引用")
    REFERENCE(2),
    @EnumConstant("表达式")
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
