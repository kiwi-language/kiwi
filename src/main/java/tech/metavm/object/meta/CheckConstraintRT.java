package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.InstanceEvaluationContext;
import tech.metavm.object.instance.query.TypeParsingContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.InstanceUtils;

@EntityType("校验约束")
public class CheckConstraintRT extends ConstraintRT<CheckConstraintParam> {

    @EntityField("条件")
    private Value condition;

    public CheckConstraintRT(Value condition, ClassType type, String message) {
        super(ConstraintKind.CHECK, type, message);
        setCondition(condition);
    }

    @Override
    public String getDefaultMessage() {
        return "校验规则校验不通过";
    }

    @Override
    protected CheckConstraintParam getParam(boolean forPersistence) {
        return new CheckConstraintParam(
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

}
