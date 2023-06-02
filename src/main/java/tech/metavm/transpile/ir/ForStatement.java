package tech.metavm.transpile.ir;

import java.util.List;

public record ForStatement(
        CodeBlock block,
        ForControl control,
        Statement body
) implements Statement {

    @Override
    public List<Statement> getChildren() {
        return List.of(body);
    }
}
