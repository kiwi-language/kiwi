package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.ConditionalValueDTO;

@EntityType
public class ConditionalValue extends Entity {
    private final Branch branch;
    private Value value;

    public ConditionalValue(Branch branch, Value value) {
        this.branch = branch;
        this.value = value;
    }

    public Branch getBranch() {
        return branch;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public ConditionalValueDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new ConditionalValueDTO(
                    serContext.getStringId(branch),
                    value.toDTO()
            );
        }
    }

    public String getText() {
        return branch.getIndex() + ": " + value.getText();
    }
}
