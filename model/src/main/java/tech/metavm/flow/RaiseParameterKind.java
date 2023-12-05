package tech.metavm.flow;

import tech.metavm.entity.EnumConstant;
import tech.metavm.util.NncUtils;

public enum RaiseParameterKind {

    @EnumConstant("错误信息")
    MESSAGE(1),
    @EnumConstant("中断对象")
    THROWABLE(2),
    ;

    private final int code;

    RaiseParameterKind(int code) {
        this.code = code;
    }

    public static RaiseParameterKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public int getCode() {
        return code;
    }
}
