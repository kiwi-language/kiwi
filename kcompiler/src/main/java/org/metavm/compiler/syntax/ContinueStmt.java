package org.metavm.compiler.syntax;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.Name;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class ContinueStmt extends Stmt {

    private @Nullable Name label;
    private @Nullable Node target;

    public ContinueStmt(@Nullable Name label) {
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
        writer.write("continue");
        if(label != null) {
            writer.write(" ");
            writer.write(label);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitContinueStmt(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
    }

    public Node getTarget() {
        return Objects.requireNonNull(target, "Target not set yet");
    }

    public void setTarget(@NotNull Node target) {
        this.target = target;
    }
}
