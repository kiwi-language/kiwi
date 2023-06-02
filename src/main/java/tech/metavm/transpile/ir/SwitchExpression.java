package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class SwitchExpression extends IRExpressionBase  {

    private final IRExpression expression;
    private final List<SwitchExpressionCase> cases;

    public SwitchExpression(IRExpression expression, List<SwitchExpressionCase> cases) {
        this.expression = expression;
        this.cases = new ArrayList<>(cases);
    }

    public IRExpression getExpression() {
        return expression;
    }

    public List<SwitchExpressionCase> getCases() {
        return cases;
    }

    @Override
    public IRType type() {
        return IRTypeUtil.getCompatibleType(NncUtils.map(cases, SwitchExpressionCase::getType));
    }


    @Override
    public List<Statement> getChildren() {
        return NncUtils.flatMap(cases, SwitchExpressionCase::getStatements);
    }
}
