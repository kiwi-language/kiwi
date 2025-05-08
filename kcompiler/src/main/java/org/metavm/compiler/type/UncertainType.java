package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.ConstantTags;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.syntax.UncertainTypeNode;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;

public final class UncertainType implements Type, Comparable<UncertainType> {
    private final Type lowerBound;
    private final Type upperBound;

    UncertainType(Type lowerBound, Type upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.write("[");
        writer.writeType(lowerBound);
        writer.write(", ");
        writer.writeType(upperBound);
        writer.write("]");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return lowerBound.isAssignableFrom(type);
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitUncertainType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_UNCERTAIN;
    }

    @Override
    public int compareTo(@NotNull UncertainType o) {
        if (this == o)
            return 0;
        if (o instanceof UncertainType that) {
            var r = Types.instance.compare(lowerBound, that.lowerBound);
            if (r != 0)
                return r;
            return Types.instance.compare(upperBound, that.upperBound);
        }
        else
            return Integer.compare(getTag(), o.getTag());
    }

    @Override
    public boolean contains(Type that) {
        return that.isAssignableFrom(lowerBound) && upperBound.isAssignableFrom(that);
    }

    public Type getLowerBound() {
        return lowerBound;
    }

    public Type getUpperBound() {
        return upperBound;
    }

    @Override
    public ElementTable getTable() {
        return upperBound.getTable();
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return "[" + lowerBound.getInternalName(current) + "," + upperBound.getInternalName(current) + "]";
    }

    @Override
    public TypeNode makeNode() {
        var node = new UncertainTypeNode(lowerBound.makeNode(), upperBound.makeNode());
        node.setType(this);
        return node;
    }

    @Override
    public String toString() {
        return "UncertainType[" +
                "lowerBound=" + lowerBound + ", " +
                "upperBound=" + upperBound + ']';
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.UNCERTAIN_TYPE);
        lowerBound.write(output);
        upperBound.write(output);
    }

    @Override
    public Closure getClosure() {
        return upperBound.getClosure();
    }
}
