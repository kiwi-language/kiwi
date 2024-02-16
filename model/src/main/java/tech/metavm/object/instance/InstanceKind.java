package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.ArrayInstanceParam;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.ListInstanceParam;
import tech.metavm.object.instance.rest.PrimitiveInstanceParam;
import tech.metavm.util.NncUtils;

public enum InstanceKind {

    CLASS(1, ClassInstanceParam.class),
    ARRAY(2, ArrayInstanceParam.class),
    PRIMITIVE(3, PrimitiveInstanceParam.class),
    LIST(4, ListInstanceParam.class),

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
