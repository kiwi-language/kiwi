package tech.metavm.object.meta;

import tech.metavm.entity.ConstraintDef;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.util.InstanceUtils;

@EntityType("校验约束")
public class CheckConstraint extends Constraint<CheckConstraintParamDTO> {

    @EntityField("条件")
    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(Value condition, ClassType type, String message) {
        super(ConstraintKind.CHECK, type, message);
        setCondition(condition);
    }

    @Override
    public String getDefaultMessage() {
        return "校验规则校验不通过";
    }

    @Override
    protected CheckConstraintParamDTO getParam(boolean forPersistence) {
        return new CheckConstraintParamDTO(
                condition.toDTO(forPersistence)
        );
    }

    @Override
    public String getDesc() {
        return "";
    }

    public void setCondition(Value condition) {
        this.condition = condition;
    }

    public boolean check(ClassInstance instance) {
        return InstanceUtils.isTrue(condition.evaluate(new InstanceEvaluationContext(instance)));
    }

    public ConstraintDef<?> getConstraintDef() {
        return constraintDef;
    }

    public void setConstraintDef(ConstraintDef<?> constraintDef) {
        this.constraintDef = constraintDef;
    }
}
