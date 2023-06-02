package tech.metavm.transpile.ir;


import java.util.List;

public record SwitchExpressionCase(
        SwitchCaseCondition caseCondition,
        SwitchRuleOutcome outcome
) {

    public IRType getType() {
        return outcome.getType();
    }

    public List<Statement> getStatements() {
        return outcome.getStatements();
    }

}
