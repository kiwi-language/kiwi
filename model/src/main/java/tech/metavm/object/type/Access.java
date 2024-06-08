package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;
import java.util.Arrays;

@EntityType
public enum Access {
    PUBLIC(1),
    PACKAGE(2),
    PRIVATE(3),
    PROTECTED(4)

    ;

    private final int code;

    Access(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static Access getByCode(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElseThrow(() -> BusinessException.invalidParams("Can not find Access for code: " + code));
    }

    public static @Nullable Access findByCode(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElse(null);
    }

}
