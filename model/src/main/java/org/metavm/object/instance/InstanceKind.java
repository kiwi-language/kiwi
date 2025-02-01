package org.metavm.object.instance;

import org.metavm.object.instance.rest.*;
import org.metavm.util.Utils;

public enum InstanceKind {

    CLASS(1, ClassInstanceParam.class),
    ARRAY(2, ArrayInstanceParam.class),
    PRIMITIVE(3, PrimitiveInstanceParam.class),
    LIST(4, ListInstanceParam.class),
    STRING(5, StringInstanceParam.class),


    ;

    private final int code;
    private final Class<?> paramClass;

    InstanceKind(int code, Class<?> paramClass) {
        this.code = code;
        this.paramClass = paramClass;
    }

    public static InstanceKind getByParamClass(Class<?> paramClass) {
        return Utils.findRequired(
                values(),
                v -> v.paramClass == paramClass
        );
    }

    public static InstanceKind getByCode(int code) {
        return Utils.findRequired(
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
