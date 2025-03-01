package org.metavm.compiler.element;

import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CapturedType implements Element, Type {

    private final TypeVariable typeVariable;
    private final UncertainType uncertainType;
    private @Nullable Closure closure;

    public CapturedType(TypeVariable typeVariable, UncertainType uncertainType) {
        this.typeVariable = typeVariable;
        this.uncertainType = uncertainType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CAPTURED_TYPE);
        Elements.writeReference(typeVariable, output);
    }

    @Override
    public SymName getName() {
        return null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return null;
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {

    }

    @Override
    public String getText() {
        return Element.super.getText();
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return type == PrimitiveType.NEVER || this == type;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return null;
    }

    @Override
    public int getTag() {
        return 0;
    }

    @Override
    public ElementTable getTable() {
        return uncertainType.getTable();
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return null;
    }

    @Override
    public TypeNode makeNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getUpperBound() {
        return uncertainType.getUpperBound();
    }

    @Override
    public Closure getClosure() {
        if (closure == null) {
            var cl = uncertainType.getUpperBound().getClosure();
            closure = cl.insert(this);
        }
        return closure;
    }

    public TypeVariable getTypeVariable() {
        return typeVariable;
    }

    public UncertainType getUncertainType() {
        return uncertainType;
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }
}
