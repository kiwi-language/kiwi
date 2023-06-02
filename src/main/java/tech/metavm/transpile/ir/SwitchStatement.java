package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.List;

public record SwitchStatement(
        IRExpression expression,
        List<SwitchCase> cases
) implements Statement {

    @Override
    public List<Statement> getChildren() {
        return NncUtils.flatMap(cases, SwitchCase::statements);
    }
}
