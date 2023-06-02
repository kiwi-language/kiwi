package tech.metavm.transpile.ir;

import tech.metavm.transpile.BlockTypeResolver;

import java.util.List;

public record StatementsSwitchRuleOutcome(
        List<Statement> statements
) implements SwitchRuleOutcome {
    @Override
    public IRType getType() {
        return BlockTypeResolver.resolve(
                Yield.class,
                Yield::expression,
                statements
        );
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }
}
