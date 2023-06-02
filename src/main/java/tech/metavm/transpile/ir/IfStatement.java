package tech.metavm.transpile.ir;

import javax.annotation.Nullable;
import java.util.List;

public record IfStatement(
        IRExpression expression,
        CodeBlock block,
        Statement body,
        @Nullable Statement elseBody
) implements Statement {

    @Override
    public List<Statement> getChildren() {
        if(elseBody != null) {
            return List.of(body, elseBody);
        }
        else {
            return List.of(body);
        }
    }
}
