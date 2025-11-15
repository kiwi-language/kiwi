package org.metavm.compiler.analyze;

import org.jetbrains.annotations.Nullable;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.Closure;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.TypeVisitor;
import org.metavm.compiler.generate.KlassOutput;

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
