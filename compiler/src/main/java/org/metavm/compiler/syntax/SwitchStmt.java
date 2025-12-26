package org.metavm.compiler.syntax;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class SwitchStmt extends Stmt {
    private final Expr selector;
    private final List<CaseClause> cases;

    public SwitchStmt(
            Expr selector,
            List<CaseClause> cases
    ) {
        this.selector = selector;
        this.cases = cases;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("switch (");
        selector.write(writer);
        writer.writeln(") {");
        writer.indent();
        for (CaseClause c : cases) {
            c.write(writer);
            writer.writeln();
        }
        writer.deIndent();
        writer.writeln("}");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitSwitchStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(selector);
        cases.forEach(action);
    }

    public Expr selector() {
        return selector;
    }

    public List<CaseClause> cases() {
        return cases;
    }

    @Override
    public String toString() {
        return "SwitchStmt[" +
                "selector=" + selector + ", " +
                "cases=" + cases + ']';
    }

}
