package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.ValueType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.Objects;

@ValueType("常量表达式")
public class ConstantExpression extends Expression {

    @EntityField("常量值")
    private final Instance value;

    public ConstantExpression(Instance value) {
        this.value = value;
    }

    public Instance getValue() {
        return value;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        if(value instanceof StringInstance stringInstance) {
            return "'" + stringInstance.getValue().replaceAll("'", "''") + "'";
        }
        else if(value instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue() + "";
        }
        else {
            return "$" + NncUtils.requireNonNull(value.getId());
        }
    }

    @Override
    public Type getType() {
        return ValueUtil.getValueType(value);
    }

    @Override
    public int precedence() {
        return 0;
    }

}
