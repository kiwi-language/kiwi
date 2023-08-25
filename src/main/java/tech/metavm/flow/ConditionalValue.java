package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.Expression;

@EntityType("分支值")
public class ConditionalValue extends Entity {
    private final Branch branch;
    private final Expression value;

    public ConditionalValue(Branch branch, Expression value) {
        this.branch = branch;
        this.value = value;
    }

    public Branch getBranch() {
        return branch;
    }

    public Expression getValue() {
        return value;
    }
}
