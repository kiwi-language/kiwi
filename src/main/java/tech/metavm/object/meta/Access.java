package tech.metavm.object.meta;

import tech.metavm.util.BusinessException;

import java.util.Arrays;

public enum Access {
    Public(1),
    Protected(2),
    Private(3)

    ;

    private final int code;

    Access(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static Access getByCodeRequired(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElseThrow(() -> BusinessException.invalidParams("访问控制类型不存在: " + code));
    }

    public static Access getByCode(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElse(null);
    }

}
