package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.util.BusinessException;

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

    public static Access fromCode(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElseThrow(() -> BusinessException.invalidParams("Can not find Access for code: " + code));
    }

    public static @Nullable Access findByCode(int code) {
        return Arrays.stream(values()).filter(acc -> acc.code == code).findAny()
                .orElse(null);
    }

}
