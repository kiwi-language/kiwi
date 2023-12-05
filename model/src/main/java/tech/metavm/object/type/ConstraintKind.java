package tech.metavm.object.type;

import tech.metavm.object.type.rest.dto.ConstraintKindCodes;
import tech.metavm.util.NncUtils;

public enum ConstraintKind {
    UNIQUE(ConstraintKindCodes.UNIQUE, IndexParam.class),
    CHECK(ConstraintKindCodes.CHECK, CheckConstraintParam.class)
    ;

    private final int code;
    private final Class<?> paramClass;

    ConstraintKind(int code, Class<?> paramClass) {
        this.code = code;
        this.paramClass = paramClass;
    }

    public static ConstraintKind getByParamClass(Class<?> klass) {
        return NncUtils.findRequired(values(), v -> v.paramClass.equals(klass));
    }

    public int code() {
        return code;
    }

    public Class<?> paramClass() {
        return paramClass;
    }

    public static ConstraintKind getByCode(int code) {
        return NncUtils.findRequired(values(), t -> t.code == code);
    }

}
