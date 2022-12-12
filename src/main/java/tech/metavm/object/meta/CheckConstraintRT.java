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

@EntityType("校验约束")
public class CheckConstraintRT extends ConstraintRT<CheckConstraintParam> {

    @EntityField("条件")
    private Value condition;

    public CheckConstraintRT(ConstraintDTO constraintDTO, CheckConstraintParam param, ClassType type) {
        super(ConstraintKind.CHECK, type, constraintDTO.message());
        setParam(param);
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

    public void setParam(CheckConstraintParam param) {
        condition = ValueFactory.getValue(param.value(), new TypeParsingContext(getDeclaringType()));
    }

    public boolean check(ClassInstance instance) {
        return Boolean.TRUE.equals(condition.evaluate(new InstanceEvaluationContext(instance)));
    }

}
