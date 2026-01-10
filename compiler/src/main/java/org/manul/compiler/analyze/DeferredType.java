package org.manul.compiler.analyze;

import org.jetbrains.annotations.Nullable;
import org.manul.compiler.element.ElementTable;
import org.manul.compiler.element.ElementWriter;
import org.manul.compiler.element.Func;
import org.manul.compiler.syntax.TypeNode;
import org.manul.compiler.type.Closure;
import org.manul.compiler.type.Type;
import org.manul.compiler.type.TypeVisitor;
import org.manul.compiler.generate.KlassOutput;

public class DeferredType implements Type {

    public static final DeferredType instance = new DeferredType();

    private DeferredType() {
    }

    @Override
    public void write(KlassOutput output) {
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.write("deferred");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return false;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitDeferredType(this);
    }

    @Override
    public int getTag() {
        return 0;
    }

    @Override
    public ElementTable getTable() {
        return null;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return null;
    }

    @Override
    public TypeNode makeNode() {
        return null;
    }

    @Override
    public Closure getClosure() {
        return null;
    }
}
