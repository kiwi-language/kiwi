package tech.metavm.object.meta;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionEvaluator;
import tech.metavm.object.instance.query.ExpressionParser;
import tech.metavm.object.instance.query.VarType;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;

public class CheckConstraintRT extends ConstraintRT<CheckConstraintParam> {

    private Expression expression;

    public CheckConstraintRT(ConstraintPO po, CheckConstraintParam param, Type type) {
        super(po, type);
        setParam(param);
    }

    public CheckConstraintRT(ConstraintDTO constraintDTO, CheckConstraintParam param, Type type) {
        super(ConstraintKind.CHECK, type);
        setParam(param);
    }

    @Override
    protected CheckConstraintParam getParam(boolean forPersistence) {
        return new CheckConstraintParam(
                expression.buildSelf(forPersistence ? VarType.ID : VarType.NAME)
        );
    }

    @Override
    public String getDesc() {
        return expression.buildSelf(VarType.NAME);
    }

    public void setParam(CheckConstraintParam param) {
        expression = ExpressionParser.parse(getType(), param.expression());
    }

    public boolean check(Instance instance) {
        return Boolean.TRUE.equals(ExpressionEvaluator.evaluate(expression, instance));
    }

}
