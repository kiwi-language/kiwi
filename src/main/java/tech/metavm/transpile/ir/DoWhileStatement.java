package tech.metavm.transpile.ir;

import java.util.List;

public record DoWhileStatement(
        Statement body,
        IRExpression condition
) implements Statement {

    @Override
    public List<Statement> getChildren() {
        return List.of(body);
    }

}
