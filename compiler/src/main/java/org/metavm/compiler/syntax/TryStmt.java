package org.metavm.compiler.syntax;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class TryStmt extends Stmt {

    private Block body;
    private List<Catcher> catchers;

    public TryStmt(Block body, List<Catcher> catchers) {
        this.body = body;
        this.catchers = catchers;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    public List<Catcher> getCatchers() {
        return catchers;
    }

    public void setCatchers(List<Catcher> catchers) {
        this.catchers = catchers;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("try ");
        writer.write(body);
        for (Catcher catcher : catchers) {
            writer.write(catcher);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitTryStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(body);
        catchers.forEach(action);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TryStmt tryStmt = (TryStmt) object;
        return Objects.equals(body, tryStmt.body) && Objects.equals(catchers, tryStmt.catchers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, catchers);
    }
}
