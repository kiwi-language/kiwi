package tech.metavm.transpile.ir;

import java.util.List;

public record Synchronized(IRExpression expression, CodeBlock body) implements Statement{

    @Override
    public List<Statement> getChildren() {
        return List.of(body);
    }

}
