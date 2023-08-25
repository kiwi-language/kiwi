package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.Expression;
import tech.metavm.object.meta.Field;
import tech.metavm.util.Table;

import java.util.HashMap;
import java.util.Map;

@EntityType("分支输出字段")
public class BranchNodeOutputField extends Entity {

    @EntityField("字段")
    private final Field field;
    @EntityField("值")
    private final Table<ConditionalValue> values = new Table<>(ConditionalValue.class);

    public BranchNodeOutputField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public void setValue(Branch branch, Expression value) {
        values.remove(ConditionalValue::getBranch, branch);
        values.add(new ConditionalValue(branch, value));
    }

}
