package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.ArrayParamDTO;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.PrimitiveParamDTO;
import tech.metavm.util.NncUtils;

public enum InstanceKind {

    CLASS(1, ClassInstanceParam.class),
    ARRAY(2, ArrayParamDTO.class),
    PRIMITIVE(3, PrimitiveParamDTO.class)

    ;

    private final int code;
    private final Class<?> paramClass;

    InstanceKind(int code, Class<?> paramClass) {
        this.code = code;
        this.paramClass = paramClass;
    }

    public static InstanceKind getByParamClass(Class<?> paramClass) {
        return NncUtils.findRequired(
                values(),
                v -> v.paramClass == paramClass
        );
    }

    public static InstanceKind getByCode(int code) {
        return NncUtils.findRequired(
                values(),
                v -> v.code == code
        );
    }

    public int code() {
        return code;
    }

    public Class<?> paramClass() {
        return paramClass;
    }

}
