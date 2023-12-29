package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.MergeFieldDTO;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

@EntityType("合并节点字段")
public class MergeNodeField extends Entity implements LocalKey {

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
        if (values != null) {
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

    public MergeFieldDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new MergeFieldDTO(
                    field.getName(),
                    serContext.getRef(field),
                    serContext.getRef(field.getType()),
                    NncUtils.sortAndMap(values,
                            Comparator.comparingLong(value -> value.getBranch().getIndex()),
                            ConditionalValue::toDTO)
            );
        }
    }

    @Override
    public boolean isValidLocalKey() {
        return field.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(field.getCode());
    }

    public String getText() {
        return field.getName() + ": {" + NncUtils.join(values, ConditionalValue::getText, ",") + "}";
    }
}
