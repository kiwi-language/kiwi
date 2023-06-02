package tech.metavm.transpile.ir;

import java.util.List;

public record ExpressionSwitchRuleOutcome(
        IRExpression expression
) implements SwitchRuleOutcome {
    @Override
    public IRType getType() {
        return expression.type();
    }

    @Override
    public List<Statement> getStatements() {
        return List.of(expression);
    }
}
