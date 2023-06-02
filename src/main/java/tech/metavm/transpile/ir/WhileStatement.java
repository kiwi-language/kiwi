package tech.metavm.transpile.ir;

import java.util.List;

public record WhileStatement(
        IRExpression condition,
        Statement body
) implements Statement {

    @Override
    public List<Statement> getChildren() {
        return List.of(body);
    }
}
