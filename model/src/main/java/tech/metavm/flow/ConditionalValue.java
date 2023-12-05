package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.ConditionalValueDTO;

@EntityType("分支值")
public class ConditionalValue extends Entity {
    @EntityField("分支")
    private final Branch branch;
    @ChildEntity("值")
    private Value value;

    public ConditionalValue(Branch branch, Value value) {
        this.branch = branch;
        this.value = addChild(value, "value");
    }

    public Branch getBranch() {
        return branch;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = addChild(value, "value");
    }

    public ConditionalValueDTO toDTO(boolean persisting) {
        try(var context = SerializeContext.enter()) {
            return new ConditionalValueDTO(
                    context.getRef(branch),
                    value.toDTO(persisting)
            );
        }
    }

}
