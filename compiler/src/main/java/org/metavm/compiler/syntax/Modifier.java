package org.metavm.compiler.syntax;

import java.util.Objects;
import java.util.function.Consumer;

public final class Modifier extends Node {
    private final ModifierTag tag;

    public Modifier(ModifierTag tag) {
        this.tag = tag;
    }

    public static Modifier fromText(String text) {
        return new Modifier(ModifierTag.valueOf(text.toUpperCase()));
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(tag.name().toLowerCase());
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitModifier(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    public ModifierTag tag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Modifier) obj;
        return Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public Modifier setPos(int pos) {
        return (Modifier) super.setPos(pos);
    }
}
