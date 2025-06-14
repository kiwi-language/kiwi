package org.metavm.compiler.syntax;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.Name;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class BreakStmt extends Stmt {

    private @Nullable Name label;
    private @Nullable Node target;

    public BreakStmt(@Nullable Name label) {
        this.label = label;
    }

    @Nullable
    public Name getLabel() {
        return label;
    }

    public void setLabel(@Nullable Name label) {
        this.label = label;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("break");
        if(label != null) {
            writer.write(" ");
            writer.write(label);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitBreakStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
    }

    public Node getTarget() {
        return Objects.requireNonNull(target, "Break target not resolved yet");
    }

    public void setTarget(@NotNull Node target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BreakStmt breakStmt = (BreakStmt) object;
        return Objects.equals(label, breakStmt.label) && Objects.equals(target, breakStmt.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, target);
    }
}
