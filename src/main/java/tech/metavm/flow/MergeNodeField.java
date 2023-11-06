package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.MergeFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.entity.ChildArray;
import tech.metavm.util.NncUtils;
import tech.metavm.entity.ReadonlyArray;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;

@EntityType("合并节点字段")
public class MergeNodeField extends Entity {

    @EntityField("字段")
    private final Field field;
    @ChildEntity("值")
    private final ChildArray<ConditionalValue> values = addChild(new ChildArray<>(ConditionalValue.class), "values");

    public MergeNodeField(Field field, MergeNode mergeNode) {
        this(field, mergeNode, null);
    }

    public MergeNodeField(Field field, MergeNode mergeNode, @Nullable Map<Branch, Value> values) {
        this.field = field;
        mergeNode.addField(this);
        if(values != null) {
            setValues(values);
        }
    }

    public Field getField() {
        return field;
    }

    public Value getValue(Branch branch) {
        return values.get(ConditionalValue::getBranch, branch).getValue();
    }

    public void setValue(Branch branch, Value value) {
        var condValue = values.get(ConditionalValue::getBranch, branch);
        if (condValue == null) {
            values.addChild(new ConditionalValue(branch, value));
        } else {
            condValue.setValue(value);
        }
    }

    public void setValues(Map<Branch, Value> values) {
        values.forEach(this::setValue);
    }

    public ReadonlyArray<ConditionalValue> getValues() {
        return values;
    }

    public MergeFieldDTO toDTO(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new MergeFieldDTO(
                    field.getName(),
                    context.getRef(field),
                    context.getRef(field.getType()),
                    NncUtils.sortAndMap(values,
                            Comparator.comparingLong(value -> value.getBranch().getIndex()),
                            v -> v.toDTO(persisting))
            );
        }
    }
}
