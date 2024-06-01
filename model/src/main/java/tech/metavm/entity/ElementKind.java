package tech.metavm.entity;

import tech.metavm.expression.Expression;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

@EntityType("元素类型")
public enum ElementKind {

    TYPE(1, Type.class),
    FLOW(2, Flow.class),
    FIELD(3, Field.class),
    VALUE(4, Value.class),
    EXPRESSION(5, Expression.class),
    NODE(6, NodeRT.class),
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
