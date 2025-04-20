package org.metavm.compiler.element;

import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CapturedType extends ElementBase implements Element, Type {

    private final TypeVar typeVar;
    private final UncertainType uncertainType;
    private @Nullable Closure closure;

    public CapturedType(TypeVar typeVar, UncertainType uncertainType) {
        this.typeVar = typeVar;
        this.uncertainType = uncertainType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CAPTURED_TYPE);
        Elements.writeReference(typeVar, output);
    }

    @Override
    public Name getName() {
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
    public void writeType(ElementWriter writer) {

    }

    @Override
    public String getTypeText() {
        return super.getText();
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

    public TypeVar getTypeVariable() {
        return typeVar;
    }

    public UncertainType getUncertainType() {
        return uncertainType;
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }
}
