package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.MergeFieldDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@EntityType("合并节点字段")
public class MergeNodeField extends Entity {

    @EntityField("字段")
    private final Field field;
    @ChildEntity("值")
    private final Table<ConditionalValue> values = new Table<>(ConditionalValue.class, true);

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
            values.add(new ConditionalValue(branch, value));
        } else {
            condValue.setValue(value);
        }
    }

    public void setValues(Map<Branch, Value> values) {
        values.forEach(this::setValue);
    }

    public List<ConditionalValue> getValues() {
        return Collections.unmodifiableList(values);
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
