package org.metavm.flow;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.ConditionalValueDTO;

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
