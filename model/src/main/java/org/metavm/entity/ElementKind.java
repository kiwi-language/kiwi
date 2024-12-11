package org.metavm.entity;

import org.metavm.api.Entity;
import org.metavm.expression.Expression;
import org.metavm.flow.Value;
import org.metavm.flow.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

@Entity
public enum ElementKind {

    TYPE(1, Type.class),
    FLOW(2, Flow.class),
    FIELD(3, Field.class),
    VALUE(4, Value.class),
    EXPRESSION(5, Expression.class),
    NODE(6, Node.class),
    METHOD(7, Method.class),
    FUNCTION(8, Function.class)

    ;

    private final int code;
    private final Class<? extends Element> elementClass;

    ElementKind(int code, Class<? extends Element> elementClass) {
        this.code = code;
        this.elementClass = elementClass;
    }

    public int code() {
        return code;
    }

    public Class<? extends Element> elementClass() {
        return elementClass;
    }

    public static ElementKind getByElementClass(Class<? extends Element> elementClass) {
        var realClass = EntityUtils.getRealType(elementClass);
        return NncUtils.findRequired(values(), v -> v.elementClass == realClass);
    }

    public static ElementKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
