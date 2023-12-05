package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.util.BusinessException;

import java.util.Arrays;

@EntityType("可见范围")
public enum Access {
    @EnumConstant("公开")
    PUBLIC(1),
    @EnumConstant("模块内可见")
    MODULE(2),
    @EnumConstant("隐藏")
    PRIVATE(3)

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
